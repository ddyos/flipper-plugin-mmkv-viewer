package com.ddyos.flipper.mmkv.plugin

import com.tencent.mmkv.MMKV

class MMKVDescriptor @JvmOverloads constructor(
    val name: String,
    val mode: Int = MMKV.SINGLE_PROCESS_MODE,
    val cryptKey: String? = null
)