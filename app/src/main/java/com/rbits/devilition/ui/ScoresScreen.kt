package com.rbits.devilition.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rbits.devilition.R
import com.rbits.devilition.ui.theme.DevilitionTheme

const val SCORES_SCREEN_PADDING_DP = 4
const val SCORES_SCREEN_GAP_DP = 10

@Composable
fun ScoresScreen(
    pastGames: List<GameState>,
    modifier: Modifier = Modifier,
) {
    // TODO: Add setting for time bonus
    val includeTimeBonus = true

    val highscore = remember {
        pastGames
            .filter {it.stage == GameStage.WIN }
            .maxOfOrNull { it.score(includeTimeBonus) }
    }
    val gamesPlayed = pastGames.count()
    val gamesWon = pastGames.count { it.stage == GameStage.WIN }
    val gamesLost = pastGames.count { it.stage == GameStage.LOSE }

    Column(
        modifier = modifier
            .padding(SCORES_SCREEN_PADDING_DP.dp)
    ) {
        Text(
            stringResource(R.string.highscore),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            highscore?.toString() ?: "",
            style = MaterialTheme.typography.bodyLarge,
        )

        Text(
            stringResource(R.string.games_played),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(top = SCORES_SCREEN_GAP_DP.dp),
        )
        Text(
            gamesPlayed.toString(),
            style = MaterialTheme.typography.bodyLarge,
        )

        Text(
            stringResource(R.string.games_won),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(top = SCORES_SCREEN_GAP_DP.dp),
        )
        Text(
            gamesWon.toString(),
            style = MaterialTheme.typography.bodyLarge,
        )

        Text(
            stringResource(R.string.games_lost),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(top = SCORES_SCREEN_GAP_DP.dp),
        )
        Text(
            gamesLost.toString(),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}


private const val previewHeight = 835
private const val previewWidth = 375
@Preview(showBackground = false, widthDp = previewWidth, heightDp = previewHeight)
@Composable
fun ScoresScreenPreview() {
    val previousGames = listOf<GameState>(GameState.new())

    DevilitionTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .size(height = previewHeight.dp, width = previewWidth.dp)
        ) {
            ScoresScreen(pastGames = previousGames)
        }
    }
}