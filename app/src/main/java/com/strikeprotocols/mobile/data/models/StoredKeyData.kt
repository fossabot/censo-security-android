package com.strikeprotocols.mobile.data.models

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

data class StoredKeyData(
    val initVector: String,
    val encryptedKeysData: String
) {
    fun toJson(): String {
        return Gson().toJson(this, StoredKeyData::class.java)
    }

    companion object {
        fun mapToJson(keyMap: HashMap<String, String>): String {
            return Gson().toJson(keyMap)
        }

        fun mapFromJson(json: String): HashMap<String, String> {
            val typeOfHashMap: Type = object : TypeToken<HashMap<String?, String?>?>() {}.type
            return Gson().fromJson(json, typeOfHashMap)
        }

        fun fromJson(json: String): StoredKeyData {
            return Gson().fromJson(json, StoredKeyData::class.java)
        }

        const val SOLANA_KEY = "solana_key"
        const val ROOT_SEED = "root_seed"
    }
}