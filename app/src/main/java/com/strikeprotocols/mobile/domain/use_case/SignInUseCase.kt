package com.strikeprotocols.mobile.domain.use_case

import com.strikeprotocols.mobile.common.Resource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import kotlin.random.Random

class SignInUseCase @Inject constructor() {

    //todo: replace with legitimate api call
    fun execute(): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        delay(3000)
        emit(Resource.Success(true))
    }

}