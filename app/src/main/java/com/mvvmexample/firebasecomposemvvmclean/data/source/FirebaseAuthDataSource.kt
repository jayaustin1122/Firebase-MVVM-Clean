package com.mvvmexample.firebasecomposemvvmclean.data.source

import android.util.Log
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.mvvmexample.firebasecomposemvvmclean.domain.model.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthDataSource @Inject constructor() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun signUp(email: String, password: String): Flow<Resource<AuthResult>> = flow {
        emit(Resource.Loading())
        try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            emit(Resource.Success(result))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Authentication failed"))
        }
    }

    fun login(email: String, password: String): Flow<Resource<AuthResult>> = flow {
        emit(Resource.Loading())
        try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            emit(Resource.Success(result))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Login failed"))
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun isUserAuthenticated(): Boolean {
        return auth.currentUser != null
    }

    fun signInWithCredential(credential: AuthCredential): Flow<Resource<FirebaseUser>> = flow {
        emit(Resource.Loading())
        try {
            val result = auth.signInWithCredential(credential).await()
            result.user?.let {
                emit(Resource.Success(it))
            } ?: emit(Resource.Error("Sign-in failed: No user returned"))
        } catch (e: Exception) {
            Log.e("FirebaseAuth", "Sign-in error", e)
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }
}