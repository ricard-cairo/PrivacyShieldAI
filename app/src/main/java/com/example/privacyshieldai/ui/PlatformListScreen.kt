package com.example.privacyshieldai.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.privacyshieldai.R
import com.example.privacyshieldai.domain.PlatformStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlatformListScreen(
    state: UiState,
    onOpenPlatform: (String) -> Unit,
    onRefresh: () -> Unit,
    onMessageShown: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val messageText = state.message?.let { stringResource(it.textRes()) }

    LaunchedEffect(state.message) {
        if (messageText != null) {
            snackbarHostState.showSnackbar(messageText)
            onMessageShown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    TextButton(onClick = onRefresh, enabled = !state.refreshing) {
                        Text(stringResource(R.string.action_refresh))
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        if (state.loading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            item { ProgressHeader(state) }

            if (state.installedItems.isNotEmpty()) {
                item(key = "header_installed") { SectionHeader(stringResource(R.string.section_installed)) }
                items(state.installedItems, key = { it.platform.id }) { item ->
                    PlatformRow(item, onOpenPlatform)
                }
            }
            if (state.otherItems.isNotEmpty()) {
                item(key = "header_other") { SectionHeader(stringResource(R.string.section_other)) }
                items(state.otherItems, key = { it.platform.id }) { item ->
                    PlatformRow(item, onOpenPlatform)
                }
            }
            item {
                state.dataVersion?.let {
                    Text(
                        text = stringResource(R.string.catalog_version, it),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
    )
}

@Composable
private fun PlatformRow(item: PlatformItem, onOpenPlatform: (String) -> Unit) {
    ListItem(
        leadingContent = { PlatformIcon(item) },
        headlineContent = { Text(item.platform.name) },
        supportingContent = {
            Column {
                Text(item.platform.category)
                Text(
                    text = taskSummary(item),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        trailingContent = { StatusBadge(item.status) },
        modifier = Modifier.clickable { onOpenPlatform(item.platform.id) },
    )
    HorizontalDivider()
}

@Composable
private fun ProgressHeader(state: UiState) {
    val total = state.totalTasks
    val done = state.doneTasks
    Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.progress_title, done, total),
                style = MaterialTheme.typography.titleMedium,
            )
            LinearProgressIndicator(
                progress = { if (total == 0) 0f else done.toFloat() / total },
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = stringResource(
                    R.string.progress_breakdown,
                    state.trainingDone,
                    state.items.size,
                    state.featuresDone,
                    state.featuresTotal,
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = stringResource(R.string.progress_hint),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

/** "Entrenamiento ✓ · Funciones IA •" para ver de un vistazo el estado de cada tarea. */
@Composable
private fun taskSummary(item: PlatformItem): String {
    val training = stringResource(R.string.task_training_short, item.trainingStatus.symbol())
    val features = item.featuresStatus?.let { stringResource(R.string.task_features_short, it.symbol()) }
    return listOfNotNull(training, features).joinToString(" · ")
}

private fun PlatformStatus.symbol(): String = when (this) {
    PlatformStatus.DONE -> "✓"
    PlatformStatus.PENDING -> "•"
    PlatformStatus.REVIEW -> "!"
}

private fun UserMessage.textRes(): Int = when (this) {
    UserMessage.UPDATED -> R.string.msg_updated
    UserMessage.UP_TO_DATE -> R.string.msg_up_to_date
    UserMessage.FAILED -> R.string.msg_failed
    UserMessage.NOT_CONFIGURED -> R.string.msg_not_configured
}
