package com.rbits.devilition.ui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rbits.devilition.data.getImageIdFromSprite
import com.rbits.devilition.data.getStringIdFromSprite
import com.rbits.devilition.ui.theme.DevilitionTheme

const val NUM_EXPLOSION_FRAMES = 6

@Composable
fun GridCellItem(
    item: GridItem,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    clickEnabled: Boolean = false,
    highlighted: Boolean = false,
    targeted: Boolean = false,
    armed: Boolean = false,
) {
    val surfaceColor = if (targeted || armed) {
        MaterialTheme.colorScheme.tertiary
    } else if (item is SpriteItem) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        Color.Transparent
    }

    val elevation = if (item is SpriteItem) {
        2.dp
    } else {
        0.dp
    }

    val border = if (highlighted) {
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
        enabled = clickEnabled,
        border = border,
        modifier = modifier
            .aspectRatio(1f),
    ) {
        Box(
            modifier = Modifier
                .padding(4.dp),
        ) {
            if (item is SpriteItem) {
                val bitmap = ImageBitmap.imageResource(
                    getImageIdFromSprite(item)
                )

                Image(
                    bitmap = bitmap,
                    contentDescription = stringResource(getStringIdFromSprite(item)),
                    contentScale = ContentScale.Fit,
                    alignment = Alignment.BottomCenter,
                    filterQuality = FilterQuality.None,
                    modifier = Modifier
                        .fillMaxSize()
                )
            }
        }
    }
}


@Preview()
@Composable
fun GridCellItemPreview() {
    val piece = GridItem.Piece(
        pieceType = PieceType.SNAKE,
        facing = Direction.DOWN,
        id = 0,
    )

    DevilitionTheme(darkTheme = true) {
        GridCellItem(
            item = piece,
            modifier = Modifier
                .size(60.dp, 60.dp),
        )
    }
}
