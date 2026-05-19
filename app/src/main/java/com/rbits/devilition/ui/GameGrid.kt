package com.rbits.devilition.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.DragAndDropState
import com.mohamedrejeb.compose.dnd.rememberDragAndDropState
import com.rbits.devilition.data.GRID_HEIGHT
import com.rbits.devilition.data.GRID_WIDTH
import com.rbits.devilition.ui.theme.DevilitionTheme

@Composable
fun GameGrid(
    gridState: Array<Array<GridItem?>>,
    dragAndDropState: DragAndDropState<GridItem.Piece>,
    onItemDropped: (GridItem.Piece, Pair<Int, Int>) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(IntrinsicSize.Max)
    ) {
        gridState.withIndex().forEach { (rowIndex, row) ->
            Row(
                modifier = Modifier
                    .weight(1f, fill = false),
            ) {
                row.withIndex().forEach { (colIndex, item) ->
                    GridCell(
                        dragAndDropState,
                        position = Pair(rowIndex, colIndex),
                        item = item,
                        onItemDropped = { item ->
                            onItemDropped(item, Pair(rowIndex, colIndex))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(2.dp),
                    )
                }
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
fun GameGridPreview() {
    val gridState = Array(GRID_HEIGHT) { Array<GridItem?>(GRID_WIDTH) {
        if (it % 2 == 0) {
            GridItem.Piece(
                type = PieceType.SNAKE,
                facing = Direction.DOWN,
                id = 0,
            )
        } else {
            null
        }
    } }

    DevilitionTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .size(height = previewHeight.dp, width = previewWidth.dp)
        ) {
            GameGrid(
                gridState,
                rememberDragAndDropState(),
                onItemDropped = {_, _ -> },
                modifier = Modifier,
            )
        }
    }
}

