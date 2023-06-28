//
// Copyright (C) 2023 The LineageOS Project
//
// SPDX-License-Identifier: Apache-2.0
//

#pragma once

#include <aidl/android/hardware/light/BnLights.h>

namespace aidl {
namespace android {
namespace hardware {
namespace light {

// Default implementation that reports no supported lights.
class Lights : public BnLights {
    ndk::ScopedAStatus setLightState(int id, const HwLightState& state) override;
    ndk::ScopedAStatus getLights(std::vector<HwLight>* types) override;
};

}  // namespace light
}  // namespace hardware
}  // namespace android
}  // namespace aidl
