package com.ddyos.flipper.mmkv.plugin

import android.text.TextUtils
import com.facebook.flipper.core.FlipperConnection
import com.facebook.flipper.core.FlipperObject
import com.facebook.flipper.core.FlipperPlugin
import com.tencent.mmkv.MMKV
import java.io.File
import java.util.*

/**
 * Flipper plugin for MMKV, modify from SharedPreferencesFlipperPlugin
 */
class MMKVFlipperPlugin : FlipperPlugin {

    companion object {
        private const val PLUGIN_ID = "flipper-plugin-mmkv-viewer"

        private fun buildDescriptorForDefault(): List<MMKVDescriptor> {
            val descriptors: MutableList<MMKVDescriptor> = ArrayList()
            descriptors.add(MMKVDescriptor(MMKV.defaultMMKV().mmapID()))

            descriptors.addAll(getAllMMKVDescriptors())
            return descriptors
        }

        fun getAllMMKVDescriptors(
            rootFolder: String = MMKV.getRootDir(),
            mmkvDescriptorMode: Int = MMKV.SINGLE_PROCESS_MODE,
            cryptKey: String? = null
        ): List<MMKVDescriptor>{

            val rootDirFile = File(rootFolder)
            if (!rootDirFile.isDirectory) return emptyList()

            return rootDirFile.listFiles { file -> file.extension != "crc" }
                .map { MMKVDescriptor(it.name, mmkvDescriptorMode, cryptKey) }
                ?: emptyList()
        }

    }

    private var mConnection: FlipperConnection? = null
    private val mMMKVMap: MutableMap<MMKV, MMKVDescriptor> = mutableMapOf()

    constructor (): this(buildDescriptorForDefault())

    @JvmOverloads
    constructor(
        name: String,
        mode: Int = MMKV.SINGLE_PROCESS_MODE,
        cryptKey: String? = null
    ) : this(
        listOf(
        MMKVDescriptor(
            name,
            mode,
            cryptKey
        )
    ))

    /**
     * Creates a MMKV plugin for Flipper
     *
     * @param descriptors A list of [MMKVDescriptor]s that describe the list of
     * preferences to retrieve.
     */
    constructor(descriptors: List<MMKVDescriptor>) {
        for (descriptor in descriptors) {
            val preferences = MMKV.mmkvWithID(descriptor.name, descriptor.mode, descriptor.cryptKey)
            mMMKVMap[preferences] = descriptor
        }
    }

    override fun getId(): String {
        return PLUGIN_ID
    }

    private fun getMMKVFor(name: String): MMKV {
        for ((key, value) in mMMKVMap) {
            if (value.name == name) {
                return key
            }
        }
        throw IllegalStateException("Unknown shared preferences $name")
    }

    private fun getFlipperObjectFor(name: String): FlipperObject? {
        return getFlipperObjectFor(getMMKVFor(name))
    }

    private fun getFlipperObjectFor(mmkv: MMKV): FlipperObject? {
        val builder = FlipperObject.Builder()
        val keys = mmkv.allKeys()
        keys?.let {
            for (key in keys) {
                val `val` = getObjectValue(mmkv, key)
                builder.put(key, `val`)
            }
        }
        return builder.build()
    }

    /**
     * get value with object type, because type-erasure inside mmkv
     */
    private fun getObjectValue(mmkv: MMKV, key: String): Any? {
        // string or string-set
        val value = mmkv.decodeString(key)
        if (!TextUtils.isEmpty(value)) {
            return if (value[0].toInt() == 0x01) {
                mmkv.decodeStringSet(key)
            } else {
                value
            }
        }
        // float double
        val set = mmkv.decodeStringSet(key)
        if (set != null && set.size == 0) {
            val valueFloat = mmkv.decodeFloat(key)
            val valueDouble = mmkv.decodeDouble(key)
            return if (valueFloat.compareTo(0f) == 0 || valueFloat.compareTo(Float.NaN) == 0) {
                valueDouble
            } else {
                valueFloat
            }
        }
        // int long bool
        // for bool, true = 1(int), false = 0(int)
        val valueInt = mmkv.decodeInt(key)
        val valueLong = mmkv.decodeLong(key)
        return if (valueInt.toLong() != valueLong) {
            valueLong
        } else {
            valueInt
        }
    }

    override fun onConnect(connection: FlipperConnection) {
        mConnection = connection
        connection.receive("getAllSharedPreferences") { _, responder ->
            val builder = FlipperObject.Builder()
            for ((key, value) in mMMKVMap) {
                builder.put(value.name, getFlipperObjectFor(key))
            }
            responder.success(builder.build())
        }
        connection.receive("getSharedPreferences") { params, responder ->
            val name = params.getString("name")
            if (name != null) {
                responder.success(getFlipperObjectFor(name))
            }
        }
        connection.receive("setSharedPreference") { params, responder ->
            val sharedPreferencesName = params.getString(
                    "sharedPreferencesName")
            val preferenceName = params.getString("preferenceName")
            val sharedPrefs = getMMKVFor(
                    sharedPreferencesName)
            val originalValue = getObjectValue(sharedPrefs, preferenceName)
            val editor = sharedPrefs.edit()
            if (originalValue is Boolean) {
                editor.putBoolean(preferenceName, params.getBoolean("preferenceValue"))
            } else if (originalValue is Long) {
                editor.putLong(preferenceName, params.getLong("preferenceValue"))
            } else if (originalValue is Int) {
                editor.putInt(preferenceName, params.getInt("preferenceValue"))
            } else if (originalValue is Float) {
                editor.putFloat(preferenceName, params.getFloat("preferenceValue"))
            } else if (originalValue is String) {
                editor.putString(preferenceName, params.getString("preferenceValue"))
            } else {
                throw IllegalArgumentException(
                        "Type not supported: $preferenceName")
            }
            editor.apply()
            responder.success(getFlipperObjectFor(sharedPreferencesName))
        }
    }

    override fun onDisconnect() {
        mConnection = null
    }

    override fun runInBackground(): Boolean {
        return false
    }

}