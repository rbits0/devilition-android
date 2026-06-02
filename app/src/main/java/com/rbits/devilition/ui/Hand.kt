package com.rbits.devilition.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import com.rbits.devilition.ui.theme.DevilitionTheme

@Composable
fun Hand(
    handState: Array<GridItem.Piece?>,
    numAvailablePieces: Int,
    dragAndDropState: DragAndDropState<GridItem.Piece>,
    cellSize: Dp,
    enabled: Boolean,
    onItemRotated: (GridItem.Piece) -> Unit,
    onConfirmPlacement: () -> Unit,
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
        ) {

            Text(
                "Pieces: $numAvailablePieces",
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
                        enabled = enabled,
                        draggableContent = { GridCellItem(
                            item,
                            onClick = {},
                            isDragging = true,
                        ) },
                    ) {
                        GridCellItem(
                            item = item,
                            onClick = { onItemRotated(item) },
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

            Button(
                onClick = onConfirmPlacement,
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Text(stringResource(R.string.confirm_placement))
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
                    type = PieceType.SNAKE,
                    id = i,
                    facing = Direction.DOWN,
                    position = PiecePos.HandPos(i)
                )},
                numAvailablePieces = 15,
                dragAndDropState = rememberDragAndDropState(),
                cellSize = 30.dp,
                enabled = true,
                onItemRotated = {},
                onConfirmPlacement = {},
                modifier = Modifier
                    .fillMaxSize()
                ,
            )
        }
    }
}