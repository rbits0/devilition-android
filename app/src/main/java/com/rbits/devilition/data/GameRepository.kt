package com.rbits.devilition.data

import android.util.Log
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.core.Serializer
import com.rbits.devilition.TAG
import com.rbits.devilition.ui.GameState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

class GameRepository(
    private val gameStore: DataStore<GameState>,
    private val pastGamesStore: DataStore<List<GameState>>,
) {

    val gameFlow = gameStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading gameState")
                emit(GameState())
            } else {
                throw exception
            }
        }

    val pastGamesFlow = pastGamesStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading past games")
                emit(listOf())
            } else {
                throw exception
            }
        }

    suspend fun updateState(state: GameState) {
        gameStore.updateData { state }
    }

    suspend fun addPastGame(gameState: GameState) {
        pastGamesStore.updateData { pastGames ->
            pastGames + gameState
        }
    }
}

object GameStateSerializer : Serializer<GameState> {
    override val defaultValue = GameState.new()

    override suspend fun readFrom(input: InputStream) =
        try {
            Json.decodeFromString<GameState>(
                input.readBytes().decodeToString()
            )
        } catch (serialization: SerializationException) {
            throw CorruptionException("Unable to read GameState", serialization)
        }

    override suspend fun writeTo(t: GameState, output: OutputStream) {
        withContext(Dispatchers.IO) {
            output.write(
                Json.encodeToString(t)
                    .encodeToByteArray()
            )
        }
    }
}

object PastGamesSerializer : Serializer<List<GameState>> {
    override val defaultValue: List<GameState> = listOf()

    override suspend fun readFrom(input: InputStream) =
        try {
            Json.decodeFromString<List<GameState>>(
                input.readBytes().decodeToString()
            )
        } catch (serialization: SerializationException) {
            throw CorruptionException("Unable to read past games", serialization)
        }

    override suspend fun writeTo(t: List<GameState>, output: OutputStream) {
        withContext(Dispatchers.IO) {
            output.write(
                Json.encodeToString(t)
                    .encodeToByteArray()
            )
        }
    }
}
