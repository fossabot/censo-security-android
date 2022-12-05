package com.censocustody.mobile.data

import com.censocustody.mobile.data.models.VerifyUser

interface StrikeUserData {
    fun getStrikeUser(): VerifyUser?
    fun setEmail(userEmail: String)
    fun getEmail(): String
    fun setStrikeUser(verifyUser: VerifyUser?)
    fun isNotNull(): Boolean
}


class StrikeUserDataImpl(val userRepository: UserRepository) : StrikeUserData {
    private var strikeUser: VerifyUser? = null
    private var email: String = ""

    override fun getStrikeUser(): VerifyUser? = strikeUser
    override fun setEmail(userEmail: String) {
        email = userEmail
    }

    override fun getEmail(): String {
        if (email.isEmpty()) {
            email = userRepository.retrieveCachedUserEmail()
        }
        return email
    }

    override fun setStrikeUser(verifyUser: VerifyUser?) {
        strikeUser = verifyUser
    }

    override fun isNotNull(): Boolean = strikeUser != null
}