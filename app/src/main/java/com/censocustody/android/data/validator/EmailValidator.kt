package com.censocustody.android.data.validator

import android.util.Patterns

interface EmailValidator {
    fun validEmail(email: String): Boolean
}

class AndroidEmailValidator : EmailValidator {
    override fun validEmail(email: String) =
        Patterns.EMAIL_ADDRESS.matcher(email).matches()
}