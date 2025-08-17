#!/usr/bin/env -S PYTHONPATH=../../../tools/extract-utils python3
#
# SPDX-FileCopyrightText: 2024 The LineageOS Project
# SPDX-License-Identifier: Apache-2.0
#

from extract_utils.file import File
from extract_utils.fixups_blob import (
    BlobFixupCtx,
    blob_fixup,
    blob_fixups_user_type,
)
from extract_utils.fixups_lib import (
    lib_fixup_remove,
    lib_fixup_remove_arch_suffix,
    lib_fixup_vendorcompat,
    lib_fixups_user_type,
    libs_clang_rt_ubsan,
    libs_proto_3_9_1,
)
from extract_utils.main import (
    ExtractUtils,
    ExtractUtilsModule,
)

namespace_imports = [
    'device/xiaomi/sm8635-common',
    'hardware/qcom-caf/sm8650',
    'hardware/qcom-caf/wlan',
    'hardware/xiaomi',
    'vendor/qcom/opensource/commonsys/display',
    'vendor/qcom/opensource/commonsys-intf/display',
    'vendor/qcom/opensource/dataservices',
]

def lib_fixup_vendor_suffix(lib: str, partition: str, *args, **kwargs):
    return f'{lib}_{partition}' if partition == 'vendor' else None


lib_fixups: lib_fixups_user_type = {
    libs_clang_rt_ubsan: lib_fixup_remove_arch_suffix,
    libs_proto_3_9_1: lib_fixup_vendorcompat,
    (
        'com.qualcomm.qti.dpm.api@1.0',
        'vendor.qti.diaghal@1.0',
        'vendor.qti.hardware.dpmaidlservice-V1-ndk',
        'vendor.qti.hardware.dpmservice@1.0',
        'vendor.qti.hardware.qccsyshal@1.0',
        'vendor.qti.hardware.qccsyshal@1.1',
        'vendor.qti.hardware.qccsyshal@1.2',
        'vendor.qti.hardware.wifidisplaysession@1.0',
        'vendor.qti.imsrtpservice@3.0',
        'vendor.qti.imsrtpservice@3.1',
        'vendor.qti.ImsRtpService-V1-ndk',
        'vendor.qti.qccvndhal_aidl-V1-ndk',
    ): lib_fixup_vendor_suffix,
    (
        'android.hardware.camera.device-V1-ndk',
        'android.hardware.camera.metadata-V2-ndk',
        'android.hardware.graphics.composer3-V2-ndk',
        'libagmclient',
        'libagmmixer',
        'libar-acdb',
        'libar-gsl',
        'libats',
        'liblx-osal',
        'libpalclient',
        'vendor.qti.hardware.AGMIPC@1.0-impl',
        'vendor.qti.hardware.display.composer3-V1-ndk',
    ): lib_fixup_remove,
}

blob_fixups: blob_fixups_user_type = {
    (
        'odm/lib64/libcamxcommonutils.so',
        'odm/lib64/libmialgoengine.so',
        'vendor/lib64/libcameraopt.so',
    ): blob_fixup()
        .add_needed('libprocessgroup_shim.so'),
    (
        'odm/lib64/libalLDC.so',
        'odm/lib64/libAncHumanVideoBokehV4.so',
        'odm/lib64/libanc_single_rt_bokeh.so',
        'odm/lib64/libMiEmojiEffect.so',
        'odm/lib64/libTrueSight.so',
        'vendor/lib64/libMiPhotoFilter.so',
        'vendor/lib64/libMiVideoFilter.so',
        'vendor/lib64/libmorpho_ubwc.so',
    ): blob_fixup()
        .clear_symbol_version('AHardwareBuffer_allocate')
        .clear_symbol_version('AHardwareBuffer_describe')
        .clear_symbol_version('AHardwareBuffer_isSupported')
        .clear_symbol_version('AHardwareBuffer_lock')
        .clear_symbol_version('AHardwareBuffer_lockPlanes')
        .clear_symbol_version('AHardwareBuffer_release')
        .clear_symbol_version('AHardwareBuffer_unlock'),
    (
        'vendor/lib64/c2.dolby.hevc.dec.so',
        'vendor/lib64/c2.dolby.hevc.sec.dec.so',
        'vendor/lib64/libqcodec2_core.so',
    ): blob_fixup()
        .add_needed('libcodec2_shim.so'),
    'vendor/lib64/c2.dolby.client.so' : blob_fixup()
        .add_needed('dolbycodec_shim.so'),
    'vendor/lib64/vendor.libdpmframework.so': blob_fixup()
        .add_needed('libhidlbase_shim.so'),
    'vendor/etc/ueventd.rc' : blob_fixup()
        .add_line_if_missing('\n# Charger\n/sys/class/qcom-battery     night_charging            0660    system  system')
}  # fmt: skip

module = ExtractUtilsModule(
    'sm8635-common',
    'xiaomi',
    blob_fixups=blob_fixups,
    lib_fixups=lib_fixups,
    namespace_imports=namespace_imports,
    check_elf=True,
)

if __name__ == '__main__':
    utils = ExtractUtils.device(module)
    utils.run()
