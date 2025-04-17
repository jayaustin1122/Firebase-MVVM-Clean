package com.mvvmexample.firebasecomposemvvmclean.presentation.ui.screens.auth

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.auth.GoogleAuthProvider
import com.mvvmexample.firebasecomposemvvmclean.R
import com.mvvmexample.firebasecomposemvvmclean.domain.model.Resource
import com.mvvmexample.firebasecomposemvvmclean.domain.model.User
import com.mvvmexample.firebasecomposemvvmclean.presentation.ui.screens.auth.components.GoogleSignInButton
import com.mvvmexample.firebasecomposemvvmclean.presentation.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun AuthScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onNavigateToProfile: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLogin by remember { mutableStateOf(true) }
    var hasAttemptedAuth by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val loginState = viewModel.loginState.collectAsState()
    val signUpState = viewModel.signUpState.collectAsState()
    val signInState = viewModel.signInState.collectAsState()

    val isLoading = hasAttemptedAuth && (
            loginState.value is Resource.Loading ||
                    signUpState.value is Resource.Loading ||
                    signInState.value is Resource.Loading
            )

    var isLoadingTooLong by remember(isLoading) {
        mutableStateOf(false)
    }

    LaunchedEffect(isLoading) {
        if (isLoading) {
            delay(5000)
            if (isLoading) {
                isLoadingTooLong = true
            }
        } else {
            isLoadingTooLong = false
        }
    }

    if (isLoadingTooLong) {
        Text(
            "Sign-in is taking longer than expected...",
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(top = 8.dp)
        )
    }

    // Initialize the One Tap client with Identity API
    val oneTapClient = remember {
        Identity.getSignInClient(context)
    }

    // Create sign-in request
    val signInRequest = remember {
        BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(context.getString(R.string.web_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .build()
    }

    LaunchedEffect(Unit) {
        viewModel.setOneTapClient(oneTapClient)
    }

    // Launcher for handling the result of One Tap sign-in
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val credential = Identity.getSignInClient(context)
                    .getSignInCredentialFromIntent(result.data)

                // Get the ID token from the credential
                val idToken = credential.googleIdToken

                // Use the ID token to sign in with Firebase
                if (idToken != null) {
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    viewModel.signInWithGoogle(firebaseCredential)
                    hasAttemptedAuth = true
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    LaunchedEffect(loginState.value, signUpState.value, signInState.value) {
        when {
            loginState.value is Resource.Success -> onNavigateToProfile()
            signUpState.value is Resource.Success -> onNavigateToProfile()
            signInState.value is Resource.Success -> onNavigateToProfile()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isLogin) "Login" else "Sign Up",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            enabled = !isLoading,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        Button(
            onClick = {
                hasAttemptedAuth = true
                if (isLogin) {
                    viewModel.login(email, password)
                } else {
                    viewModel.signUp(email, password)
                }
            },
            enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            if (isLoading && (loginState.value is Resource.Loading || signUpState.value is Resource.Loading)) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(20.dp),
                    strokeWidth = 2.dp
                )
            }
            Text(text = if (isLogin) "Login" else "Sign Up")
        }

        TextButton(
            onClick = { isLogin = !isLogin },
            enabled = !isLoading
        ) {
            Text(
                text = if (isLogin) "Need an account? Sign Up" else "Already have an account? Login"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        Text(
            text = "OR",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        GoogleSignInButton(
            onClick = {
                hasAttemptedAuth = true
                oneTapClient.signOut().addOnCompleteListener {
                    oneTapClient.beginSignIn(signInRequest)
                        .addOnSuccessListener { result ->
                            try {
                                val intentSenderRequest = IntentSenderRequest.Builder(
                                    result.pendingIntent.intentSender
                                ).build()
                                launcher.launch(intentSenderRequest)
                            } catch (e: Exception) {
                                // Handle the exception
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("Error",e.message.toString())

                        }
                }
            },
            enabled = !isLoading,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        val errorMessage = if (hasAttemptedAuth) {
            when {
                loginState.value is Resource.Error -> (loginState.value as Resource.Error<User>).message
                signUpState.value is Resource.Error -> (signUpState.value as Resource.Error<User>).message
                signInState.value is Resource.Error -> (signInState.value as Resource.Error<User>).message
                else -> null
            }
        } else null

        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}