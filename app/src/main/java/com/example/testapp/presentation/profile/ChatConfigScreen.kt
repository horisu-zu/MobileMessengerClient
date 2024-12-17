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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavController
import com.example.testapp.R
import com.example.testapp.domain.dto.user.UserResponse
import com.example.testapp.presentation.templates.section.ColorRow
import com.example.testapp.presentation.templates.section.SectionAppBar
import com.example.testapp.presentation.viewmodel.user.UserViewModel
import com.example.testapp.utils.UserColorGenerator
import com.example.testapp.utils.UserColorGenerator.toColor
import com.example.testapp.utils.UserColorGenerator.toHexString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun ChatConfigScreen(
    userData: UserResponse?,
    profileNavController: NavController,
    userViewModel: UserViewModel,
    scope: CoroutineScope
) {
    var userColor by remember { mutableStateOf(userData?.userColor?.toColor() ?: Color.White) }

    val isDataChanged by remember {
        derivedStateOf {
            userColor != (userData?.userColor?.toColor() ?: Color.White)
        }
    }

    Scaffold(
        topBar = {
            SectionAppBar(
                title = stringResource(id = R.string.propage_edit_chat),
                isDataChanged = isDataChanged,
                onBackClick = {
                    profileNavController.popBackStack()
                },
                onSaveClick = {
                    scope.launch {
                        val updatedUser = userData?.copy(userColor = userColor.toHexString())
                        if (updatedUser != null) {
                            userViewModel.updateUser(updatedUser)
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
            val (colorRow, description) = createRefs()

            ColorRow(
                colors = UserColorGenerator.getColors(),
                currentColor = userColor,
                modifier = Modifier.constrainAs(colorRow) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                },
                onColorSelected = { selectedColor ->
                    userColor = selectedColor
                },
            )
        }
    }
}