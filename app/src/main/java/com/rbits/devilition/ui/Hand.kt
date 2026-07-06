package com.rbits.devilition.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.DragAndDropState
import com.mohamedrejeb.compose.dnd.drag.DraggableItem
import com.mohamedrejeb.compose.dnd.rememberDragAndDropState
import com.rbits.devilition.R
import com.rbits.devilition.data.NUM_ROUNDS
import com.rbits.devilition.ui.theme.DevilitionTheme
import com.rbits.devilition.warningButtonColors

@Composable
fun Hand(
    handState: Array<GridItem.Piece?>,
    numAvailablePieces: Int,
    round: Int,
    seconds: Int,
    dragAndDropState: DragAndDropState<GridItem.Piece>,
    cellSize: Dp,
    dragEnabled: Boolean,
    confirmEnabled: Boolean,
    startDetonateEnabled: Boolean,
    detonateStarted: Boolean,
    confirmDetonateEnabled: Boolean,
    onItemRotated: (GridItem.Piece) -> Unit,
    onConfirmPlacement: () -> Unit,
    onCancelPlacement: () -> Unit,
    onStartDetonate: () -> Unit,
    onConfirmDetonate: () -> Unit,
    onCancelDetonate: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        tonalElevation = 2.dp,
        shadowElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(10.dp)
                .width(IntrinsicSize.Max),
        ) {

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                Text(
                    formatSeconds(seconds),
                    style = MaterialTheme.typography.bodyLarge,
                )

                Text(
                    stringResource(
                        R.string.round_count,
                        round,
                        NUM_ROUNDS,
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            Text(
                stringResource(R.string.piece_count, numAvailablePieces),
                style = MaterialTheme.typography.titleLarge,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                handState.forEach { item -> if (item != null) {
                    DraggableItem(
                        state = dragAndDropState,
                        key = item.id,
                        data = item,
                        enabled = dragEnabled,
                        draggableContent = { GridCellItem(
                            item,
                            onClick = {},
                            highlighted = true,
                        ) },
                    ) {
                        GridCellItem(
                            item = item,
                            onClick = { onItemRotated(item) },
                            clickEnabled = true,
                            highlighted = true,
                            modifier = Modifier
                                .size(cellSize)
                                .graphicsLayer {
                                    alpha = if (isDragging) 0f else 1f
                                }
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(cellSize)
                    )
                }}
            }

            if (!detonateStarted) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(top = 12.dp),
                ) {

                    OutlinedButton(
                        onClick = onCancelPlacement,
                        enabled = confirmEnabled,
                    ) {
                        Text(stringResource(R.string.cancel_placement))
                    }

                    Button(
                        onClick = onConfirmPlacement,
                        enabled = confirmEnabled,
                    ) {
                        Text(stringResource(R.string.confirm_placement))
                    }

                }

                Button(
                    onClick = { onStartDetonate() },
                    enabled = startDetonateEnabled,
                ) {
                    Text(stringResource(R.string.start_detonate))
                }

                Button(
                    onClick = { onReset() },
                    colors = warningButtonColors(),
                ) {
                    Text(stringResource(R.string.reset_game))
                }
            } else {
                // Detonate started
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(top = 12.dp),
                ) {

                    OutlinedButton(
                        onClick = { onCancelDetonate() },
                    ) {
                        Text(stringResource(R.string.cancel_detonate))
                    }

                    Button(
                        onClick = onConfirmDetonate,
                        enabled = confirmDetonateEnabled,
                    ) {
                        Text(stringResource(R.string.confirm_detonate))
                    }

                }
            }

        }
    }
}


private const val previewHeight = 300
private const val previewWidth = 375
@Preview(showBackground = false, heightDp = previewHeight, widthDp = previewWidth)
@Composable
fun HandPreview() {
    DevilitionTheme(darkTheme = true) {
        Box(
            modifier = Modifier.size(previewWidth.dp, previewHeight.dp)
        ) {
            Hand(
                handState = Array(3) { i -> GridItem.Piece(
                    pieceType = PieceType.SNAKE,
                    id = i,
                    facing = Direction.DOWN,
                    position = PiecePos.HandPos(i)
                )},
                numAvailablePieces = 15,
                round = 1,
                seconds = 0,
                dragAndDropState = rememberDragAndDropState(),
                cellSize = 30.dp,
                dragEnabled = true,
                confirmEnabled = true,
                startDetonateEnabled = true,
                detonateStarted = false,
                confirmDetonateEnabled = true,
                onItemRotated = {},
                onConfirmPlacement = {},
                onCancelPlacement = {},
                onStartDetonate = {},
                onConfirmDetonate = {},
                onCancelDetonate = {},
                onReset = {},
                modifier = Modifier
                    .fillMaxSize()
                ,
            )
        }
    }
}