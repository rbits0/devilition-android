package com.rbits.devilition.ui

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    position: Pair<Int, Int>,
    onItemDropped: (GridItem.Piece) -> Unit,
    modifier: Modifier = Modifier,
    item: GridItem? = null,
) {
    Surface(
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
            .aspectRatio(1f)
            .dropTarget(
                state = dragAndDropState,
                key = position,
                onDrop = { state ->
                    onItemDropped(state.data)
                },
            ),
    ) {
        if (item != null) {
            if (item is GridItem.Piece) {
                DraggableItem(
                    state = dragAndDropState,
                    key = item.id,
                    data = item,
                    enabled = true,
                ) {
                    GridCellItem(
                        item,
                        modifier = Modifier
                            .graphicsLayer {
                                alpha = if (isDragging) 0f else 1f
                            }
                    )
                }
            } else {
                GridCellItem(item)
            }
        }
    }
}


@Preview()
@Composable
fun GridCellPreview() {
    val piece = GridItem.Piece(
        type = PieceType.SNAKE,
        facing = Direction.DOWN,
        id = 0,
    )

    DevilitionTheme(darkTheme = true) {
        GridCell(
            rememberDragAndDropState(),
            position = Pair(0, 0),
            onItemDropped = {},
            item = piece,
            modifier = Modifier
                .size(60.dp, 60.dp),
        )
    }
}