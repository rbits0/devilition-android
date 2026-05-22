package com.rbits.devilition.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mohamedrejeb.compose.dnd.DragAndDropContainer
import com.mohamedrejeb.compose.dnd.rememberDragAndDropState
import com.rbits.devilition.data.GRID_HEIGHT
import com.rbits.devilition.data.GRID_WIDTH
import com.rbits.devilition.ui.theme.DevilitionTheme

const val GRID_SPACING_DP = 2

@Composable
fun GameScreen(
    modifier: Modifier = Modifier,
    gameViewModel: GameViewModel = viewModel(),
) {
    val gameUiState by gameViewModel.uiState.collectAsState()
    val dragAndDropState = rememberDragAndDropState<GridItem.Piece>()

    gameViewModel.roundStart()


    DragAndDropContainer(
        state = dragAndDropState,
        modifier = modifier.fillMaxSize()
            .padding(4.dp),
    ) {
        BoxWithConstraints {
            // Calculate the size of GridCell
            // We're not using this to set the size of the cells in the grid manually, because it
            // doesn't round to an int nicely. Instead we're letting the grid size it automatically
            // and using cellSize for Hand
            val itemUsableWidth = maxWidth - (GRID_SPACING_DP * (GRID_WIDTH - 1)).dp
            val itemUsableHeight = maxHeight - (GRID_SPACING_DP * (GRID_HEIGHT - 1)).dp
            val isGridMaxWidth = (itemUsableWidth / itemUsableHeight) < (GRID_WIDTH / GRID_HEIGHT)
            // Should be the exact same size as the cells in GameGrid, but not rounded to nearest px
            val cellSize = if (isGridMaxWidth) {
                itemUsableWidth / GRID_WIDTH
            } else {
                itemUsableHeight / GRID_HEIGHT
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                GameGrid(
                    gameUiState.grid,
                    dragAndDropState,
                    onItemDropped = {item, position ->
                        gameViewModel.movePiece(item, position)
                    },
                )

                Hand(
                    handState = gameUiState.hand,
                    numAvailablePieces = gameUiState.numAvailablePieces,
                    dragAndDropState = dragAndDropState,
                    cellSize = cellSize,
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
        }
    }
}


private const val previewHeight = 835
private const val previewWidth = 375
//private const val previewHeight = 375
//private const val previewWidth = 835

@Preview(showBackground = false, widthDp = previewWidth, heightDp = previewHeight)
@Composable
fun GameScreenPreview() {
    DevilitionTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .size(height = previewHeight.dp, width = previewWidth.dp)
                .background(MaterialTheme.colorScheme.background)
        ) {
            GameScreen()
        }
    }
}