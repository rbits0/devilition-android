package com.rbits.devilition.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rbits.devilition.R
import com.rbits.devilition.ui.theme.DevilitionTheme

@Composable
fun GridCell(
    modifier: Modifier = Modifier,
    item: GridItem? = null,
) {
    val elevation = if (item == null) {
        0.dp
    } else {
        2.dp
    }


    val surfaceColor = if (item == null) {
        MaterialTheme.colorScheme.surfaceContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    Surface(
        tonalElevation = elevation,
        shadowElevation = elevation,
        color = surfaceColor,
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
            .aspectRatio(1f),
    ) {
        Box(
            modifier = Modifier
                .padding(4.dp),
        ) {
            if (item is GridItem.Piece) {
                Image(
                    painter = painterResource(R.drawable.snake_vertical),
                    contentDescription = stringResource(R.string.snake),
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}


@Preview()
@Composable
fun GridCellPreview() {
    val piece = GridItem.Piece(
        type = PieceType.SNAKE,
        rotation = 0,
    )

    DevilitionTheme(darkTheme = true) {
        GridCell(
            item = piece,
            modifier = Modifier
                .size(60.dp, 60.dp)
        )
    }
}