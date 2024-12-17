package com.example.testapp.presentation.profile

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavController
import com.example.testapp.R
import com.example.testapp.presentation.templates.section.Section
import com.example.testapp.presentation.templates.section.SectionAppBar
import com.example.testapp.presentation.templates.section.SectionItem
import java.util.Locale

@Composable
fun LanguageScreen(
    onLanguageSelected: (String) -> Unit,
    profileNavController: NavController
) {
    val context = LocalContext.current
    val availableLanguages = listOf(
        "en" to context.getString(R.string.language_english),
        "uk" to context.getString(R.string.language_ukrainian)
    )
    var selectedLanguage by remember { mutableStateOf(Locale.getDefault().language) }

    val isDataChanged by remember {
        derivedStateOf {
            selectedLanguage != Locale.getDefault().language
        }
    }

    Scaffold(
        topBar = {
            SectionAppBar(
                title = stringResource(id = R.string.propage_edit_language),
                isDataChanged = isDataChanged,
                onBackClick = {
                    profileNavController.popBackStack()
                },
                onSaveClick = {
                    onLanguageSelected(selectedLanguage)
                }
            )
        }
    ) { innerPadding ->
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Section(
                title = stringResource(id = R.string.profile_language),
                items = availableLanguages.map { (code, name) ->
                    SectionItem.Radio(
                        title = name,
                        selected = code == selectedLanguage,
                        onClick = {
                            selectedLanguage = code
                            onLanguageSelected(code)
                        }
                    )
                }
            )
        }
    }
}