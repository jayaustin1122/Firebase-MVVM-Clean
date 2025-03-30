package com.mvvmexample.firebasecomposemvvmclean.domain.usecase

import com.mvvmexample.firebasecomposemvvmclean.domain.model.Resource
import com.mvvmexample.firebasecomposemvvmclean.domain.model.User
import com.mvvmexample.firebasecomposemvvmclean.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserUseCase @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke(): Flow<Resource<User>> {
        return repository.getUser()
    }
}