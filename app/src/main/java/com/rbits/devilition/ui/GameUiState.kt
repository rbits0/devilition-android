package com.rbits.devilition.ui

import android.util.Log
import com.rbits.devilition.TAG
import com.rbits.devilition.data.GRID_HEIGHT
import com.rbits.devilition.data.GRID_WIDTH
import com.rbits.devilition.data.HAND_SIZE
import com.rbits.devilition.data.NUM_STARTING_TOWNIES
import com.rbits.devilition.data.demonTypeHealth
import com.rbits.devilition.data.demonsPerRound
import com.rbits.devilition.data.piecesPerRound
import com.rbits.devilition.data.tierPieces
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.math.max

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

enum class TownieType {
    MAN_1,
    MAN_2,
    WOMAN_1,
    WOMAN_2,
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

sealed class PiecePos {
    data class GridPos(val x: Int, val y: Int) : PiecePos()
    data class HandPos(val pos: Int) : PiecePos()
}

sealed class ExplosionPos {
    data class RelativePos(val x: Int, val y: Int) : ExplosionPos()
    data class FixedPos(val x: Int, val y: Int) : ExplosionPos()
}

// Item with a sprite
sealed interface SpriteItem

sealed class GridItem {

    data class Demon(
        val type: DemonType,
        val health: Int,
        val maxHealth: Int,
    ) : GridItem(), SpriteItem

    // Boss takes up a 2x2 space
    // If this item is hit, it should damage the boss
    data class BossHitbox(
        val bossPos: PiecePos.GridPos,
    ) : GridItem()

    data class Piece(
        val type: PieceType,
        val facing: Direction,
        val id: Int,
        val placementConfirmed: Boolean = true,
        val position: PiecePos? = null,
        val color: RocketColor = RocketColor.entries.random(),
        var rocketTargetId: Int? = null,
    ) : GridItem(), SpriteItem

    data class Townie(
        val type: TownieType
    ) : GridItem(), SpriteItem

    class Hole() : GridItem()

}



data class GameUiState(
    var grid: Array<Array<GridItem?>> = Array(GRID_HEIGHT) { Array(GRID_WIDTH) { null } },
    var hand: Array<GridItem.Piece?>,
    var bag: List<PieceType> = listOf(),
    var numAvailablePieces: Int = 0,
    var round: Int = 0,
    var score: Int = 0,
    var idCounter: Int = 0,
    // Position of the piece that has been dragged onto the board,
    // but hasn't had placement confirmed
    var unconfirmedPiece: GridItem.Piece? = null,
    var armedPieces: MutableSet<GridItem.Piece> = mutableSetOf(),
) {

    companion object {
        fun new(): GameUiState {
            val bag: MutableList<PieceType> = mutableListOf()
            var idCounter = 0
            val hand: MutableList<GridItem.Piece> = mutableListOf()

            // Start with `HAND_SIZE` pieces in hand
            for (i in 0..<HAND_SIZE) {
                val id = idCounter
                val type = drawNewPiece(bag)
                idCounter++
                hand.add(
                    GridItem.Piece(
                        type = type,
                        facing = Direction.DOWN,
                        id = id,
                        position = PiecePos.HandPos(i),
                    )
                )
            }

            val state = GameUiState(
                hand = hand.toTypedArray(),
                bag = bag,
                idCounter = idCounter,
            )

            for (_i in 0..<NUM_STARTING_TOWNIES) {
                state.placeRandomTownie()
            }

            state.roundStart()
            return state
        }
    }


    // Auto-generated function to handle the array
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GameUiState

        if (numAvailablePieces != other.numAvailablePieces) return false
        if (round != other.round) return false
        if (score != other.score) return false
        if (idCounter != other.idCounter) return false
        if (!grid.contentDeepEquals(other.grid)) return false
        if (!hand.contentEquals(other.hand)) return false
        if (bag != other.bag) return false
        if (unconfirmedPiece != other.unconfirmedPiece) return false
        if (armedPieces != other.armedPieces) return false

        return true
    }
    // Auto-generated function to handle the array
    override fun hashCode(): Int {
        var result = numAvailablePieces
        result = 31 * result + round
        result = 31 * result + score
        result = 31 * result + idCounter
        result = 31 * result + grid.contentDeepHashCode()
        result = 31 * result + hand.contentHashCode()
        result = 31 * result + bag.hashCode()
        result = 31 * result + (unconfirmedPiece?.hashCode() ?: 0)
        result = 31 * result + armedPieces.hashCode()
        return result
    }


    fun clone(): GameUiState = this.copy(
        grid = grid.map{ it.clone() }.toTypedArray(),
        hand = hand.clone(),
    )

