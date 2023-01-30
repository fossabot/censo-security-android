package com.censocustody.android.presentation.key_recovery

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.censocustody.android.common.BaseWrapper
import com.censocustody.android.common.censoLog
import com.censocustody.android.data.CryptographyManagerImpl
import com.censocustody.android.data.KeyRepository
import com.censocustody.android.data.UserRepository
import com.censocustody.android.data.models.CipherRepository
import com.google.crypto.tink.aead.subtle.AesGcmSiv
import com.google.crypto.tink.subtle.Hkdf
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PublicKey
import java.security.spec.ECGenParameterSpec
import javax.inject.Inject


@HiltViewModel
class KeyRecoveryViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val keyRepository: KeyRepository,
    private val cipherRepository: CipherRepository
) : ViewModel() {

    var state by mutableStateOf(KeyRecoveryState())
        private set

    //region VM SETUP
    fun onStart() {
        viewModelScope.launch {
            testOutKeyAgreementSetup()
        }
    }

    private suspend fun testOutKeyAgreementSetup() {
        val userEmail = userRepository.retrieveUserEmail()
        val deviceKeyId = userRepository.retrieveUserDeviceId(userEmail)

        //Key Agreement
        val keyAgreement = cipherRepository.getKeyAgreementForKeyRecovery(deviceKeyId)

        //Device Public Key
        val devicePublicKey = userRepository.retrieveUserDevicePublicKey(userEmail)

        //Ephemeral Public Key and creating shared secret
        val ephemeralKeyPair = generateEphemeralKey()
        keyAgreement!!.doPhase(ephemeralKeyPair.public, true)
        val sharedSecret = keyAgreement.generateSecret()

        val salt = byteArrayOf()
        val info = ByteArrayOutputStream()
        info.write("ECDH secp256r1 AES-256-GCM-SIV\u0000".toByteArray(StandardCharsets.UTF_8))
        info.write(BaseWrapper.decode(devicePublicKey))
        info.write(ephemeralKeyPair.public.encoded)

        // This example uses the Tink library and the HKDF key derivation function.
        val key = AesGcmSiv(
            Hkdf.computeHkdf(
                "HMACSHA256", sharedSecret, salt, info.toByteArray(), 32
            )
        )

        val encryptedData = key.encrypt("this data here".toByteArray(StandardCharsets.UTF_8), byteArrayOf())

        val decryptedData = key.decrypt(encryptedData, byteArrayOf())

        censoLog(message = "Decrypted data: ${String(decryptedData, charset = Charsets.UTF_8)}")
    }

    fun generateEphemeralKey(): KeyPair {
        val kg = KeyPairGenerator.getInstance("EC")
        val kpgparams = ECGenParameterSpec("secp256r1")
        kg.initialize(kpgparams)

        val kp = kg.generateKeyPair()
        return kp
    }
}