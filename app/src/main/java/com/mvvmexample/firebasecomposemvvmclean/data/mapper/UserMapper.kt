package com.mvvmexample.firebasecomposemvvmclean.data.mapper

import com.google.firebase.auth.FirebaseUser
import com.mvvmexample.firebasecomposemvvmclean.domain.model.User

fun FirebaseUser.toUser(): User {
    return User(
        id = uid,
        email = email ?: "",
        displayName = displayName ?: "",
        photoUrl = photoUrl.toString()
    )
}