    fun roundStart() {
        round += 1

        if (round < 10) {
            healDemons()

            placeRandomHoles(count = 1)
            placeDemons(round)
        } else {
            // FINAL ROUND

            // Replace all demons with holes
            grid.forEachIndexed { rowIndex, row ->
                row.forEachIndexed { colIndex, item ->
                    if (item is GridItem.Demon) {
                        grid[rowIndex][colIndex] = GridItem.Hole()
                    }
                }
            }

            // Replace 4x4 square in middle with holes
            // All spots from (3, 3) to (6, 6)
            for (rowIndex in 3..6) {
                for (colIndex in 3..6) {
                    grid[rowIndex][colIndex] = GridItem.Hole()
                }
            }

            placeBoss()

            // Place 2-4 holes
            // TODO: Check the specifics of how this is done in UFO 50
            val numHoles = (2..4).random()
            placeRandomHoles(numHoles)
        }

        numAvailablePieces += piecesPerRound(round)
    }

    // Heal all elder demons
    fun healDemons() {
        grid.forEachIndexed { rowIndex, row ->
            row.forEachIndexed { colIndex, item ->
                if (item is GridItem.Demon && item.type == DemonType.ELDER) {
                    grid[rowIndex][colIndex] = item.copy(health = item.maxHealth)
                }
            }
        }


    }

    fun placeRandomHoles(count: Int = 1) {
        val emptySpaces = getEmptySpaces(grid).toMutableSet()

        for (_i in 0..<count) {
            val holePos = emptySpaces.random()
            emptySpaces.remove(holePos)
            grid[holePos.x][holePos.y] = GridItem.Hole()
        }
    }

    fun placeDemons(round: Int) {
        val emptySpaces = getEmptySpaces(grid).toMutableSet()

        demonsPerRound(round).forEach { (demonType, count) ->
            val health = demonTypeHealth(demonType)

            // Place `count` demons of `demonType`
            for (i in 0..<count) {
                val demonPos = emptySpaces.random()
                emptySpaces.remove(demonPos)
                grid[demonPos.x][demonPos.y] = GridItem.Demon(
                    type = demonType,
                    health = health,
                    maxHealth = health,
                )
            }
        }
    }

    // Place boss in middle
    // Boss takes up a 2x2 space
    fun placeBoss() {
        grid[4][4] = GridItem.Demon(
            type = DemonType.BOSS,
            health = demonTypeHealth(DemonType.BOSS),
            maxHealth = demonTypeHealth(DemonType.BOSS),
        )
        for (pos in listOf(Pair(4, 5), Pair(5, 4), Pair(5, 5))) {
            grid[pos.first][pos.second] = GridItem.BossHitbox(
                bossPos = PiecePos.GridPos(4, 4),
            )
        }
    }

    fun movePiece(item: GridItem.Piece, to: PiecePos.GridPos) {
        val from = item.position
        if (from == null) {
            Log.e(TAG, "Piece position is null")
            return
        }

        when (from) {

            is PiecePos.GridPos -> {
                // Move piece within grid
                val newItem = item.copy(position = to)

                grid[from.x][from.y] = null
                grid[to.x][to.y] = newItem

                // Update unconfirmedPiece
                if (unconfirmedPiece == item) {
                    unconfirmedPiece = newItem
                }
            }

            is PiecePos.HandPos -> {
                // Place piece from hand
                val newItem = item.copy(
                    position = to,
                    placementConfirmed = false,
                )
                hand[from.pos] = null
                unconfirmedPiece = newItem

                grid[to.x][to.y] = newItem
            }

        }
    }

    fun rotatePiece(item: GridItem.Piece) {
        when (val pos = item.position) {

            is PiecePos.GridPos -> {
                grid[pos.x][pos.y] = item.copy(
                    facing = rotateClockwise(item.facing)
                )
            }

            is PiecePos.HandPos -> {
                hand[pos.pos] = item.copy(
                    facing = rotateClockwise(item.facing)
                )
            }

            null -> {}

        }
    }

    // Confirm piece, and draw new piece to replace empty spot in hand
    fun confirmPlacement() {
        val unconfirmedPiece = this.unconfirmedPiece ?: return
        val position = unconfirmedPiece.position
        if (position !is PiecePos.GridPos) {
            Log.e(TAG, "Unable to place piece: Invalid position")
            return
        }

        val newPiecePos = PiecePos.HandPos(hand.indexOf(null))
        val id = idCounter
        idCounter += 1
        numAvailablePieces -= 1

        if (unconfirmedPiece.type == PieceType.ROCKET) {
            // Mark piece as confirmed
            grid[position.x][position.y] = unconfirmedPiece.copy(
                rocketTargetId = id,
                placementConfirmed = true,
            )

            // Replace rocket with pad instead of drawing from bag
            hand[newPiecePos.pos] = GridItem.Piece(
                type = PieceType.ROCKET_PAD,
                facing = Direction.DOWN,
                position = newPiecePos,
                id = id,
                color = unconfirmedPiece.color,
            )

        } else {
            // Mark piece as confirmed
            grid[position.x][position.y] = unconfirmedPiece.copy(
                placementConfirmed = true,
            )

            // Draw piece from bag to replace moved piece
            val newBag = bag.toMutableList()
            val type = drawNewPiece(newBag)
            hand[newPiecePos.pos] = GridItem.Piece(
                type = type,
                facing = Direction.DOWN,
                id = id,
                position = newPiecePos,
            )
            bag = newBag
        }

        this.unconfirmedPiece = null
    }

