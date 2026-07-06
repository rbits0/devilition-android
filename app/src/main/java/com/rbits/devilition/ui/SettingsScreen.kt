package com.rbits.devilition.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rbits.devilition.LIST_SCREEN_PADDING_DP
import com.rbits.devilition.R
import com.rbits.devilition.TITLE_BOTTOM_PADDING_DP
import com.rbits.devilition.data.Settings
import com.rbits.devilition.ui.theme.DevilitionTheme

@Composable
fun SettingsScreen(
    settings: Settings,
    setTimeBonusEnabled: (Boolean) -> Unit,
    deletePastGames: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var deletePastGamesDialogVisible by remember { mutableStateOf(false) }

    val numListItems = 1
    val listItemColors = ListItemDefaults.colors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
        modifier = modifier.padding(LIST_SCREEN_PADDING_DP.dp),
    ) {
        item {
            Text(
                stringResource(R.string.settings),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(
                    bottom = TITLE_BOTTOM_PADDING_DP.dp - ListItemDefaults.SegmentedGap,
                )
            )
        }

        item {
            SegmentedListItem (
                checked = settings.timeBonusEnabled,
                onCheckedChange = setTimeBonusEnabled,
                shapes = ListItemDefaults.segmentedShapes(0, numListItems),
                colors = listItemColors,
                trailingContent = {
                    Switch(
                        checked = settings.timeBonusEnabled,
                        onCheckedChange = null
                    )
                }
            ) {
                Text(stringResource(R.string.enable_time_bonus))
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            ListItem (
                colors = ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                ),
                onClick = { deletePastGamesDialogVisible = true }
            ) {
                Text(stringResource(R.string.delete_past_games))
            }
        }
    }

    if (deletePastGamesDialogVisible) {
        ConfirmDialog(
            text = stringResource(R.string.confirm_delete_past_games),
            onDismiss = { deletePastGamesDialogVisible = false },
            onConfirm = {
                deletePastGamesDialogVisible = false
                deletePastGames()
            }
        )
    }
}


@Preview()
@Composable
fun SettingsScreenPreview() {
    DevilitionTheme(darkTheme = true) {
        SettingsScreen(
            settings = Settings(),
            setTimeBonusEnabled = {},
            deletePastGames = {},
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
        )
    }
}