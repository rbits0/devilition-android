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

enum class RocketColor {
    PINK,
    BLUE,
}

enum class Direction {
    UP,
    DOWN,
    LEFT,
    RIGHT,
}

sealed class GridItem {

    data class Demon(
        val type: DemonType,
        val health: Int,
        val maxHealth: Int,
    ) : GridItem()

    // Boss takes up a 2x2 space
    // If this item is hit, it should damage the boss
    data class BossHitbox(
        val bossPos: Pair<Int, Int>,
    ) : GridItem()

    data class Piece(
        val type: PieceType,
        val facing: Direction,
        val id: Int,
        val position: Pair<Int, Int>? = null,
        val color: RocketColor? = null,
    ) : GridItem()

    class Hole() : GridItem()

}



data class GameUiState(
    val grid: Array<Array<GridItem?>> = Array(GRID_HEIGHT) { Array(GRID_WIDTH) { null } },
    val hand: List<GridItem.Piece> = listOf(),
    val bag: List<GridItem.Piece> = listOf(),
    val numAvailablePieces: Int = 0,
    val round: Int = 0,
    val score: Int = 0,
) {

    // Auto-generated function to handle the array
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GameUiState

        if (score != other.score) return false
        if (!grid.contentDeepEquals(other.grid)) return false

        return true
    }

    // Auto-generated function to handle the array
    override fun hashCode(): Int {
        var result = score
        result = 31 * result + grid.contentDeepHashCode()
        return result
    }
}