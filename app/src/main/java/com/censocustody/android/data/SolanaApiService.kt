package com.censocustody.android.data

import com.google.gson.*
import com.censocustody.android.BuildConfig
import com.censocustody.android.common.BaseWrapper
import com.censocustody.android.data.models.MultipleAccountsResponse
import com.censocustody.android.data.models.Nonce
import com.censocustody.android.presentation.durable_nonce.DurableNonceViewModel
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import retrofit2.Response as RetrofitResponse
import java.lang.reflect.Type


interface SolanaApiService {

    companion object {

        fun create(): SolanaApiService {

            val client = OkHttpClient.Builder()
                .cache(null)

            if (BuildConfig.DEBUG) {
                val logger =
                    HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
                client.addInterceptor(logger)
            }

            val customGson = GsonBuilder()
                .registerTypeAdapter(DurableNonceViewModel.RPCParam::class.java, RPCSerializer())
                .registerTypeAdapter(MultipleAccountsResponse::class.java, MultipleAccountsDeserializer())
                .create()

            return Retrofit.Builder()
                .baseUrl(BuildConfig.SOLANA_URL)
                .client(client.build())
                .addConverterFactory(GsonConverterFactory.create(customGson))
                .build()
                .create(SolanaApiService::class.java)
        }
    }

    @POST("/")
    suspend fun multipleAccounts(@Body multipleAccountsBody: DurableNonceViewModel.MultipleAccountsBody) : RetrofitResponse<MultipleAccountsResponse>
}

class RPCSerializer : JsonSerializer<DurableNonceViewModel.RPCParam> {
    override fun serialize(
        src: DurableNonceViewModel.RPCParam?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        if (src == null) {
            return JsonArray()
        }

        val jsonArray = JsonArray()
        val keysArray = JsonArray()
        for (key in src.keys) {
            keysArray.add(key)
        }

        jsonArray.add(keysArray)

        val jsonObject = JsonObject()
        jsonObject.addProperty("commitment", src.commitment)
        jsonObject.addProperty("encoding", src.encoding)
        jsonArray.add(jsonObject)

        return jsonArray
    }
}

class MultipleAccountsDeserializer : JsonDeserializer<MultipleAccountsResponse> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): MultipleAccountsResponse {
        return parseData(json)
    }

    fun parseData(json: JsonElement?): MultipleAccountsResponse {
        val nonces = mutableListOf<Nonce>()
        var slot = NO_SLOT

        if (json !is JsonObject) {
            return MultipleAccountsResponse(nonces = nonces, slot = slot)
        }

        val result = json.asJsonObject?.get(RESULT_JSON_KEY)
        if (result !is JsonObject) {
            return MultipleAccountsResponse(nonces = nonces, slot = slot)
        }

        val context = result.get(CONTEXT_JSON_KEY)

        if (context is JsonObject) {
            slot = context.getAsJsonPrimitive(SLOT_JSON_KEY).asInt
        }

        val values = result.get(VALUE_JSON_KEY)
        if (values !is JsonArray) {
            return MultipleAccountsResponse(nonces = nonces, slot = slot)
        }

        for (value in values) {
            if (value !is JsonObject) {
                continue
            }

            val data = value.asJsonObject?.get(DATA_JSON_KEY)
            if (data !is JsonArray) {
                continue
            }


            val dataAsString =
                if (data.asJsonArray.size() > 0) {
                    val nonceText = data.asJsonArray.get(0)
                    if (nonceText.isJsonPrimitive) {
                        nonceText.asString
                    } else {
                        continue
                    }
                } else {
                    continue
                }

            val dataByteArray = BaseWrapper.decodeFromBase64(dataAsString)
            if (dataByteArray.size < END_NONCE_INDEX + 1) {
                continue
            }

            val nonceByteArray =
                dataByteArray.slice(START_NONCE_INDEX.until(END_NONCE_INDEX)).toByteArray()
            val base58Data = BaseWrapper.encode(nonceByteArray)
            nonces.add(Nonce(base58Data))
        }

        return MultipleAccountsResponse(nonces = nonces, slot = slot)
    }

    companion object {
        const val RESULT_JSON_KEY = "result"
        const val VALUE_JSON_KEY = "value"
        const val DATA_JSON_KEY = "data"
        const val CONTEXT_JSON_KEY = "context"
        const val SLOT_JSON_KEY = "slot"
        const val START_NONCE_INDEX = 40
        const val END_NONCE_INDEX = 72
        const val NO_SLOT = -1
    }
}