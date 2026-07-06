package com.rbits.devilition.data

import androidx.annotation.DrawableRes
import androidx.annotation.IntRange
import androidx.annotation.StringRes
import com.rbits.devilition.R
import com.rbits.devilition.ui.DemonType
import com.rbits.devilition.ui.Direction
import com.rbits.devilition.ui.Explosion
import com.rbits.devilition.ui.GridItem
import com.rbits.devilition.ui.PieceType
import com.rbits.devilition.ui.RocketColor
import com.rbits.devilition.ui.SpriteItem
import com.rbits.devilition.ui.TownieType

const val GRID_HEIGHT = 8
const val GRID_WIDTH = 10
const val HAND_SIZE = 3
const val NUM_STARTING_TOWNIES = 2
const val NUM_ROUNDS = 10

val tierPieces = mapOf(
    1 to listOf(PieceType.CANNON, PieceType.ROCKET, PieceType.BOMB),
    2 to listOf(PieceType.PLUS, PieceType.CROSS, PieceType.TOAD),
    3 to listOf(PieceType.STRAWMAN, PieceType.SNAKE),
)

fun demonsPerRound(round: Int): Map<DemonType, Int>  =
    when (round) {
        1 -> mapOf(DemonType.MINOR to 8, DemonType.MAJOR to 0, DemonType.ELDER to 0)
        2 -> mapOf(DemonType.MINOR to 9, DemonType.MAJOR to 0, DemonType.ELDER to 0)
        3 -> mapOf(DemonType.MINOR to 9, DemonType.MAJOR to 1, DemonType.ELDER to 0)
        4 -> mapOf(DemonType.MINOR to 9, DemonType.MAJOR to 2, DemonType.ELDER to 0)
        5 -> mapOf(DemonType.MINOR to 9, DemonType.MAJOR to 3, DemonType.ELDER to 0)
        6 -> mapOf(DemonType.MINOR to 9, DemonType.MAJOR to 3, DemonType.ELDER to 1)
        7 -> mapOf(DemonType.MINOR to 9, DemonType.MAJOR to 3, DemonType.ELDER to 2)
        8 -> mapOf(DemonType.MINOR to 9, DemonType.MAJOR to 3, DemonType.ELDER to 3)
        else -> mapOf(DemonType.MINOR to 9, DemonType.MAJOR to 3, DemonType.ELDER to 4)
    }
fun piecesPerRound(@IntRange(1, 10) round: Int): Int =
    when (round) {
        1, 2, 5, 8, 9 -> 15
        3, 6 -> 10
        4, 7 -> 20
        10 -> 13
        else -> throw IndexOutOfBoundsException("Round must be between 1 and 10")
    }

fun demonTypeHealth(demonType: DemonType): Int =
    when (demonType) {
        DemonType.MINOR -> 1
        DemonType.MAJOR -> 2
        DemonType.ELDER -> 2
        DemonType.BOSS -> 10
    }

@DrawableRes
fun getImageIdFromSprite(item: SpriteItem, explosionAnimationState: Int = 0): Int = (
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

        is Explosion -> {
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

@StringRes
fun getStringIdFromSprite(item: SpriteItem): Int = when (item) {
    is GridItem.Piece -> {
        when (item.pieceType) {
            PieceType.ROCKET -> when (item.color) {
                RocketColor.BLUE -> R.string.rocket
                else -> R.string.rocket_alt
            }
            PieceType.ROCKET_PAD -> when (item.color) {
                RocketColor.BLUE -> R.string.rocket_pad
                else -> R.string.rocket_pad_alt
            }
            PieceType.SNAKE -> R.string.snake
            PieceType.STRAWMAN -> R.string.strawman
            PieceType.CANNON -> R.string.cannon
            PieceType.TOAD -> R.string.toad
            PieceType.CROSS -> R.string.cross
            PieceType.PLUS -> R.string.plus
            PieceType.BOMB -> R.string.bomb
        }
    }

    is GridItem.Demon -> {
        when (item.demonType) {
            DemonType.MINOR -> R.string.demon_minor
            DemonType.MAJOR -> R.string.demon_major
            DemonType.ELDER -> R.string.demon_elder
            DemonType.BOSS -> R.string.demon_boss
        }
    }

    is GridItem.Townie -> R.string.townie

    is Explosion -> R.string.explosion
}