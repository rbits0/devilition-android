package com.rbits.devilition.ui

import com.rbits.devilition.data.GRID_HEIGHT
import com.rbits.devilition.data.GRID_WIDTH


enum class DemonType {
    MINOR,
    MAJOR,
    ELDER,
    BOSS,
}

enum class PieceType {
    SNAKE,
    STRAWMAN,
    TOAD,
    CROSS,
    PLUS,
    BOMB,
    ROCKET,
    ROCKET_PAD,
    CANNON,
}

enum class Direction {
    UP,
    RIGHT,
    DOWN,
    LEFT,
}

enum class RocketColor {
    PINK,
    BLUE,
}

sealed class GridItem {

    data class Demon(
        val type: DemonType,
        val health: Int,
        val maxHealth: Int,
    ) : GridItem()

    data class Piece(
        val type: PieceType,
        val rotation: Int,
        val color: RocketColor? = null,
    ) : GridItem()

}



data class GameUiState(
    val grid: Array<Array<GridItem?>> = Array(GRID_HEIGHT) { Array(GRID_WIDTH) { null } },
    val unusedPieces: List<GridItem.Piece>,
    val bag: List<GridItem.Piece>,
    val availablePieces: List<GridItem.Piece>,
    val score: Int = 0,
)