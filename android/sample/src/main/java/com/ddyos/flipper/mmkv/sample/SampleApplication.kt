package com.ddyos.flipper.mmkv.sample

import android.app.Application
import com.ddyos.flipper.mmkv.plugin.MMKVDescriptor
import com.ddyos.flipper.mmkv.plugin.MMKVFlipperPlugin
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.soloader.SoLoader
import com.tencent.mmkv.MMKV

class SampleApplication : Application() {

    companion object {
        private const val cryptKey = "cryptKey"
    }

    override fun onCreate() {
        super.onCreate()

        SoLoader.init(this, false)
        MMKV.initialize(this)

        insertTestData()

        if (BuildConfig.DEBUG) {
            val client = AndroidFlipperClient.getInstance(this)

//            client.addPlugin(MMKVFlipperPlugin())
//            client.addPlugin(MMKVFlipperPlugin("other_mmkv"))
            client.addPlugin(
                MMKVFlipperPlugin(
                    listOf(
                        MMKVDescriptor("other_mmkv"),
                        MMKVDescriptor("another_mmkv", MMKV.MULTI_PROCESS_MODE, cryptKey)
                    )
                )
            )

            client.start()
        }
    }

    private fun insertTestData() {
        MMKV.defaultMMKV().encode("hello", "mmkv")
        MMKV.defaultMMKV().encode("key2", 123)
        MMKV.defaultMMKV().close()

        MMKV.mmkvWithID("other_mmkv").encode("key1", true)
        MMKV.mmkvWithID("other_mmkv").encode("test_mmkv2", "slidng")
        MMKV.mmkvWithID("other_mmkv").close()

        val cryptMMKV = MMKV.mmkvWithID("another_mmkv", MMKV.MULTI_PROCESS_MODE, cryptKey)
        cryptMMKV.encode("2key1", true)
        cryptMMKV.close()
    }
}