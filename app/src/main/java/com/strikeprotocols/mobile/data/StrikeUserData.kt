package com.strikeprotocols.mobile.data

import com.strikeprotocols.mobile.data.models.VerifyUser

interface StrikeUserData {
    fun getStrikeUser(): VerifyUser?
    fun setStrikeUser(verifyUser: VerifyUser?)
    fun isNotNull(): Boolean
}


object StrikeUserDataImpl : StrikeUserData {
    private var strikeUser: VerifyUser? = null

    override fun getStrikeUser(): VerifyUser? = strikeUser

    override fun setStrikeUser(verifyUser: VerifyUser?) {
        strikeUser = verifyUser
    }

    override fun isNotNull(): Boolean = strikeUser != null
}