package com.mvvmexample.firebasecomposemvvmclean.domain.repository

import com.mvvmexample.firebasecomposemvvmclean.domain.model.Resource
import com.mvvmexample.firebasecomposemvvmclean.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun signUp(email: String, password: String): Flow<Resource<User>>
    fun login(email: String, password: String): Flow<Resource<User>>
    suspend fun logout(): Resource<Unit>
    fun getUser(): Flow<Resource<User>>
    fun updateUser(user: User): Flow<Resource<User>>
    fun isUserAuthenticated(): Boolean
}