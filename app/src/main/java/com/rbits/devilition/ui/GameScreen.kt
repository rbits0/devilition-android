package com.rbits.devilition.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mohamedrejeb.compose.dnd.DragAndDropContainer
import com.mohamedrejeb.compose.dnd.rememberDragAndDropState
import com.rbits.devilition.R
import com.rbits.devilition.data.GRID_HEIGHT
import com.rbits.devilition.data.GRID_WIDTH
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

const val GRID_SPACING_DP = 2
const val DETONATION_STEP_TIME_MS = 1000L

@Composable
fun GameScreen(
    modifier: Modifier = Modifier,
    gameViewModel: GameViewModel,
) {
    val scope = rememberCoroutineScope()
    val gameState by gameViewModel.gameState.collectAsStateWithLifecycle()
    val dragAndDropState = rememberDragAndDropState<GridItem.Piece>()
    var detonateStarted by remember { mutableStateOf(false) }
    var selectedForDetonation: GridItem.Piece? by remember { mutableStateOf(null) }

    val targetedCells by remember(
        dragAndDropState.draggedItem?.data,
        dragAndDropState.hoveredDropTargetKey,
        gameState.unconfirmedPiece,
    ) { derivedStateOf {
        val draggedItem = dragAndDropState.draggedItem?.data
        val position = dragAndDropState.hoveredDropTargetKey
        val unconfirmedPiece= gameState.unconfirmedPiece

        if (draggedItem != null && position is PiecePos.GridPos) {
            gameState.getPieceTargetCells(
                draggedItem.copy(position = (position))
            )
        } else if (unconfirmedPiece != null) {
            gameState.getPieceTargetCells(unconfirmedPiece)
        } else {
            null
        }
    } }

    fun onItemClicked(item: GridItem.Piece) {
        if (detonateStarted) {
            selectedForDetonation = item
        } else {
            gameViewModel.rotatePiece(item)
        }
    }

    fun onDetonate() {
        val selected = selectedForDetonation ?: return
        selectedForDetonation = null
        detonateStarted = false

        scope.launch {
            // gameState doesn't immediately update, so this keeps the up-to-date value
            var currentState = gameViewModel.armPiece(selected)

            while (currentState.stage == GameStage.DETONATION) {
                delay(DETONATION_STEP_TIME_MS)
                currentState = gameViewModel.runDetonationStep()
            }

            currentState = gameViewModel.roundEnd()
            if (currentState.stage == GameStage.ROUND_START) {
                delay(DETONATION_STEP_TIME_MS)
                gameViewModel.roundStart()
            } else {
            }
        }
    }

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
                    gameState.grid,
                    dragAndDropState,
                    onItemDropped = { item, position ->
                        gameViewModel.movePiece(item, position)
                    },
                    detonateStarted = detonateStarted,
                    selectedForDetonation = selectedForDetonation,
                    targetedCells = targetedCells,
                    onItemClicked = { onItemClicked(it) },
                )

                Hand(
                    handState = gameState.hand,
                    numAvailablePieces = gameState.numAvailablePieces,
                    dragAndDropState = dragAndDropState,
                    cellSize = cellSize,
                    dragEnabled = (
                        gameState.canPlacePieceFromHand()
                            && (!detonateStarted)
                    ),
                    confirmEnabled = gameState.unconfirmedPiece!= null,
                    startDetonateEnabled = gameState.unconfirmedPiece== null,
                    detonateStarted = detonateStarted,
                    confirmDetonateEnabled = selectedForDetonation != null,
                    onItemRotated = { item ->
                        gameViewModel.rotatePiece(item)
                    },
                    onConfirmPlacement = { gameViewModel.confirmPlacement() },
                    onCancelPlacement = { gameViewModel.cancelPlacement() },
                    onStartDetonate = {
                        detonateStarted = true
                    },
                    onConfirmDetonate = { onDetonate() },
                    onCancelDetonate = {
                        detonateStarted = false
                        selectedForDetonation = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                )

                if (gameState.stage == GameStage.LOSE) {
                    WinLoseDialog(
                        onDismiss = { gameViewModel.reset() },
                        text = stringResource(R.string.lose, gameState.score()),
                    )
                } else if (gameState.stage == GameStage.WIN) {
                    WinLoseDialog(
                        onDismiss = { gameViewModel.reset() },
                        text = stringResource(R.string.win, gameState.score()),
                    )
                }
            }
        }
    }
}


//private const val previewHeight = 835
//private const val previewWidth = 375
////private const val previewHeight = 375
////private const val previewWidth = 835
//
//@Preview(showBackground = false, widthDp = previewWidth, heightDp = previewHeight)
//@Composable
//fun GameScreenPreview() {
//    DevilitionTheme(darkTheme = true) {
//        Box(
//            modifier = Modifier
//                .size(height = previewHeight.dp, width = previewWidth.dp)
//                .background(MaterialTheme.colorScheme.background)
//        ) {
//            GameScreen()
//        }
//    }
//}