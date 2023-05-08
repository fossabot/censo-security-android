package com.censocustody.android.data.models.evm
import com.google.gson.Gson
import com.google.gson.JsonObject

class EIP712Data(json: String) {
    private val gson = Gson()
    private val jsonObject = gson.fromJson(json, JsonObject::class.java)

    data class Entry(val name: String, val type: String, val value: Any?)

    fun getDomainName(): String? {
        return getDomainEntries().firstOrNull {
                it.name == "name"
            }?.value?.toString()?.trim('"')
    }

    fun getDomainVerifyingContract(): String? {
        return getDomainEntries().firstOrNull {
            it.name == "verifyingContract"
        }?.value?.toString()?.trim('"')
    }

    fun getDomainEntries(): List<Entry> {
        val types = jsonObject.getAsJsonObject("types")
        val domain = jsonObject.getAsJsonObject("domain")

        val domainType = types.getAsJsonArray("EIP712Domain")

        return domainType.map { field ->
            val name = field.asJsonObject.get("name").asString
            val type = field.asJsonObject.get("type").asString
            val value = domain.get(name)

            Entry(name, type, value)
        }
    }

    fun hasType(type: String): Boolean {
        val types = jsonObject.getAsJsonObject("types")
        return types.has(type)
    }

    fun getEntriesForType(data: JsonObject, typeName: String): List<Entry> {
        val types = jsonObject.getAsJsonObject("types")
        return types.getAsJsonArray(typeName).map { field ->
            val name = field.asJsonObject.get("name").asString
            val type = field.asJsonObject.get("type").asString
            val value = data.get(name)

            Entry(name, type, value)
        }
    }

    fun getMessageEntries(): List<Entry> {
        val message = jsonObject.getAsJsonObject("message")
        val messageType = jsonObject.get("primaryType").asString
        return getEntriesForType(message, messageType)
    }
}
