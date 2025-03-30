package com.mvvmexample.firebasecomposemvvmclean.data.repository

import com.mvvmexample.firebasecomposemvvmclean.data.source.FirebaseAuthDataSource
import com.mvvmexample.firebasecomposemvvmclean.data.source.FirestoreUserDataSource
import com.mvvmexample.firebasecomposemvvmclean.domain.model.Resource
import com.mvvmexample.firebasecomposemvvmclean.domain.model.User
import com.mvvmexample.firebasecomposemvvmclean.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: FirebaseAuthDataSource,
    private val userDataSource: FirestoreUserDataSource
) : AuthRepository {

    override fun signUp(email: String, password: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        authDataSource.signUp(email, password).collect { result ->
            when (result) {
                is Resource.Success -> {
                    val userId = result.data.user?.uid ?: ""
                    val user = User(id = userId, email = email)
                    userDataSource.createUser(user).collect { userResource ->
                        emit(userResource)
                    }
                }
                is Resource.Loading -> emit(Resource.Loading())
                is Resource.Error -> emit(Resource.Error(result.message))
            }
        }
    }

    override fun login(email: String, password: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        authDataSource.login(email, password).collect { result ->
            when (result) {
                is Resource.Success -> {
                    val userId = result.data.user?.uid
                    if (userId != null) {
                        userDataSource.getUser(userId).collect { userResource ->
                            emit(userResource)
                        }
                    } else {
                        emit(Resource.Error("Authentication failed"))
                    }
                }
                is Resource.Loading -> emit(Resource.Loading())
                is Resource.Error -> emit(Resource.Error(result.message))
            }
        }
    }

    override suspend fun logout(): Resource<Unit> {
        return try {
            authDataSource.logout()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Logout failed")
        }
    }

    override fun getUser(): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        val userId = authDataSource.getCurrentUserId()
        if (userId != null) {
            userDataSource.getUser(userId).collect { userResource ->
                emit(userResource)
            }
        } else {
            emit(Resource.Error("No user logged in"))
        }
    }

    override fun updateUser(user: User): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        userDataSource.updateUser(user).collect { userResource ->
            emit(userResource)
        }
    }

    override fun isUserAuthenticated(): Boolean {
        return authDataSource.isUserAuthenticated()
    }
}