package com.rbits.devilition.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rbits.devilition.R

@Composable
fun GridCellItem(
    item: GridItem,
    modifier: Modifier = Modifier,
) {
    val surfaceColor = when (item) {
        is GridItem.Demon, is GridItem.Piece -> MaterialTheme.colorScheme.surfaceVariant
        is GridItem.Hole -> MaterialTheme.colorScheme.background
    }

    Surface(
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
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