    fun cancelPlacement() {
        val unconfirmedPiece= unconfirmedPiece ?: return
        val position = unconfirmedPiece.position

        if (position !is PiecePos.GridPos) {
            Log.e(TAG, "Unable to cancel placement: Invalid position")
            return
        }

        val handEmptyPosition = PiecePos.HandPos(hand.indexOf(null))

        // Remove piece from grid
        grid[position.x][position.y] = null

        // Place back in hand
        hand[handEmptyPosition.pos] = unconfirmedPiece.copy(
            position = handEmptyPosition,
            placementConfirmed = true,
        )

        this.unconfirmedPiece = null
    }

    fun armPiece(item: GridItem.Piece) {
        armedPieces = mutableSetOf(item)
    }

    fun runDetonationStep() {
        val armedPieces = armedPieces.toSet()
        this.armedPieces.clear()

        for (item in armedPieces) {
            detonatePiece(item)
        }
    }

    private fun detonatePiece(item: GridItem.Piece) {
        if (item.position !is PiecePos.GridPos) {
            Log.e(TAG, "Can't detonate piece: Invalid position")
            return
        }

        grid[item.position.x][item.position.y] = null

        val cellsToExplode = getPieceTargetCells(item)

        for (pos in cellsToExplode) {
            explodeCell(pos)
        }
    }

    fun getPieceTargetCells(item: GridItem.Piece): Set<PiecePos.GridPos> {
        // Get the relative pos of cells to explode for piece facing up
        val cellsToExplode = when (item.type) {
            PieceType.ROCKET -> {
                // Don't explode anything if the rocket can't be found
                val targetId = item.rocketTargetId ?: return setOf()
                val target = findPieceById(targetId)
                target?.position?.let {
                    if (it !is PiecePos.GridPos) return setOf()
                    setOf(ExplosionPos.FixedPos(it.x, it.y))
                } ?: return setOf()
            }
            PieceType.ROCKET_PAD -> {
                setOf(
                    ExplosionPos.RelativePos(-1, -1),
                    ExplosionPos.RelativePos(-1, 0),
                    ExplosionPos.RelativePos(-1, 1),
                    ExplosionPos.RelativePos(0, -1),
                    ExplosionPos.RelativePos(0, 1),
                    ExplosionPos.RelativePos(1, -1),
                    ExplosionPos.RelativePos(1, 0),
                    ExplosionPos.RelativePos(1, 1),
                )
            }
            PieceType.STRAWMAN -> {
                setOf(
                    ExplosionPos.RelativePos(-1, -1),
                    ExplosionPos.RelativePos(-1, 0),
                    ExplosionPos.RelativePos(-1, 1),
                )
            }
            PieceType.TOAD -> {
                setOf(
                    ExplosionPos.RelativePos(-2, 0),
                    ExplosionPos.RelativePos(2, 0),
                    ExplosionPos.RelativePos(0, -2),
                    ExplosionPos.RelativePos(0, 2),
                )
            }
            PieceType.CANNON -> {
                val gridSize = max(GRID_WIDTH, GRID_HEIGHT)
                (-(gridSize - 1)..-1).map {
                    ExplosionPos.RelativePos(it, 0)
                }.toSet()
            }
            PieceType.BOMB -> {
                setOf(
                    ExplosionPos.RelativePos(-1, -1),
                    ExplosionPos.RelativePos(-1, 0),
                    ExplosionPos.RelativePos(-1, 1),
                    ExplosionPos.RelativePos(0, -1),
                    ExplosionPos.RelativePos(0, 1),
                    ExplosionPos.RelativePos(1, -1),
                    ExplosionPos.RelativePos(1, 0),
                    ExplosionPos.RelativePos(1, 1),
                )
            }
            PieceType.PLUS -> {
                setOf(
                    ExplosionPos.RelativePos(-1, 0),
                    ExplosionPos.RelativePos(1, 0),
                    ExplosionPos.RelativePos(0, -1),
                    ExplosionPos.RelativePos(0, 1),
                )
            }
            PieceType.CROSS -> {
                setOf(
                    ExplosionPos.RelativePos(-1, -1),
                    ExplosionPos.RelativePos(-1, 1),
                    ExplosionPos.RelativePos(1, -1),
                    ExplosionPos.RelativePos(1, 1),
                )
            }
            PieceType.SNAKE -> {
                setOf(
                    ExplosionPos.RelativePos(-1, 0),
                    ExplosionPos.RelativePos(1, 0),
                )
            }
        }

        // Convert the relative cell positions to fixed GridPos
        val gridCellsToExplode = cellsToExplode.mapTo(mutableSetOf()) { pos ->
            when (pos) {
                is ExplosionPos.RelativePos -> {
                    val rotatedPos = rotatePos(pos, item.facing)
                    if (item.position !is PiecePos.GridPos) return setOf()
                    PiecePos.GridPos(
                        item.position.x + rotatedPos.x,
                        item.position.y + rotatedPos.y,
                    )
                }
                is ExplosionPos.FixedPos -> PiecePos.GridPos(pos.x, pos.y)
            }
        }

        // Remove any out of bounds position
        return gridCellsToExplode.filterTo(mutableSetOf()){ pos ->
            pos.x in 0..<GRID_HEIGHT
                && pos.y in 0..<GRID_WIDTH
        }
    }

