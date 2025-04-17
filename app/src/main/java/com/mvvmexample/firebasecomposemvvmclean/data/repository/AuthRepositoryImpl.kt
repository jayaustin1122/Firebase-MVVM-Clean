package com.mvvmexample.firebasecomposemvvmclean.data.repository

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import com.mvvmexample.firebasecomposemvvmclean.data.source.FirebaseAuthDataSource
import com.mvvmexample.firebasecomposemvvmclean.data.source.FirestoreUserDataSource
import com.mvvmexample.firebasecomposemvvmclean.domain.model.Resource
import com.mvvmexample.firebasecomposemvvmclean.domain.model.User
import com.mvvmexample.firebasecomposemvvmclean.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.concurrent.TimeoutException
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

    private suspend fun <T> withTimeout(timeMillis: Long = 10000, block: suspend () -> T): T {
        return kotlinx.coroutines.withTimeoutOrNull(timeMillis) {
            block()
        } ?: throw TimeoutException("Operation timed out after $timeMillis ms")
    }

    override fun signInWithGoogle(credential: AuthCredential): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            val authResult = withTimeout(5000) {
                var finalResult: Resource<FirebaseUser>? = null
                authDataSource.signInWithCredential(credential).collect { result ->
                    if (result !is Resource.Loading) {
                        finalResult = result
                    }
                }
                finalResult ?: Resource.Error<FirebaseUser>("No result received")
            }

            when (authResult) {
                is Resource.Success -> {
                    val firebaseUser = authResult.data
                    val userId = firebaseUser.uid

                    val userResult = withTimeout(5000) {
                        var user: Resource<User>? = null
                        userDataSource.getUser(userId).collect { result ->
                            if (result !is Resource.Loading) {
                                user = result
                            }
                        }
                        user ?: Resource.Error<User>("Failed to retrieve user data")
                    }

                    when (userResult) {
                        is Resource.Success -> {
                            emit(userResult)
                        }

                        is Resource.Error -> {
                            // User doesn't exist in Firestore, create a new one
                            val newUser = User(
                                id = userId,
                                email = firebaseUser.email ?: "",
                                displayName = firebaseUser.displayName ?: "",
                                photoUrl = firebaseUser.photoUrl?.toString() ?: ""
                            )

                            withTimeout(5000) {
                                var createResult: Resource<User>? = null
                                userDataSource.createUser(newUser).collect { result ->
                                    if (result !is Resource.Loading) {
                                        createResult = result
                                    }
                                }
                                createResult ?: Resource.Error<User>("Failed to create user")
                            }
                        }

                        is Resource.Loading -> {
                            emit(Resource.Error("Unexpected loading state"))
                        }
                    }
                }

                is Resource.Error -> {
                    emit(Resource.Error(authResult.message))
                }

                is Resource.Loading -> {
                    emit(Resource.Error("Unexpected loading state"))
                }
            }
        } catch (e: TimeoutException) {
            emit(Resource.Error("Operation timed out. Please check your network connection and try again."))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An unknown error occurred"))
        }
    }
}