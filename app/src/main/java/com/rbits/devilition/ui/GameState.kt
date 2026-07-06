package com.rbits.devilition.ui

import android.util.Log
import com.rbits.devilition.TAG
import com.rbits.devilition.data.GRID_HEIGHT
import com.rbits.devilition.data.GRID_WIDTH
import com.rbits.devilition.data.HAND_SIZE
import com.rbits.devilition.data.NUM_ROUNDS
import com.rbits.devilition.data.NUM_STARTING_TOWNIES
import com.rbits.devilition.data.demonTypeHealth
import com.rbits.devilition.data.demonsPerRound
import com.rbits.devilition.data.piecesPerRound
import com.rbits.devilition.data.tierPieces
import kotlinx.serialization.Serializable
import java.util.Locale
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.math.max
import kotlin.math.round
import kotlin.math.roundToInt

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

enum class GameStage {
    NOT_LOADED,
    ROUND_START,
    PLACING_PIECES,
    DETONATION,
    ROUND_END,
    WIN,
    LOSE,
}

@Serializable
sealed class PiecePos {
    @Serializable
    data class GridPos(val x: Int, val y: Int) : PiecePos()
    @Serializable
    data class HandPos(val pos: Int) : PiecePos()
}

@Serializable
sealed class ExplosionPos {
    @Serializable
    data class RelativePos(val x: Int, val y: Int) : ExplosionPos()
    @Serializable
    data class FixedPos(val x: Int, val y: Int) : ExplosionPos()
}

// Item with a sprite
sealed interface SpriteItem

@Serializable
sealed class GridItem {

    @Serializable
    data class Demon(
        val demonType: DemonType,
        val health: Int,
        val maxHealth: Int,
    ) : GridItem(), SpriteItem

    // Boss takes up a 2x2 space
    // If this item is hit, it should damage the boss
    @Serializable
    data class BossHitbox(
        val bossPos: PiecePos.GridPos,
    ) : GridItem()

    @Serializable
    data class Piece(
        val pieceType: PieceType,
        val facing: Direction,
        val id: Int,
        val placementConfirmed: Boolean = true,
        val position: PiecePos? = null,
        val color: RocketColor? = null,
        var rocketTargetId: Int? = null,
    ) : GridItem(), SpriteItem

    @Serializable
    data class Townie(
        val townieType: TownieType
    ) : GridItem(), SpriteItem

    @Serializable
    class Hole() : GridItem()
}

@Serializable
data class Explosion(
    val id: Int,
) : SpriteItem



