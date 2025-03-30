package com.mvvmexample.firebasecomposemvvmclean.domain.usecase

import com.mvvmexample.firebasecomposemvvmclean.domain.repository.AuthRepository
import javax.inject.Inject

class IsUserAuthenticatedUseCase @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke(): Boolean {
        return repository.isUserAuthenticated()
    }
}