package com.rbits.devilition.ui

import androidx.annotation.DrawableRes
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.rbits.devilition.R
import com.rbits.devilition.ui.theme.DevilitionTheme
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

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
    var explosionAnimationState by remember { mutableIntStateOf(0) }

    val surfaceColor = if (targeted || armed) {
        MaterialTheme.colorScheme.tertiary
    } else if (item is GridItem.Explosion) {
        Color.Transparent
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


    if (item is GridItem.Explosion) {
        LaunchedEffect(item.id) {
            for (i in 0..<NUM_EXPLOSION_FRAMES) {
                explosionAnimationState = i
                delay(1.seconds / NUM_EXPLOSION_FRAMES)
            }
        }
    } else {
        explosionAnimationState = 0
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
                    getImageId(item, explosionAnimationState)
                )

                Image(
                    bitmap = bitmap,
                    contentDescription = stringResource(R.string.snake),
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

@DrawableRes
fun getImageId(item: SpriteItem, explosionAnimationState: Int = 0): Int = (
        when (item) {

            is GridItem.Piece -> {
                when (item.pieceType) {
                    PieceType.ROCKET -> when (item.color) {
                        RocketColor.BLUE -> R.drawable.rocket
                        else -> R.drawable.rocket_alt
                    }
                    PieceType.ROCKET_PAD -> when (item.color) {
                        RocketColor.BLUE -> R.drawable.rocket_pad
                        else -> R.drawable.rocket_pad_alt
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
                when (item.demonType) {
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

            is GridItem.Townie -> {
                when(item.townieType) {
                    TownieType.MAN_1 -> R.drawable.townie_man
                    TownieType.MAN_2 -> R.drawable.townie_man_alt
                    TownieType.WOMAN_1 -> R.drawable.townie_woman
                    TownieType.WOMAN_2 -> R.drawable.townie_woman_alt
                }
            }

            is GridItem.Explosion -> {
                when (explosionAnimationState) {
                    0 -> R.drawable.explosion_0
                    1 -> R.drawable.explosion_1
                    2 -> R.drawable.explosion_2
                    3 -> R.drawable.explosion_3
                    4 -> R.drawable.explosion_4
                    else -> R.drawable.explosion_5
                }
            }

        }
)


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