@Serializable
data class GameState(
    var grid: Array<Array<GridItem?>> = Array(GRID_HEIGHT) { Array(GRID_WIDTH) { null } },
    var hand: Array<GridItem.Piece?> = Array(HAND_SIZE) { null },
    var bag: List<PieceType> = listOf(),
    var numAvailablePieces: Int = 0,
    var round: Int = 0,
    var idCounter: Int = 0,
    var rocketColor: RocketColor = RocketColor.PINK,
    // Position of the piece that has been dragged onto the board,
    // but hasn't had placement confirmed
    var unconfirmedPiece: GridItem.Piece? = null,
    var armedPieces: MutableSet<GridItem.Piece> = mutableSetOf(),
    // stage refers to what actions are about to happen or are happening.
    var stage: GameStage = GameStage.NOT_LOADED,
    var seconds: Int = 0,
    var explosions: MutableMap<PiecePos.GridPos, Explosion> = mutableMapOf(),
) {

    companion object {
        fun new(): GameState {
            val state = GameState()

            // Start with `HAND_SIZE` pieces in hand
            val bag: MutableList<PieceType> = mutableListOf()
            for (i in 0..<HAND_SIZE) {
                val type = drawNewPiece(bag)
                val piece = state.createPieceOfType(type, PiecePos.HandPos(i))
                state.hand[i] = piece
            }
            state.bag = bag


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

        other as GameState

        if (numAvailablePieces != other.numAvailablePieces) return false
        if (round != other.round) return false
        if (idCounter != other.idCounter) return false
        if (!grid.contentDeepEquals(other.grid)) return false
        if (!hand.contentEquals(other.hand)) return false
        if (bag != other.bag) return false
        if (rocketColor != other.rocketColor) return false
        if (unconfirmedPiece != other.unconfirmedPiece) return false
        if (armedPieces != other.armedPieces) return false
        if (stage != other.stage) return false
        if (seconds != other.seconds) return false

        return true
    }
    // Auto-generated function to handle the array
    override fun hashCode(): Int {
        var result = numAvailablePieces
        result = 31 * result + round
        result = 31 * result + idCounter
        result = 31 * result + grid.contentDeepHashCode()
        result = 31 * result + hand.contentHashCode()
        result = 31 * result + bag.hashCode()
        result = 31 * result + rocketColor.hashCode()
        result = 31 * result + (unconfirmedPiece?.hashCode() ?: 0)
        result = 31 * result + armedPieces.hashCode()
        result = 31 * result + stage.hashCode()
        result = 31 * result + seconds
        return result
    }


    fun clone(): GameState = this.copy(
        grid = grid.map{ it.clone() }.toTypedArray(),
        hand = hand.clone(),
    )

    fun roundStart() {
        round += 1
        numAvailablePieces += piecesPerRound(round)

        if (round < NUM_ROUNDS) {
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
            // All spots from (2, 3) to (5, 6)
            for (rowIndex in 2..5) {
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

        stage = GameStage.PLACING_PIECES
    }

    // Heal all elder demons
    fun healDemons() {
        grid.forEachIndexed { rowIndex, row ->
            row.forEachIndexed { colIndex, item ->
                if (item is GridItem.Demon && item.demonType == DemonType.ELDER) {
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
                    demonType = demonType,
                    health = health,
                    maxHealth = health,
                )
            }
        }
    }

    // Place boss in middle
    // Boss takes up a 2x2 space
    fun placeBoss() {
        val bossPos = PiecePos.GridPos(3, 4)

        grid[bossPos.x][bossPos.y] = GridItem.Demon(
            demonType = DemonType.BOSS,
            health = demonTypeHealth(DemonType.BOSS),
            maxHealth = demonTypeHealth(DemonType.BOSS),
        )
        for (pos in listOf(
            PiecePos.GridPos(3, 5),
            PiecePos.GridPos(4, 4),
            PiecePos.GridPos(4, 5),
        )) {
            grid[pos.x][pos.y] = GridItem.BossHitbox(bossPos)
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
                val newItem = item.copy(
                    facing = rotateClockwise(item.facing)
                )
                grid[pos.x][pos.y] = newItem

                if (unconfirmedPiece == item) {
                    unconfirmedPiece = newItem
                }
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
        numAvailablePieces -= 1

        if (unconfirmedPiece.pieceType == PieceType.ROCKET) {
            // Mark piece as confirmed
            grid[position.x][position.y] = unconfirmedPiece.copy(
                rocketTargetId = idCounter,
                placementConfirmed = true,
            )

            // Replace rocket with pad instead of drawing from bag
            hand[newPiecePos.pos] = GridItem.Piece(
                pieceType = PieceType.ROCKET_PAD,
                facing = Direction.DOWN,
                position = newPiecePos,
                id = idCounter,
                color = unconfirmedPiece.color,
            )

            idCounter += 1
        } else {
            // Mark piece as confirmed
            grid[position.x][position.y] = unconfirmedPiece.copy(
                placementConfirmed = true,
            )

            // Draw piece from bag to replace moved piece
            val newBag = bag.toMutableList()
            val type = drawNewPiece(newBag)
            hand[newPiecePos.pos] = createPieceOfType(type, newPiecePos)
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

        // Set stage to detonation
        // It might already be set, doesn't matter
        stage = GameStage.DETONATION
    }

    fun runDetonationStep() {
        val armedPieces = armedPieces.toSet()
        this.armedPieces.clear()
        explosions.clear()

        if (armedPieces.isEmpty()) {
            stage = GameStage.ROUND_END
            return
        }

        for (item in armedPieces) {
            detonatePiece(item)
        }
    }

    fun roundEnd() {
        val numDemons = grid.flatten().count { it is GridItem.Demon }
        val numTownies = grid.flatten().count { it is GridItem.Townie }

        if (numDemons > numTownies) {
            stage = GameStage.LOSE
            return
        }

        if (round == NUM_ROUNDS) {
            // There can't be any demons left after the final round
            stage = if (numDemons == 0) {
                GameStage.WIN
            } else {
                GameStage.LOSE
            }

            return
        }

        if (numDemons == 0) {
            placeRandomTownie()
        }

        stage = GameStage.ROUND_START
    }

    fun score(includeTimeBonus: Boolean = true): Int {
        val numPiecesAndTownies = grid.flatten().count {
            it is GridItem.Piece || it is GridItem.Townie
        }
        val leftoverPieceBonus = (numPiecesAndTownies + numAvailablePieces) * 1_000
        val minutes = (seconds / 60f).roundToInt()
        val timeBonus = max(15_000 - (250 * minutes), 0)

        var score = 10_000 + leftoverPieceBonus
        if (includeTimeBonus) {
            score += timeBonus
        }

        return score
    }

    private fun detonatePiece(item: GridItem.Piece) {
        if (item.position !is PiecePos.GridPos) {
            Log.e(TAG, "Can't detonate piece: Invalid position")
            return
        }

        grid[item.position.x][item.position.y] = null
        explosions[item.position] = Explosion(idCounter)
        idCounter++

        val cellsToExplode = getPieceTargetCells(item)

        for (pos in cellsToExplode) {
            explodeCell(pos)
        }
    }

    fun getPieceTargetCells(item: GridItem.Piece): Set<PiecePos.GridPos> {
        // Get the relative pos of cells to explode for piece facing up
        val cellsToExplode = when (item.pieceType) {
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
        var gridCellsToExplode = cellsToExplode.mapTo(mutableSetOf()) { pos ->
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
        gridCellsToExplode = gridCellsToExplode.filterTo(mutableSetOf()){ pos ->
            pos.x in 0..<GRID_HEIGHT
                && pos.y in 0..<GRID_WIDTH
        }

        // Target Boss instead of BossHitbox
        val bossHitboxes = gridCellsToExplode
            .filter { pos -> grid[pos.x][pos.y] is GridItem.BossHitbox }
            .toSet()
        if (bossHitboxes.isNotEmpty()) {
            val bossPos = (
                grid[bossHitboxes.first().x][bossHitboxes.first().y] as GridItem.BossHitbox
            ).bossPos
            gridCellsToExplode.add(bossPos)
        }
        gridCellsToExplode.removeAll(bossHitboxes)

        Log.i(TAG, "$gridCellsToExplode")
        return gridCellsToExplode
    }

    private fun explodeCell(pos: PiecePos.GridPos) {
        val item = grid[pos.x][pos.y]
        when (item) {

            is GridItem.Piece -> {
                armedPieces.add(item)
            }

            is GridItem.Demon -> {
                val newHealth = item.health - 1
                explosions[pos] = Explosion(idCounter)
                idCounter++

                if (newHealth == 0) {
                    grid[pos.x][pos.y] = null
                } else {
                    grid[pos.x][pos.y] = item.copy(health = newHealth)
                }
            }

            is GridItem.Townie -> {
                grid[pos.x][pos.y] = null
                explosions[pos] = Explosion(idCounter)
                idCounter++
            }

            is GridItem.BossHitbox -> {
                // Find the boss demon
                val boss = grid[item.bossPos.x][item.bossPos.y]
                if (boss !is GridItem.Demon) return

                // Damage the boss demon
                val newHealth = boss.health - 1
                explosions[pos] = Explosion(idCounter)
                idCounter++

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
        grid[pos.x][pos.y] = GridItem.Townie(townieType = townieType)
    }

    fun createPieceOfType(type: PieceType, position: PiecePos): GridItem.Piece {
        var piece: GridItem.Piece

        if (type == PieceType.ROCKET) {
            piece = GridItem.Piece(
                pieceType = type,
                facing = Direction.DOWN,
                id = idCounter,
                position = position,
                color = rocketColor,
            )

            rocketColor = when (rocketColor) {
                RocketColor.PINK -> RocketColor.BLUE
                RocketColor.BLUE -> RocketColor.PINK
            }
        } else {
            piece = GridItem.Piece(
                pieceType = type,
                facing = Direction.DOWN,
                id = idCounter,
                position = position,
            )
        }

        idCounter += 1
        return piece
    }

    fun canPlacePieceFromHand(): Boolean = (
        numAvailablePieces > 0
            && unconfirmedPiece == null
            && stage == GameStage.PLACING_PIECES
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

fun formatSeconds(seconds: Int): String = String.format(
    Locale.ENGLISH,
    "%02d:%02d",
    seconds / 60,
    seconds % 60,
)