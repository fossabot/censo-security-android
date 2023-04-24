package com.censocustody.android.data.storage

import com.censocustody.android.data.models.VerifyUser
import com.censocustody.android.data.repository.UserRepository

interface CensoUserData {
    fun getCensoUser(): VerifyUser?
    fun setEmail(userEmail: String)
    fun getEmail(): String
    fun setCensoUser(verifyUser: VerifyUser?)
    fun isNotNull(): Boolean
}


class CensoUserDataImpl(val userRepository: UserRepository) : CensoUserData {
    private var censoUser: VerifyUser? = null
    private var email: String = ""

    override fun getCensoUser(): VerifyUser? = censoUser
    override fun setEmail(userEmail: String) {
        email = userEmail
    }

    override fun getEmail(): String {
        if (email.isEmpty()) {
            email = userRepository.retrieveCachedUserEmail()
        }
        return email
    }

    override fun setCensoUser(verifyUser: VerifyUser?) {
        censoUser = verifyUser
    }

    override fun isNotNull(): Boolean = censoUser != null
}