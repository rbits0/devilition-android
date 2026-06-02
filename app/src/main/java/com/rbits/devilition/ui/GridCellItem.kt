package com.rbits.devilition.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rbits.devilition.R

@Composable
fun GridCellItem(
    item: GridItem,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    isDragging: Boolean = false,
) {
    val surfaceColor = when (item) {
        is GridItem.Demon, is GridItem.Piece -> MaterialTheme.colorScheme.surfaceVariant
        is GridItem.Hole, is GridItem.BossHitbox -> Color.Transparent
    }
    val elevation = when (item) {
        is GridItem.Demon, is GridItem.Piece -> 2.dp
        is GridItem.Hole, is GridItem.BossHitbox -> 0.dp
    }
    val border = if (
        isDragging
        || (item is GridItem.Piece && !item.placementConfirmed)
    ) {
        BorderStroke(
            width = 2.dp,
            brush = SolidColor(MaterialTheme.colorScheme.primary),
        )
    } else {
        null
    }

    Surface(
        tonalElevation = elevation,
        shadowElevation = elevation,
        color = surfaceColor,
        shape = RoundedCornerShape(6.dp),
        onClick = { onClick?.invoke() },
        border = border,
        modifier = modifier
            .aspectRatio(1f),
    ) {
        Box(
            modifier = Modifier
                .padding(4.dp),
        ) {
            if (item is GridItem.Piece) {
                Image(
                    painter = painterResource(getImageId(item)),
                    contentDescription = stringResource(R.string.snake),
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@DrawableRes
fun getImageId(item: GridItem): Int {
    // TODO: Implement getImageId
    return if (item is GridItem.Piece && (item.facing == Direction.LEFT || item.facing == Direction.RIGHT)) {
        R.drawable.snake_horizontal
    } else {
        R.drawable.snake_vertical
    }
}