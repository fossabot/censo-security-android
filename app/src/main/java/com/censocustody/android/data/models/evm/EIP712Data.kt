package com.censocustody.android.data.models.evm
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException

class EIP712Data(val json: String) {
    private val gson = Gson()
    private val jsonObject = try { gson.fromJson(json, JsonObject::class.java) } catch (e: JsonSyntaxException) { null }
    val isValidEIP712 = jsonObject != null

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
        return if (jsonObject == null) {
            emptyList()
        } else {
            val types = jsonObject.getAsJsonObject("types")
            val domain = jsonObject.getAsJsonObject("domain")

            val domainType = types.getAsJsonArray("EIP712Domain")

            domainType.map { field ->
                val name = field.asJsonObject.get("name").asString
                val type = field.asJsonObject.get("type").asString
                val value = domain.get(name)

                Entry(name, type, value)
            }
        }
    }

    fun hasType(type: String): Boolean {
        return jsonObject?.getAsJsonObject("types")?.has(type) ?: false
    }

    fun getEntriesForType(data: JsonObject, typeName: String): List<Entry> {
        return if (jsonObject == null) {
            emptyList()
        } else {
            val types = jsonObject.getAsJsonObject("types")
            types.getAsJsonArray(typeName).map { field ->
                val name = field.asJsonObject.get("name").asString
                val type = field.asJsonObject.get("type").asString
                val value = data.get(name)

                Entry(name, type, value)
            }
        }
    }

    fun getMessageEntries(): List<Entry> {
        return if (jsonObject == null) {
            emptyList()
        } else {
            val message = jsonObject.getAsJsonObject("message")
            val messageType = jsonObject.get("primaryType").asString
            getEntriesForType(message, messageType)
        }
    }
}
