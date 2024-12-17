package com.example.testapp.presentation.profile

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
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
fun UsernameScreen(
    userData: UserResponse?,
    profileNavController: NavController,
    userViewModel: UserViewModel,
    scope: CoroutineScope
) {
    val defaultNickname = userData?.nickname ?: "Default Username"

    var nickname by remember { mutableStateOf(userData?.nickname ?: defaultNickname) }
    var updateError by remember { mutableStateOf<String?>(null) }

    val isDataChanged by remember {
        derivedStateOf {
            nickname != (userData?.nickname ?: defaultNickname)
        }
    }

    Scaffold(
        topBar = {
            SectionAppBar(
                title = stringResource(id = R.string.propage_edit_username),
                isDataChanged = isDataChanged,
                onBackClick = {
                    profileNavController.popBackStack()
                },
                onSaveClick = {
                    scope.launch {
                        userData?.let {
                            val updatedUser = it.copy(nickname = nickname)
                            userViewModel.updateNickname(updatedUser).collect { result ->
                                result.onSuccess {
                                    profileNavController.popBackStack()
                                }.onFailure { error ->
                                    updateError = error.message
                                }
                            }
                        } ?: Log.e("Username Screen", "User data is null")
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
            val (description, section) = createRefs()

            Section(
                modifier = Modifier.constrainAs(section) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
                items = listOf(
                    SectionItem.Input(
                        label = stringResource(id = R.string.propage_edit_username),
                        value = nickname,
                        onValueChange = { newValue ->
                            nickname = newValue
                        },
                        placeholder = stringResource(id = R.string.propage_edit_username),
                        inputFilter = { it.replace(" ", "") }
                    )
                )
            )

            Card(
                modifier = Modifier
                    .constrainAs(description) {
                        top.linkTo(section.bottom, margin = 16.dp)
                        start.linkTo(parent.start, margin = 16.dp)
                        end.linkTo(parent.end, margin = 16.dp)
                        width = Dimension.fillToConstraints
                    },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(id = R.string.username_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}