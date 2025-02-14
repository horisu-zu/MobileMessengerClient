package com.example.testapp.presentation.viewmodel

import android.content.res.Configuration
import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.utils.DataStoreUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val dataStoreUtil: DataStoreUtil
) : ViewModel() {

    private val _isDarkThemeEnabled = MutableStateFlow(false)
    val isDarkThemeEnabled: StateFlow<Boolean> = _isDarkThemeEnabled

    init {
        val systemTheme = when (Resources.getSystem().configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            Configuration.UI_MODE_NIGHT_NO -> false
            else -> false
        }

        _isDarkThemeEnabled.value = systemTheme

        loadSavedTheme()
    }

    private fun loadSavedTheme() {
        viewModelScope.launch {
            dataStoreUtil.getTheme(_isDarkThemeEnabled.value).collect { isDarkTheme ->
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