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
    val unusedPieces: List<GridItem.Piece> = listOf(),
    val bag: List<GridItem.Piece> = listOf(),
    val availablePieces: List<GridItem.Piece> = listOf(),
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