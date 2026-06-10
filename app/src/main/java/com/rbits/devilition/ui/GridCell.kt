package com.rbits.devilition.ui

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.DragAndDropState
import com.mohamedrejeb.compose.dnd.drag.DraggableItem
import com.mohamedrejeb.compose.dnd.drop.dropTarget
import com.mohamedrejeb.compose.dnd.rememberDragAndDropState
import com.rbits.devilition.ui.theme.DevilitionTheme

@Composable
fun GridCell(
    dragAndDropState: DragAndDropState<GridItem.Piece>,
    position: PiecePos.GridPos,
    detonateStarted: Boolean,
    selectedForDetonation: Boolean,
    onItemDropped: (GridItem.Piece) -> Unit,
    onItemClicked: (GridItem.Piece) -> Unit,
    modifier: Modifier = Modifier,
    item: GridItem? = null,
    targeted: Boolean = false,
) {
    var isHoveredOver by remember { mutableStateOf(false) }

    // Can drop piece on this cell if this cell is empty
    // OR if dragging piece back to the same cell
    fun canDropPiece(draggedItem: GridItem.Piece) =
        item == null
        || (item is GridItem.Piece && draggedItem.id == item.id)

    val surfaceColor = if (item is GridItem.Hole || item is GridItem.BossHitbox) {
        Color.Transparent
    } else if (targeted) {
        MaterialTheme.colorScheme.tertiary
    } else if (isHoveredOver) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }

    val isDraggable = item is GridItem.Piece && !item.placementConfirmed

    Surface(
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        color = surfaceColor,
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
            .aspectRatio(1f)
            .dropTarget(
                state = dragAndDropState,
                key = position,
                onDrop = { state ->
                    if (canDropPiece(state.data)) {
                        onItemDropped(state.data)
                        isHoveredOver = false
                    }
                },
                onDragEnter = { state ->
                    if (canDropPiece(state.data)) {
                        isHoveredOver = true
                    }
                },
                onDragExit = { isHoveredOver = false },
            ),
    ) {
        if (item != null && item !is GridItem.Hole) {
            if (isDraggable) {
                // Draggable piece
                DraggableItem(
                    state = dragAndDropState,
                    key = item.id,
                    data = item,
                    enabled = true,
                    draggableContent = {
                        GridCellItem(
                            item,
                            onClick = {},
                            highlighted = true,
                        )
                    },
                ) {
                    GridCellItem(
                        item,
                        onClick = { onItemClicked(item) },
                        clickEnabled = true,
                        highlighted = true,
                        targeted = targeted,
                        modifier = Modifier
                            .graphicsLayer {
                                alpha = if (isDragging) 0f else 1f
                            }
                    )
                }
            } else if (item is GridItem.Piece) {
                // Non-draggable piece
                GridCellItem(
                    item,
                    onClick = { onItemClicked(item) },
                    clickEnabled = detonateStarted,
                    highlighted = selectedForDetonation,
                    targeted = targeted,
                )
            } else {
                // Non-piece
                GridCellItem(
                    item,
                    targeted = targeted,
                )
            }
        }
    }
}


@Preview()
@Composable
fun GridCellPreview() {
    val piece = GridItem.Piece(
        pieceType = PieceType.SNAKE,
        facing = Direction.DOWN,
        id = 0,
    )

    DevilitionTheme(darkTheme = true) {
        GridCell(
            rememberDragAndDropState(),
            position = PiecePos.GridPos(0, 0),
            onItemDropped = {},
            item = piece,
            onItemClicked = {},
            selectedForDetonation = false,
            detonateStarted = false,
            modifier = Modifier
                .size(60.dp, 60.dp),
        )
    }
}