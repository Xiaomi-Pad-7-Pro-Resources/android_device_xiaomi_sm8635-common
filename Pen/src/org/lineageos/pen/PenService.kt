/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.pen

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.hardware.input.InputManager
import android.os.Handler
import android.os.IBinder
import android.os.UEventObserver
import android.provider.Settings
import android.provider.Settings.System.PEAK_REFRESH_RATE
import android.provider.Settings.System.MIN_REFRESH_RATE
import android.util.Log

class PenService : Service() {
    private val bluetoothManager by lazy { getSystemService(BluetoothManager::class.java) }
    private val inputManager by lazy { getSystemService(InputManager::class.java) }
    private val notificationManager by lazy { getSystemService(NotificationManager::class.java) }

    private val handler by lazy { Handler(mainLooper) }

    private val observer = object : UEventObserver() {
        private val lock = Any()

        override fun onUEvent(event: UEvent) {
            synchronized(lock) {
                val pencilStatus = event.get("POWER_SUPPLY_PEN_HALL3") ?: return
                val pencilAddr = event.get("POWER_SUPPLY_PEN_MAC")?.chunked(2)?.joinToString(":") {
                    it.uppercase()
                } ?: return

                when (pencilStatus) {
                    "0" -> postNotification(pencilAddr)
                    "1" -> notificationManager.cancel(NOTIFICATION_ID)
                }
            }
        }
    }

    private val inputObserver = object : InputManager.InputDeviceListener {
        override fun onInputDeviceAdded(deviceId: Int) {
            overridePeakRefreshRateIfNeeded()
        }

        override fun onInputDeviceRemoved(deviceId: Int) {
            overridePeakRefreshRateIfNeeded()
        }

        override fun onInputDeviceChanged(deviceId: Int) {
            // Do nothing
        }
    }

    private val peakRefreshRateSettingsObserver by lazy {
        object : ContentObserver(handler) {
            override fun onChange(selfChange: Boolean) {
                super.onChange(selfChange)

                overridePeakRefreshRateIfNeeded()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.getStringExtra(EXTRA_PENCIL_ADDR)?.let {
            bondBtDevice(it)
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        overridePeakRefreshRateIfNeeded()
        inputManager.registerInputDeviceListener(inputObserver, handler)
        observer.startObserving("DEVPATH=/devices/platform/soc/soc:qcom,pmic_glink/soc:qcom,pmic_glink:qcom,battery_charger")
    }

    override fun onDestroy() {
        super.onDestroy()

        if (lastOverrideRefreshRateStatus) unregisterOverrideRefreshRate()
        inputManager.unregisterInputDeviceListener(inputObserver)
        observer.stopObserving()
    }

    private fun bondBtDevice(pencilAddr: String) {
        val adapter = bluetoothManager.adapter
        @Suppress("DEPRECATION") adapter.enable()

        val scanner = run {
            repeat(50) {
                adapter.bluetoothLeScanner?.let {
                    return@run it
                }
                Thread.sleep(100)
            }
            return@run null
        }
        scanner?.startScan(
            listOf(ScanFilter.Builder().setDeviceAddress(pencilAddr).build()),
            ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setReportDelay(0L)
                .build(),
            object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult) {
                    super.onScanResult(callbackType, result)
                    scanner.stopScan(this)

                    result.device.createBond()
                }

                override fun onBatchScanResults(results: MutableList<ScanResult>) {
                    super.onBatchScanResults(results)
                    scanner.stopScan(this)
                }

                override fun onScanFailed(errorCode: Int) {
                    super.onScanFailed(errorCode)
                    scanner.stopScan(this)
                }
            }
        )
    }

    private fun disablePenEvents() {
        for (id in inputManager.inputDeviceIds) {
            if (isXiaomiPenDevice(id)) inputManager.disableInputDevice(id)
        }
    }

    private fun isXiaomiPenDevice(id: Int): Boolean {
        val inputDevice = inputManager.getInputDevice(id) ?: return false
        return inputDevice.getVendorId() == 34 && inputDevice.getProductId() == 19840
    }

    private fun overridePeakRefreshRateIfNeeded() {
        val isPenConnected = inputManager.inputDeviceIds.firstOrNull {
            return@firstOrNull isXiaomiPenDevice(it)
        } != null

        if (isPenConnected) {
            Settings.System.putString(contentResolver, PEAK_REFRESH_RATE, "120.0")
            Settings.System.putString(contentResolver, MIN_REFRESH_RATE, "120.0")
            disablePenEvents()
            if (!lastOverrideRefreshRateStatus) registerOverrideRefreshRate()
        } else if (!isPenConnected && lastOverrideRefreshRateStatus) {
            Settings.System.putString(contentResolver, PEAK_REFRESH_RATE, "0.0")
            Settings.System.putString(contentResolver, MIN_REFRESH_RATE, "0.0")
            if(lastOverrideRefreshRateStatus) unregisterOverrideRefreshRate()
        }
    }

    private fun postNotification(pencilAddr: String) {
        val adapter = bluetoothManager.adapter

        if (pencilAddr == "0") return

        if (adapter.bondedDevices.contains(adapter.getRemoteDevice(pencilAddr))) {
            Log.e(TAG, "$pencilAddr already bonded, bailing out")
            return
        }

        if (notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID) == null) {
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_ID,
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
        }

        val contentIntent = PendingIntent.getService(
            this,
            0,
            Intent(this, PenService::class.java).apply {
                putExtra(EXTRA_PENCIL_ADDR, pencilAddr)
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stylus)
            .setContentTitle(getString(R.string.pen_attached))
            .setContentText(getString(R.string.tap_to_connect))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun registerOverrideRefreshRate() {
        if (lastOverrideRefreshRateStatus) return
        lastOverrideRefreshRateStatus = true
        contentResolver.registerContentObserver(Settings.System.getUriFor(PEAK_REFRESH_RATE), false, peakRefreshRateSettingsObserver)
        handler.post { peakRefreshRateSettingsObserver.onChange(true) }
    }

    private fun unregisterOverrideRefreshRate() {
        if (!lastOverrideRefreshRateStatus) return

        try {
            contentResolver.unregisterContentObserver(peakRefreshRateSettingsObserver)
        } catch (e: Exception) {
            Log.w(TAG, "Unregister content observer failed", e)
        } finally {
            lastOverrideRefreshRateStatus = false
        }
    }

    companion object {
        private const val TAG = "XiaomiPenService"

        private const val EXTRA_PENCIL_ADDR = "pencil_addr"

        private const val NOTIFICATION_CHANNEL_ID = "XiaomiPen"
        private const val NOTIFICATION_ID = 1000

        private var lastOverrideRefreshRateStatus = false
    }
}
