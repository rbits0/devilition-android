package com.rbits.devilition.ui

import android.graphics.drawable.shapes.RectShape
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.rememberDragAndDropState
import com.rbits.devilition.R
import com.rbits.devilition.ui.theme.DevilitionTheme

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
            if (item is SpriteItem) {
                Image(
                    painter = painterResource(getImageId(item)),
                    contentDescription = stringResource(R.string.snake),
                    contentScale = ContentScale.Fit,
                    alignment = Alignment.BottomCenter,
                    modifier = Modifier
                        .fillMaxSize()
//                        .background(Color.Red)
//                        .clip(GenericShape{ size, layoutDirection ->
//                            addRect(Rect(0f, 0f, 100f, 100f))
//                        }),
                )
            }
        }
    }
}

@DrawableRes
fun getImageId(item: SpriteItem): Int = (
    when (item) {

        is GridItem.Piece -> {
            when (item.type) {
                PieceType.ROCKET -> when (item.color) {
                    RocketColor.BLUE -> R.drawable.rocket
                    RocketColor.PINK -> R.drawable.rocket_alt
                }
                PieceType.ROCKET_PAD -> when (item.color) {
                    RocketColor.BLUE -> R.drawable.rocket_pad
                    RocketColor.PINK -> R.drawable.rocket_pad_alt
                }
                PieceType.SNAKE -> when (item.facing) {
                    Direction.UP, Direction.DOWN -> R.drawable.snake_vert
                    Direction.LEFT, Direction.RIGHT -> R.drawable.snake_horz
                }
                PieceType.STRAWMAN -> when (item.facing) {
                    Direction.UP -> R.drawable.straw_man_u
                    Direction.DOWN -> R.drawable.straw_man_d
                    Direction.LEFT -> R.drawable.straw_man_l
                    Direction.RIGHT -> R.drawable.straw_man_r
                }
                PieceType.CANNON -> when (item.facing) {
                    Direction.UP -> R.drawable.cannon_u
                    Direction.DOWN -> R.drawable.cannon_d
                    Direction.LEFT -> R.drawable.cannon_l
                    Direction.RIGHT -> R.drawable.cannon_r
                }
                PieceType.TOAD -> R.drawable.toad
                PieceType.CROSS -> R.drawable.cross
                PieceType.PLUS -> R.drawable.plus
                PieceType.BOMB -> R.drawable.bomb
            }
        }

        is GridItem.Demon -> {
            when (item.type) {
                DemonType.MINOR -> R.drawable.demon_minor
                DemonType.MAJOR -> if (item.health > 1) {
                    R.drawable.demon_major
                } else {
                    R.drawable.demon_major_hurt
                }
                DemonType.ELDER -> if (item.health > 1) {
                    R.drawable.demon_elder
                } else {
                    R.drawable.demon_elder_hurt
                }
                DemonType.BOSS -> R.drawable.demon_boss
            }
        }
    }
)


@Preview()
@Composable
fun GridCellItemPreview() {
    val piece = GridItem.Piece(
        type = PieceType.SNAKE,
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
