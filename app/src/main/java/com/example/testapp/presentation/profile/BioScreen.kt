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
import androidx.compose.ui.res.stringResource
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavController
import com.example.testapp.R
import com.example.testapp.domain.dto.user.UserResponse
import com.example.testapp.presentation.templates.section.Section
import com.example.testapp.presentation.templates.section.SectionAppBar
import com.example.testapp.presentation.templates.section.SectionItem
import com.example.testapp.presentation.viewmodel.user.UserViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun BioScreen(
    userData: UserResponse?,
    profileNavController: NavController,
    userViewModel: UserViewModel,
    scope: CoroutineScope
) {
    val defaultFirstName = stringResource(id = R.string.auth_first_name)
    val defaultLastName = stringResource(id = R.string.auth_last_name)

    var firstName by remember { mutableStateOf(userData?.firstName ?: defaultFirstName) }
    var lastName by remember { mutableStateOf(userData?.lastName ?: defaultLastName) }
    var bio by remember { mutableStateOf(userData?.description ?: "") }

    val isDataChanged by remember {
        derivedStateOf {
            firstName != (userData?.firstName ?: defaultFirstName) ||
            lastName != (userData?.lastName ?: defaultLastName) ||
            bio != (userData?.description ?: "")
        }
    }

    Scaffold(
        topBar = {
            SectionAppBar(
                title = stringResource(id = R.string.profile_empty_bio_title),
                isDataChanged = isDataChanged,
                onBackClick = {
                    profileNavController.popBackStack()
                },
                onSaveClick = {
                    val updatedUser = userData?.copy(
                        firstName = if (firstName != (userData.firstName)) firstName else userData.firstName,
                        lastName = if (lastName != (userData.lastName ?: defaultLastName)) lastName else userData.lastName,
                        description = if (bio != userData.description) bio else userData.description
                    )
                    updatedUser?.let {
                        scope.launch {
                            userViewModel.updateUser(it)
                            profileNavController.popBackStack()
                        }
                    }
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
                title = stringResource(id = R.string.propage_edit_bio),
                items = listOf(
                    SectionItem.Input(
                        label = stringResource(id = R.string.auth_first_name),
                        value = firstName,
                        limit = 30,
                        placeholder = stringResource(id = R.string.bio_first_name),
                        onValueChange = { newValue ->
                            firstName = newValue
                        }
                    ),
                    SectionItem.Input(
                        label = stringResource(id = R.string.bio_last_name),
                        value = lastName,
                        limit = 30,
                        placeholder = stringResource(id = R.string.bio_last_name),
                        onValueChange = { newValue ->
                            lastName = newValue
                        }
                    ),
                    SectionItem.Input(
                        label = stringResource(id = R.string.profile_empty_bio_title),
                        value = bio,
                        limit = 50,
                        placeholder = stringResource(id = R.string.profile_empty_bio_subtitle),
                        onValueChange = { newValue ->
                            bio = newValue
                        }
                    )
                )
            )
        }
    }
}