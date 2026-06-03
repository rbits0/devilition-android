package com.rbits.devilition.data

import com.rbits.devilition.ui.DemonType
import com.rbits.devilition.ui.PieceType

const val GRID_HEIGHT = 8
const val GRID_WIDTH = 10
const val HAND_SIZE = 3
const val NUM_STARTING_TOWNIES = 2

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
fun piecesPerRound(round: Int): Int =
    when (round) {
        1, 2, 5, 8, 9 -> 15
        3, 6 -> 10
        4, 7 -> 20
        else -> 0
    }

fun demonTypeHealth(demonType: DemonType): Int =
    when (demonType) {
        DemonType.MINOR -> 1
        DemonType.MAJOR -> 2
        DemonType.ELDER -> 2
        DemonType.BOSS -> 10
    }

