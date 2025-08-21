package com.rbits.devilition.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rbits.devilition.ui.theme.DevilitionTheme

@Composable
fun GridCell(
    modifier: Modifier = Modifier,
    item: GridItem? = null,
) {
    val elevation = if (item == null) {
        CardDefaults.cardElevation(0.dp)
    } else {
        CardDefaults.cardElevation(2.dp)
    }


    val cardColor = if (item == null) {
        MaterialTheme.colorScheme.surfaceContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        elevation = elevation,
        colors = CardDefaults.cardColors(cardColor),
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f),
    ) {
        Box(
            modifier = Modifier
                .padding(4.dp),
        ) {
            if (item is GridItem.Piece) {
                Text(
                    item.type.name,
                    style = MaterialTheme.typography.bodySmall,
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