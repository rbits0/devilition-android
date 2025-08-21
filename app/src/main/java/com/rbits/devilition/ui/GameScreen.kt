package com.rbits.devilition.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rbits.devilition.ui.theme.DevilitionTheme

@Composable
fun GameScreen(
    gameViewModel: GameViewModel = viewModel(),
) {
    val gameUiState by gameViewModel.uiState.collectAsState()
}


@Preview(showBackground = true)
@Composable
fun GameScreenPreview() {
    DevilitionTheme {
        GameScreen()
    }
}