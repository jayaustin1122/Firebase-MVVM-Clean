package com.mvvmexample.firebasecomposemvvmclean.domain.usecase

import com.mvvmexample.firebasecomposemvvmclean.domain.model.Resource
import com.mvvmexample.firebasecomposemvvmclean.domain.model.User
import com.mvvmexample.firebasecomposemvvmclean.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SignUpUseCase @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke(email: String, password: String): Flow<Resource<User>> {
        return repository.signUp(email, password)
    }
}
