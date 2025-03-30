package com.mvvmexample.firebasecomposemvvmclean.di

import com.mvvmexample.firebasecomposemvvmclean.data.repository.AuthRepositoryImpl
import com.mvvmexample.firebasecomposemvvmclean.data.source.FirebaseAuthDataSource
import com.mvvmexample.firebasecomposemvvmclean.data.source.FirestoreUserDataSource
import com.mvvmexample.firebasecomposemvvmclean.domain.repository.AuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        authDataSource: FirebaseAuthDataSource,
        userDataSource: FirestoreUserDataSource
    ): AuthRepository {
        return AuthRepositoryImpl(authDataSource, userDataSource)
    }
}