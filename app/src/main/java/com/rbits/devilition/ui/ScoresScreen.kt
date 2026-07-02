package com.rbits.devilition.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rbits.devilition.LIST_SCREEN_PADDING_DP
import com.rbits.devilition.R
import com.rbits.devilition.ui.theme.DevilitionTheme

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

    val numListItems = 4
    val listItemColors = ListItemDefaults.colors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
        modifier = modifier
            .padding(LIST_SCREEN_PADDING_DP.dp)
    ) {
        item {
            SegmentedListItem (
                shapes = ListItemDefaults.segmentedShapes(0, numListItems),
                colors = listItemColors,
                trailingContent = {
                    Text(
                        highscore?.toString() ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                },
            ) {
                Text(stringResource(R.string.highscore))
            }
        }

        item {
            SegmentedListItem(
                shapes = ListItemDefaults.segmentedShapes(1, numListItems),
                colors = listItemColors,
                trailingContent = {
                    Text(
                        gamesPlayed.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            ) {
                Text(stringResource(R.string.games_played))
            }
        }

        item {
            SegmentedListItem(
                shapes = ListItemDefaults.segmentedShapes(2, numListItems),
                colors = listItemColors,
                trailingContent = {
                    Text(
                        gamesWon.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            ) {
                Text(stringResource(R.string.games_won))
            }
        }

        item {
            SegmentedListItem(
                shapes = ListItemDefaults.segmentedShapes(3, numListItems),
                colors = listItemColors,
                trailingContent = {
                    Text(
                        gamesLost.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            ) {
                Text(stringResource(R.string.games_lost))
            }
        }
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