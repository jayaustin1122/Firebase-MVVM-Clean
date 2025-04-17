package com.mvvmexample.firebasecomposemvvmclean

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.mvvmexample.firebasecomposemvvmclean.presentation.ui.screens.AppNavigation
import com.mvvmexample.firebasecomposemvvmclean.presentation.ui.theme.FireBaseComposeMvvmCleanTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FireBaseComposeMvvmCleanTheme {
                AppNavigation()
            }
        }
    }
}