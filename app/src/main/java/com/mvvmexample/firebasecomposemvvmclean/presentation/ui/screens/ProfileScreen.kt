package com.mvvmexample.firebasecomposemvvmclean.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.mvvmexample.firebasecomposemvvmclean.domain.model.Resource
import com.mvvmexample.firebasecomposemvvmclean.domain.model.User
import com.mvvmexample.firebasecomposemvvmclean.presentation.viewmodel.AuthViewModel

@Composable
fun ProfileScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onNavigateToAuth: () -> Unit
) {
    val userState = viewModel.userState.collectAsState()
    val updateUserState = viewModel.updateUserState.collectAsState()

    var displayName by remember { mutableStateOf("") }
    var showEditForm by remember { mutableStateOf(false) }
    var hasAttemptedUpdate by remember { mutableStateOf(false) }
    val isUpdating = hasAttemptedUpdate && updateUserState.value is Resource.Loading

    LaunchedEffect(userState.value) {
        if (userState.value is Resource.Success) {
            displayName = (userState.value as Resource.Success<User>).data.displayName
        } else if (userState.value is Resource.Error) {
            onNavigateToAuth()
        }
    }

    LaunchedEffect(updateUserState.value) {
        if (updateUserState.value !is Resource.Loading && hasAttemptedUpdate) {
            if (updateUserState.value is Resource.Success) {
                showEditForm = false
                viewModel.getUser()
                hasAttemptedUpdate = false
            }
            // Keep hasAttemptedUpdate true to show error if update failed
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (userState.value) {
            is Resource.Loading -> {
                CircularProgressIndicator()
            }
            is Resource.Success -> {
                val user = (userState.value as Resource.Success<User>).data

                Text(
                    text = "Profile",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                if (user.photoUrl.isNotEmpty()) {
                    AsyncImage(
                        model = user.photoUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.displayName.take(1).uppercase(),
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = if (user.displayName.isNotEmpty()) user.displayName else "No Name Set",
                    style = MaterialTheme.typography.headlineMedium
                )

                Text(
                    text = user.email,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(32.dp))

                if (!showEditForm) {
                    Button(onClick = {
                        showEditForm = true
                        hasAttemptedUpdate = false
                        displayName = user.displayName
                    }) {
                        Text("Edit Profile")
                    }
                } else {
                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        label = { Text("Display Name") },
                        enabled = !isUpdating,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                hasAttemptedUpdate = true
                                val updatedUser = user.copy(displayName = displayName)
                                viewModel.updateUser(updatedUser)
                            },
                            enabled = !isUpdating && displayName.isNotBlank()
                        ) {
                            if (isUpdating) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .padding(end = 8.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                            Text("Save")
                        }

                        OutlinedButton(
                            onClick = {
                                showEditForm = false
                                displayName = user.displayName
                                hasAttemptedUpdate = false
                            },
                            enabled = !isUpdating
                        ) {
                            Text("Cancel")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        viewModel.logout()
                        onNavigateToAuth()
                    },
                    enabled = !isUpdating
                ) {
                    Text("Logout")
                }

                if (hasAttemptedUpdate && updateUserState.value is Resource.Error) {
                    Text(
                        text = (updateUserState.value as Resource.Error<User>).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
            is Resource.Error -> {
                Text(
                    text = (userState.value as Resource.Error<User>).message,
                    color = MaterialTheme.colorScheme.error
                )

                Button(onClick = onNavigateToAuth) {
                    Text("Back to Login")
                }
            }
        }
    }
}