    private fun explodeCell(pos: PiecePos.GridPos) {
        val item = grid[pos.x][pos.y]
        when (item) {

            is GridItem.Piece -> {
                armedPieces.add(item)
            }

            is GridItem.Demon -> {
                val newHealth = item.health - 1
                if (newHealth == 0) {
                    grid[pos.x][pos.y] = null
                } else {
                    grid[pos.x][pos.y] = item.copy(health = newHealth)
                }
            }

            is GridItem.Townie -> {
                grid[pos.x][pos.y] = null
            }

            is GridItem.BossHitbox -> {
                // Find the boss demon
                val boss = grid[item.bossPos.x][item.bossPos.y]
                if (boss !is GridItem.Demon) return

                // Damage the boss demon
                val newHealth = boss.health - 1
                if (newHealth == 0) {
                    grid[item.bossPos.x][item.bossPos.y] = null
                } else {
                    grid[item.bossPos.x][item.bossPos.y] = boss.copy(health = newHealth)
                }
            }

            is GridItem.Hole, null -> return

        }
    }

    private fun findPieceById(id: Int): GridItem.Piece? {
        val foundPieceList = grid.mapNotNull { row ->
            row.find { item ->
                item is GridItem.Piece && item.id == id
            }
        }

        return foundPieceList.firstOrNull() as GridItem.Piece?
    }

    fun placeRandomTownie() {
        val emptySpaces = getEmptySpaces(grid).toMutableSet()
        val pos = emptySpaces.random()
        val townieType = TownieType.entries.random()
        grid[pos.x][pos.y] = GridItem.Townie(type = townieType)
    }

    fun canPlacePieceFromHand(): Boolean = (
        numAvailablePieces > 0
            && unconfirmedPiece == null
            && armedPieces.isEmpty()
    )
}

fun getEmptySpaces(grid: Array<Array<GridItem?>>): List<PiecePos.GridPos> {
    return grid.flatMapIndexed { rowIndex, row ->
        row.mapIndexedNotNull { colIndex, item ->
            if (item == null) {
                PiecePos.GridPos(rowIndex, colIndex)
            } else null
        }
    }
}

fun drawNewPiece(bag: MutableList<PieceType>): PieceType {
    if (bag.isEmpty()) {
        // 1 Tier 1 piece
        bag.add(tierPieces[1]!!.random())

        // 3 Tier 2 pieces
        for (i in 0..<3) {
            bag.add(tierPieces[2]!!.random())
        }

        // 2 Tier 3 pieces
        for (i in 0..<2) {
            bag.add(tierPieces[3]!!.random())
        }

        bag.shuffle()
    }

    return bag.removeAt(bag.lastIndex)
}

fun rotateClockwise(direction: Direction) =
    when (direction) {
        Direction.UP -> Direction.RIGHT
        Direction.RIGHT -> Direction.DOWN
        Direction.DOWN -> Direction.LEFT
        Direction.LEFT -> Direction.UP
    }

// Rotate pos from facing up to the new direction, around (0, 0)
fun rotatePos(
    pos: ExplosionPos.RelativePos,
    direction: Direction
): ExplosionPos.RelativePos = when (direction) {
    Direction.UP -> pos
    Direction.DOWN -> ExplosionPos.RelativePos(-pos.x, -pos.y)
    Direction.LEFT -> ExplosionPos.RelativePos(-pos.y, pos.x)
    Direction.RIGHT -> ExplosionPos.RelativePos(pos.y, -pos.x)
}
