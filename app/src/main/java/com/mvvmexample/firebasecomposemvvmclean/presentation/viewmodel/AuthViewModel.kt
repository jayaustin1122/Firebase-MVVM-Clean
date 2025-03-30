package com.mvvmexample.firebasecomposemvvmclean.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mvvmexample.firebasecomposemvvmclean.domain.model.Resource
import com.mvvmexample.firebasecomposemvvmclean.domain.model.User
import com.mvvmexample.firebasecomposemvvmclean.domain.usecase.GetUserUseCase
import com.mvvmexample.firebasecomposemvvmclean.domain.usecase.IsUserAuthenticatedUseCase
import com.mvvmexample.firebasecomposemvvmclean.domain.usecase.LoginUseCase
import com.mvvmexample.firebasecomposemvvmclean.domain.usecase.LogoutUseCase
import com.mvvmexample.firebasecomposemvvmclean.domain.usecase.SignUpUseCase
import com.mvvmexample.firebasecomposemvvmclean.domain.usecase.UpdateUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase,
    private val loginUseCase: LoginUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val updateUserUseCase: UpdateUserUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val isUserAuthenticatedUseCase: IsUserAuthenticatedUseCase
) : ViewModel() {

    private val _signUpState = MutableStateFlow<Resource<User>>(Resource.Loading())
    val signUpState: StateFlow<Resource<User>> = _signUpState

    private val _loginState = MutableStateFlow<Resource<User>>(Resource.Loading())
    val loginState: StateFlow<Resource<User>> = _loginState

    private val _userState = MutableStateFlow<Resource<User>>(Resource.Loading())
    val userState: StateFlow<Resource<User>> = _userState

    private val _updateUserState = MutableStateFlow<Resource<User>>(Resource.Loading())
    val updateUserState: StateFlow<Resource<User>> = _updateUserState

    init {
        getUser()
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            signUpUseCase(email, password).collect { result ->
                _signUpState.value = result
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            loginUseCase(email, password).collect { result ->
                _loginState.value = result
            }
        }
    }

    fun getUser() {
        viewModelScope.launch {
            getUserUseCase().collect { result ->
                _userState.value = result
            }
        }
    }

    fun updateUser(user: User) {
        viewModelScope.launch {
            updateUserUseCase(user).collect { result ->
                _updateUserState.value = result
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            _userState.value = Resource.Error("No user logged in")
        }
    }

    fun isUserAuthenticated(): Boolean {
        return isUserAuthenticatedUseCase()
    }
}