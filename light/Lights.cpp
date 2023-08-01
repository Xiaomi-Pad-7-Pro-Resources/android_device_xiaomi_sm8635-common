//
// Copyright (C) 2023 The LineageOS Project
//
// SPDX-License-Identifier: Apache-2.0
//

#include "Lights.h"

#include <android-base/logging.h>
#include <fstream>

#define RED_LED         "/sys/class/leds/red/"
#define GREEN_LED       "/sys/class/leds/green/"
#define BLUE_LED        "/sys/class/leds/blue/"

#define LED_BREATH          "breath"
#define LED_BRIGHTNESS      "brightness"
#define LED_DELAY_ON        "delay_on"
#define LED_DELAY_OFF       "delay_off"
#define LED_MAX_BRIGHTNESS  "max_brightness"

#define BATTERY_CAPACITY          "/sys/class/power_supply/battery/capacity"
#define BATTERY_STATUS_FILE       "/sys/class/power_supply/battery/status"

#define BATTERY_STATUS_CHARGING     "Charging"
#define BATTERY_STATUS_DISCHARGING  "Discharging"
#define BATTERY_STATUS_FULL         "Full"

enum battery_status {
    BATTERY_UNKNOWN = 0,
    BATTERY_LOW,
    BATTERY_FREE,
    BATTERY_CHARGING,
    BATTERY_FULL,
};

namespace {
/*
 * Write value to path and close file.
 */
static void set(std::string path, std::string value) {
    std::ofstream file(path);

    if (!file.is_open()) {
        LOG(WARNING) << "failed to write " << value.c_str() << " to " << path.c_str();
        return;
    }

    file << value;
}

static int get(std::string path) {
    std::ifstream file(path);
    int value;

    if (!file.is_open()) {
        LOG(WARNING) << "failed to read from: " << path.c_str();
        return 0;
    }

    file >> value;
    return value;
}

static int readStr(std::string path, char *buffer, size_t size)
{

    std::ifstream file(path);

    if (!file.is_open()) {
        LOG(WARNING) << "failed to read: " << path.c_str();
        return -1;
    }

    file.read(buffer, size);
    file.close();
    return 1;
}

static void set(std::string path, int value) {
    set(path, std::to_string(value));
}

int getBatteryStatus()
{
    int err;

    char status_str[16];
    int capacity = 0;

    err = readStr(BATTERY_STATUS_FILE, status_str, sizeof(status_str));
    if (err <= 0) {
        LOG(WARNING) << "failed to read battery status: " << err;
        return BATTERY_UNKNOWN;
    }

    capacity = get(BATTERY_CAPACITY);

    if (0 == strncmp(status_str, BATTERY_STATUS_FULL, 4)) {
            return BATTERY_FULL;
        }

    if (0 == strncmp(status_str, BATTERY_STATUS_DISCHARGING, 11)) {
            return BATTERY_FREE;
        }

    if (0 == strncmp(status_str, BATTERY_STATUS_CHARGING, 8)) {
        if (capacity < 90) {
            return BATTERY_CHARGING;
        } else {
            return BATTERY_FULL;
        }
    } else {
        if (capacity < 10) {
            return BATTERY_LOW;
        } else {
            return BATTERY_FREE;
        }
    }
}

static void handleNotification(int id, const HwLightState& state) {

    uint32_t brightness = (state.color >> 24) & 0xFF;
    uint32_t red = ((state.color >> 16) & 0xFF) * brightness / 0xFF;
    uint32_t green = ((state.color >> 8) & 0xFF) * brightness / 0xFF;
    uint32_t blue = (state.color & 0xFF) * brightness / 0xFF;

    switch (state.flashMode) {
        case FlashMode::HARDWARE:
            /* Enable breathing */
            if (!!red)
                set(RED_LED LED_BREATH, 1);

            if (!!green)
                set(GREEN_LED LED_BREATH, 1);

            if (!!blue)
                set(BLUE_LED LED_BREATH, 1);
            break;
        case FlashMode::TIMED:
            /* Enable blinking */
            if (!!red) {
                set(RED_LED LED_DELAY_ON, state.flashOnMs);
                set(RED_LED LED_DELAY_OFF, state.flashOffMs);
            }

            if (!!green) {
                set(GREEN_LED LED_DELAY_ON, state.flashOnMs);
                set(GREEN_LED LED_DELAY_OFF, state.flashOffMs);
            }

            if (!!blue) {
                set(BLUE_LED LED_DELAY_ON, state.flashOnMs);
                set(BLUE_LED LED_DELAY_OFF, state.flashOffMs);
            }
            break;
        case FlashMode::NONE:
        default:
            int battery_state = getBatteryStatus();
            if (id == (int) LightType::BATTERY) {
                if (battery_state == BATTERY_CHARGING || battery_state == BATTERY_LOW) {
                    set(GREEN_LED LED_BRIGHTNESS, 0);
                    set(BLUE_LED LED_BRIGHTNESS, 0);
                    set(RED_LED LED_BRIGHTNESS, red);
                } else if (battery_state == BATTERY_FULL) {
                    set(RED_LED LED_BRIGHTNESS, 0);
                    set(BLUE_LED LED_BRIGHTNESS, 0);
                    set(GREEN_LED LED_BRIGHTNESS, green);
                } else if (battery_state == BATTERY_FREE) {
                    set(RED_LED LED_BRIGHTNESS, 0);
                    set(GREEN_LED LED_BRIGHTNESS, 0);
                    set(BLUE_LED LED_BRIGHTNESS, 0);
                }
            } else if (id == (int) LightType::NOTIFICATIONS) {
                set(GREEN_LED LED_BRIGHTNESS, green);
                set(BLUE_LED LED_BRIGHTNESS, blue);
                set(RED_LED LED_BRIGHTNESS, red);
            }
            break;
    }
    return;
}

/* Keep sorted in the order of importance. */
static std::vector<LightType> backends = {
    LightType::ATTENTION,
    LightType::BATTERY,
    LightType::NOTIFICATIONS,
};

}  // anonymous namespace

namespace aidl {
namespace android {
namespace hardware {
namespace light {

ndk::ScopedAStatus Lights::setLightState(int id, const HwLightState& state) {
    LOG(INFO) << "Lights setting state for id=" << id << " to color " << std::hex << state.color;
    switch(id) {
        case (int) LightType::ATTENTION:
            handleNotification(id, state);
            return ndk::ScopedAStatus::ok();
        case (int) LightType::BATTERY:
            handleNotification(id, state);
            return ndk::ScopedAStatus::ok();
        case (int) LightType::NOTIFICATIONS:
            handleNotification(id, state);
            return ndk::ScopedAStatus::ok();
        default:
            return ndk::ScopedAStatus::fromExceptionCode(EX_UNSUPPORTED_OPERATION);
    }
}

ndk::ScopedAStatus Lights::getLights(std::vector<HwLight>* lights) {
    LOG(INFO) << "Lights reporting supported lights";
    int i = 0;

    for (const LightType& backend : backends) {
        HwLight hwLight;
        hwLight.id = (int) backend;
        hwLight.type = backend;
        hwLight.ordinal = i;
        lights->push_back(hwLight);
        i++;
    }

    return ndk::ScopedAStatus::ok();
}

}  // namespace light
}  // namespace hardware
}  // namespace android
}  // namespace aidl
