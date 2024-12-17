package com.example.testapp.presentation.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.testapp.utils.DataStoreUtil

@Composable
fun TokenFragment(
    dataStoreUtil: DataStoreUtil
) {
    val accessToken by dataStoreUtil.getAccessToken().collectAsState(initial = null)
    val refreshToken by dataStoreUtil.getRefreshToken().collectAsState(initial = null)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TokenComponent(
            title = "Access Token",
            token = accessToken
        )
        TokenComponent(
            title = "Refresh Token",
            token = refreshToken
        )
    }
}

@Composable
fun TokenComponent(
    title: String,
    token: String?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = "$title:")
            Text(text = token ?: "How did you get it?")
        }
    }
}