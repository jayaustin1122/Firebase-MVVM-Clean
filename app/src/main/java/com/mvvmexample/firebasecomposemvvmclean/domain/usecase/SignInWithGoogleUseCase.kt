package com.mvvmexample.firebasecomposemvvmclean.domain.usecase

import com.google.firebase.auth.AuthCredential
import com.mvvmexample.firebasecomposemvvmclean.domain.model.Resource
import com.mvvmexample.firebasecomposemvvmclean.domain.model.User
import com.mvvmexample.firebasecomposemvvmclean.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SignInWithGoogleUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(credential: AuthCredential): Flow<Resource<User>> {
        return authRepository.signInWithGoogle(credential)
    }
}
