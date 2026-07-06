package com.rbits.devilition.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mohamedrejeb.compose.dnd.DragAndDropContainer
import com.mohamedrejeb.compose.dnd.rememberDragAndDropState
import com.rbits.devilition.R
import com.rbits.devilition.data.GRID_HEIGHT
import com.rbits.devilition.data.GRID_WIDTH
import com.rbits.devilition.data.MockGameRepository
import com.rbits.devilition.ui.theme.DevilitionTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

const val GRID_SPACING_DP = 2
val DETONATION_STEP_TIME = 1.seconds

@Composable
fun GameScreen(
    gameState: GameState,
    roundStart: () -> Unit,
    movePiece: (GridItem.Piece, PiecePos.GridPos) -> Unit,
    rotatePiece: (GridItem.Piece) -> Unit,
    confirmPlacement: () -> Unit,
    cancelPlacement: () -> Unit,
    armPiece: (GridItem.Piece) -> GameState,
    runDetonationStep: () -> GameState,
    roundEnd: () -> GameState,
    reset: () -> Unit,
    addToPastGames: () -> Unit,
    startTimer: () -> Unit,
    stopTimer: () -> Unit,
    modifier: Modifier = Modifier,
    timeBonusEnabled: Boolean = true,
) {
    val scope = rememberCoroutineScope()
    val dragAndDropState = rememberDragAndDropState<GridItem.Piece>()
    var detonateStarted by remember { mutableStateOf(false) }
    var selectedForDetonation: GridItem.Piece? by remember { mutableStateOf(null) }
    var resetDialogVisible by remember { mutableStateOf(false) }
    var detonateJob: Job? by remember { mutableStateOf(null) }

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

    fun onDetonate() {
        val selected = selectedForDetonation
        selectedForDetonation = null
        detonateStarted = false

        detonateJob = scope.launch {
            // gameState doesn't immediately update, so this keeps the up-to-date value
            var currentState = gameState

            if (selected != null) {
                currentState = armPiece(selected)
            }

            while (currentState.stage == GameStage.DETONATION) {
                delay(DETONATION_STEP_TIME)
                currentState = runDetonationStep()
            }

            if (currentState.stage == GameStage.ROUND_END) {
                currentState = roundEnd()
            }
            when (currentState.stage) {
                GameStage.ROUND_START -> {
                    delay(DETONATION_STEP_TIME)
                    roundStart()
                }
                GameStage.WIN, GameStage.LOSE -> {
                    addToPastGames()
                }
                else -> {}
            }
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        startTimer()

        // If left composition during detonation, we need to continue it
        val detonateJob = detonateJob
        if (detonateJob == null || !detonateJob.isActive) {
            when (gameState.stage) {
                GameStage.DETONATION,
                GameStage.ROUND_END,
                GameStage.ROUND_START -> {
                    onDetonate()
                }
                else -> {}
            }
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        stopTimer()
    }

    fun onItemClicked(item: GridItem.Piece) {
        if (detonateStarted) {
            selectedForDetonation = item
        } else {
            rotatePiece(item)
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

            // We're using this instead of FlexBox because FlexBox has some weird bugs with layout
            RowOrColumn(
                direction = (
                    if (isGridMaxWidth) {
                        RowOrColumnDirection.Column
                    } else {
                        RowOrColumnDirection.Row
                    }
                ),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                GameGrid(
                    gameState.grid,
                    dragAndDropState,
                    onItemDropped = movePiece,
                    detonateStarted = detonateStarted,
                    selectedForDetonation = selectedForDetonation,
                    armedCells = gameState.armedPieces.mapNotNullTo(mutableSetOf()) {
                        piece -> piece.position as? PiecePos.GridPos
                    },
                    targetedCells = targetedCells,
                    onItemClicked = { onItemClicked(it) },
                    modifier = Modifier
                        .width(IntrinsicSize.Min)
                        .weight(1f, false),
                )

                Hand(
                    handState = gameState.hand,
                    numAvailablePieces = gameState.numAvailablePieces,
                    round = gameState.round,
                    seconds = gameState.seconds,
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
                    onItemRotated = rotatePiece,
                    onConfirmPlacement = confirmPlacement,
                    onCancelPlacement = cancelPlacement,
                    onStartDetonate = {
                        detonateStarted = true
                    },
                    onConfirmDetonate = { onDetonate() },
                    onCancelDetonate = {
                        detonateStarted = false
                        selectedForDetonation = null
                    },
                    onReset = { resetDialogVisible = true },
                    modifier = (
                        if (isGridMaxWidth) {
                            Modifier.fillMaxWidth()
                        } else {
                            Modifier.fillMaxHeight()
                        }
                    )
                )

            }

            if (resetDialogVisible) {
                ResetDialog(
                    onDismiss = { resetDialogVisible = false },
                    onConfirm = {
                        resetDialogVisible = false
                        reset()
                    }
                )
            } else if (gameState.stage == GameStage.LOSE) {
                WinLoseDialog(
                    onDismiss = reset,
                    text = stringResource(
                        R.string.lose,
                        gameState.score(timeBonusEnabled),
                    ),
                )
            } else if (gameState.stage == GameStage.WIN) {
                WinLoseDialog(
                    onDismiss = reset,
                    text = stringResource(
                        R.string.win,
                        gameState.score(timeBonusEnabled),
                    ),
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
    val gameViewModel = viewModel<GameViewModel>(
        factory = GameViewModelFactory(MockGameRepository())
    )
    val gameState by gameViewModel.gameState.collectAsStateWithLifecycle()

    DevilitionTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .size(height = previewHeight.dp, width = previewWidth.dp)
                .background(MaterialTheme.colorScheme.background)
        ) {
            GameScreen(
                gameState,
                roundStart = gameViewModel::roundStart,
                movePiece = gameViewModel::movePiece,
                rotatePiece = gameViewModel::rotatePiece,
                confirmPlacement = gameViewModel::confirmPlacement,
                cancelPlacement = gameViewModel::cancelPlacement,
                armPiece = gameViewModel::armPiece,
                runDetonationStep = gameViewModel::runDetonationStep,
                roundEnd = gameViewModel::roundEnd,
                reset = gameViewModel::reset,
                addToPastGames = gameViewModel::addToPastGames,
                startTimer = gameViewModel::startTimer,
                stopTimer = gameViewModel::stopTimer,
            )
        }
    }
}