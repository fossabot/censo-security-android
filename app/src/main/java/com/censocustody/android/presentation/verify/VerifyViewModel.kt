package com.censocustody.android.presentation.verify

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.censocustody.android.common.Resource
import com.censocustody.android.common.censoLog
import com.censocustody.android.common.wrapper.BaseWrapper
import com.censocustody.android.data.cryptography.CryptographyManagerImpl
import java.util.UUID

@HiltViewModel
class VerifyViewModel @Inject constructor(
) : ViewModel() {

    var state by mutableStateOf(VerifyState())
        private set

    val cryptographyManagerImpl = CryptographyManagerImpl()

    val keyName = UUID.randomUUID().toString()

    val mainKey = cryptographyManagerImpl.getOrCreateKey(keyName)

    val data = BaseWrapper.decode("3yQ")

    fun signData() {
        val signedData = cryptographyManagerImpl.signData(
            keyName = keyName,
            dataToSign = data
        )

        censoLog(message = "Signed data: ${BaseWrapper.encode(signedData)}")

        state = state.copy(
            signedData = signedData,
            verified = Resource.Uninitialized
        )
    }

    fun verifyData() {
        state = try {
            val verified = cryptographyManagerImpl.verifySignature(
                keyName = keyName,
                dataSigned = data,
                signatureToCheck = state.signedData
            )

            censoLog(message = "Verified: $verified")

            state.copy(verified = Resource.Success(verified))
        } catch (e: Exception) {
            e.printStackTrace()
            censoLog(message = "Failed to verify signature: ${e.message}")
            state.copy(verified = Resource.Success(false))
        }
    }
}