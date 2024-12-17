package com.example.testapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.utils.DataStoreUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ThemeViewModel(
    private val dataStoreUtil: DataStoreUtil,
    private val isSystemDarkTheme: Boolean
) : ViewModel() {

    private val _isDarkThemeEnabled = MutableStateFlow(isSystemDarkTheme)
    val isDarkThemeEnabled: StateFlow<Boolean> = _isDarkThemeEnabled

    init {
        viewModelScope.launch {
            loadSavedTheme()
        }
    }

    private fun loadSavedTheme() {
        viewModelScope.launch {
            dataStoreUtil.getTheme(isSystemDarkTheme).collect { isDarkTheme ->
                _isDarkThemeEnabled.value = isDarkTheme
            }
        }
    }

    fun setTheme(isDarkTheme: Boolean) {
        viewModelScope.launch {
            _isDarkThemeEnabled.value = isDarkTheme
            dataStoreUtil.saveTheme(isDarkTheme)
        }
    }
}