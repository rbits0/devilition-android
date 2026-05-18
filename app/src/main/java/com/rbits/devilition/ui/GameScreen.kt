package com.rbits.devilition.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rbits.devilition.data.GRID_HEIGHT
import com.rbits.devilition.data.GRID_WIDTH
import com.rbits.devilition.ui.theme.DevilitionTheme

@Composable
fun GameScreen(
    modifier: Modifier = Modifier,
    gameViewModel: GameViewModel = viewModel(),
) {
    val gameUiState by gameViewModel.uiState.collectAsState()

//    var pixels: Float
//    with(LocalDensity.current) {
//        pixels = 1.dp.toPx()
//    }

    // TODO: Remove
    val gridState = Array(GRID_HEIGHT) { Array<GridItem?>(GRID_WIDTH) {
        GridItem.Piece(type = PieceType.SNAKE, rotation = 0)
    } }

    GameGrid(gridState, modifier = modifier)
}


@Preview(showBackground = true)
@Composable
fun GameScreenPreview() {
    DevilitionTheme {
        GameScreen()
    }
}