[AID_VENDOR_QTI_DIAG]
value:2901

[AID_VENDOR_QDSS]
value:2902

[AID_VENDOR_RFS]
value:2903

[AID_VENDOR_RFS_SHARED]
value:2904

[AID_VENDOR_ADPL_ODL]
value:2905

[AID_VENDOR_QRTR]
value:2906

[AID_VENDOR_THERMAL]
value:2907

[AID_VENDOR_FASTRPC]
value:2908

[AID_VENDOR_QTR]
value:2909

[AID_VENDOR_NXP_STRONGBOX]
value:2910

[AID_VENDOR_NXP_WEAVER]
value:2911

[AID_VENDOR_SSGTZD]
value:2912

[AID_VENDOR_THALES_STRONGBOX]
value:2913

[AID_VENDOR_QCC]
value:2914

[AID_VENDOR_NXP_AUTHSECRET]
value:2915

[AID_VENDOR_THALES_WEAVER]
value:2916

[system/vendor/bin/cnd]
mode: 0755
user: AID_SYSTEM
group: AID_SYSTEM
caps: NET_BIND_SERVICE NET_ADMIN BLOCK_SUSPEND

[system/vendor/bin/hw/android.hardware.bluetooth@1.0-service-qti]
mode: 0755
user: AID_SYSTEM
group: AID_SYSTEM
caps: NET_ADMIN BLOCK_SUSPEND

[system/vendor/bin/hw/android.hardware.bluetooth@1.0-service-qti-lazy]
mode: 0755
user: AID_SYSTEM
group: AID_SYSTEM
caps: NET_ADMIN BLOCK_SUSPEND

[system/vendor/bin/ims_rtp_daemon]
mode: 0755
user: AID_RADIO
group: AID_RADIO
caps: NET_BIND_SERVICE

[system/vendor/bin/imsdatadaemon]
mode: 0755
user: AID_RADIO
group: AID_RADIO
caps: NET_BIND_SERVICE

[system/vendor/bin/imsrcsd]
mode: 0755
user: AID_RADIO
group: AID_RADIO
caps: NET_BIND_SERVICE WAKE_ALARM BLOCK_SUSPEND

[system/vendor/bin/loc_launcher]
mode: 0755
user: AID_GPS
group: AID_GPS
caps: SETGID SETUID

[system/vendor/bin/pd-mapper]
mode: 0755
user: AID_SYSTEM
group: AID_SYSTEM
caps: NET_BIND_SERVICE

[system/vendor/bin/pm-service]
mode: 0755
user: AID_SYSTEM
group: AID_SYSTEM
caps: NET_BIND_SERVICE

[system/vendor/bin/slim_daemon]
mode: 0755
user: AID_GPS
group: AID_GPS
caps: NET_BIND_SERVICE

[system/vendor/bin/wcnss_filter]
mode: 0755
user: AID_BLUETOOTH
group: AID_BLUETOOTH
caps: BLOCK_SUSPEND

[vendor/bin/cnd]
mode: 0755
user: AID_SYSTEM
group: AID_SYSTEM
caps: NET_BIND_SERVICE NET_ADMIN BLOCK_SUSPEND

[vendor/bin/hw/android.hardware.bluetooth@1.0-service-qti]
mode: 0755
user: AID_BLUETOOTH
group: AID_BLUETOOTH
caps: NET_ADMIN BLOCK_SUSPEND

[vendor/bin/hw/android.hardware.bluetooth@1.0-service-qti-lazy]
mode: 0755
user: AID_BLUETOOTH
group: AID_BLUETOOTH
caps: NET_ADMIN BLOCK_SUSPEND

[vendor/bin/hw/android.hardware.security.keymint-service.strongbox-thales]
mode: 0755
user: 2913
group: AID_SYSTEM
caps: SYS_ADMIN SYS_NICE

[vendor/bin/hw/android.hardware.weaver-service.thales]
mode: 0755
user: 2916
group: AID_SYSTEM
caps: SYS_ADMIN SYS_NICE

[vendor/bin/ims_rtp_daemon]
mode: 0755
user: AID_RADIO
group: AID_RADIO
caps: NET_BIND_SERVICE

[vendor/bin/imsdaemon]
mode: 0755
user: AID_RADIO
group: AID_RADIO
caps: NET_BIND_SERVICE WAKE_ALARM BLOCK_SUSPEND

[vendor/bin/imsdatadaemon]
mode: 0755
user: AID_RADIO
group: AID_RADIO
caps: NET_BIND_SERVICE

[vendor/bin/imsrcsd]
mode: 0755
user: AID_RADIO
group: AID_RADIO
caps: NET_BIND_SERVICE WAKE_ALARM BLOCK_SUSPEND

[vendor/bin/loc_launcher]
mode: 0755
user: AID_GPS
group: AID_GPS
caps: SETGID SETUID

[vendor/bin/pd-mapper]
mode: 0755
user: AID_SYSTEM
group: AID_SYSTEM
caps: NET_BIND_SERVICE

[vendor/bin/pm-service]
mode: 0755
user: AID_SYSTEM
group: AID_SYSTEM
caps: NET_BIND_SERVICE

[vendor/bin/sensors.qti]
mode: 0755
user: AID_SYSTEM
group: AID_SYSTEM
caps: NET_BIND_SERVICE

[vendor/bin/slim_daemon]
mode: 0755
user: AID_GPS
group: AID_GPS
caps: NET_BIND_SERVICE

[vendor/bin/wcnss_filter]
mode: 0755
user: AID_BLUETOOTH
group: AID_BLUETOOTH
caps: BLOCK_SUSPEND

[vendor/bin/xtwifi-client]
mode: 0755
user: AID_GPS
group: AID_GPS
caps: NET_BIND_SERVICE WAKE_ALARM BLOCK_SUSPEND

[vendor/firmware_mnt/image/*]
mode: 0771
user: AID_SYSTEM
group: AID_SYSTEM
caps: 0

