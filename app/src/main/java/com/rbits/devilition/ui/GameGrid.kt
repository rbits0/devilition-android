package com.rbits.devilition.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rbits.devilition.data.GRID_HEIGHT
import com.rbits.devilition.data.GRID_WIDTH
import com.rbits.devilition.ui.theme.DevilitionTheme

@Composable
fun GameGrid(
    gridState: Array<Array<GridItem?>>,
    modifier: Modifier = Modifier,
) {
    val numRows = gridState.size
    val numColumns = gridState[0].size

    Column(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        gridState.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
            ) {
                row.forEach { item ->
                    GridCell(
                        item = item,
                        modifier = Modifier
                            .weight(1f)
                            .padding(2.dp)
                    )
                }
            }
        }
    }
}


@Preview(showBackground = false)
@Composable
fun GameGridPreview() {
    val gridState = Array(GRID_HEIGHT) { Array<GridItem?>(GRID_WIDTH) { null } }

    DevilitionTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .size(height = 250.dp, width = 200.dp)
        ) {
            GameGrid(
                gridState = gridState,
                modifier = Modifier
            )
        }
    }
}

