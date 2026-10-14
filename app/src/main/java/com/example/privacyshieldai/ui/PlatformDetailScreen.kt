package com.example.privacyshieldai.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.privacyshieldai.R
import com.example.privacyshieldai.data.FeatureMechanism
import com.example.privacyshieldai.data.Mechanism
import com.example.privacyshieldai.domain.PlatformStatus
import com.example.privacyshieldai.domain.Task
import com.example.privacyshieldai.util.Links

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlatformDetailScreen(
    item: PlatformItem,
    onBack: () -> Unit,
    onSetDone: (Task, Boolean) -> Unit,
) {
    val platform = item.platform
    val context = LocalContext.current
    val emailSubject = stringResource(R.string.gdpr_email_subject)
    val emailBody = stringResource(R.string.gdpr_email_body)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(platform.name) },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text(stringResource(R.string.action_back)) }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Cabecera
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PlatformIcon(item, size = 48.dp)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    StatusBadge(item.status)
                    Text(
                        text = if (item.isInstalled) {
                            stringResource(R.string.installed_label, platform.category)
                        } else {
                            platform.category
                        },
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }

            item.installedPackage?.let { pkg ->
                OutlinedButton(
                    onClick = {
                        if (!Links.openApp(context, pkg)) {
                            Toast.makeText(context, R.string.error_open_app, Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(R.string.action_open_app, platform.name)) }
            }

            // Tarea 1: que no usen tus datos para entrenar IA
            TaskSection(
                title = stringResource(R.string.task_training_title),
                subtitle = null,
                status = item.trainingStatus,
                summary = platform.summary,
                regions = platform.regions,
                steps = platform.steps,
                notes = platform.notes,
                updatedOn = platform.updatedOn,
                sourceUrl = platform.sourceUrl,
                onSetDone = { onSetDone(Task.TRAINING, it) },
            ) {
                // Los formularios web (FORM) no tienen botón: el usuario llega desde la propia app siguiendo los pasos.
                val settingUrl = platform.url
                if (settingUrl != null && platform.mechanism == Mechanism.TOGGLE) {
                    SettingButton(url = settingUrl, linkVerified = platform.linkVerified)
                }
                platform.email?.let { to ->
                    Button(
                        onClick = { Links.sendEmail(context, to, emailSubject, emailBody) },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(stringResource(R.string.action_send_request)) }
                }
            }

            // Tarea 2: desactivar u ocultar las funciones de IA
            val features = platform.aiFeatures
            val featuresStatus = item.featuresStatus
            if (features != null && featuresStatus != null) {
                TaskSection(
                    title = stringResource(R.string.task_features_title),
                    subtitle = stringResource(
                        when (features.mechanism) {
                            FeatureMechanism.TOGGLE -> R.string.features_toggle
                            FeatureMechanism.PARTIAL -> R.string.features_partial
                            FeatureMechanism.NONE -> R.string.features_none
                        }
                    ),
                    status = featuresStatus,
                    summary = features.summary,
                    regions = emptyList(),
                    steps = features.steps,
                    notes = features.notes,
                    updatedOn = features.updatedOn,
                    sourceUrl = features.sourceUrl,
                    onSetDone = { onSetDone(Task.FEATURES, it) },
                ) {
                    val url = features.url
                    if (url != null) { // también en NONE: p. ej. X permite borrar el historial de Grok
                        SettingButton(url = url, linkVerified = features.linkVerified)
                    }
                }
            }
        }
    }
}

/** Bloque de una tarea: estado, explicación, pasos, botones de acción y marcar como hecha. */
@Composable
private fun TaskSection(
    title: String,
    subtitle: String?,
    status: PlatformStatus,
    summary: String,
    regions: List<String>,
    steps: List<String>,
    notes: String?,
    updatedOn: String,
    sourceUrl: String?,
    onSetDone: (Boolean) -> Unit,
    actions: @Composable ColumnScope.() -> Unit,
) {
    val context = LocalContext.current
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
                StatusBadge(status)
            }
            subtitle?.let {
                Text(it, style = MaterialTheme.typography.labelLarge)
            }

            if (status == PlatformStatus.REVIEW) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ),
                ) {
                    Text(stringResource(R.string.review_warning), modifier = Modifier.padding(12.dp))
                }
            }

            Text(summary, style = MaterialTheme.typography.bodyLarge)

            if (regions.isNotEmpty()) {
                Text(
                    stringResource(R.string.regions_label, regions.joinToString(", ")),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Text(stringResource(R.string.steps_title), style = MaterialTheme.typography.titleSmall)
            steps.forEachIndexed { index, step ->
                Text("${index + 1}. $step", style = MaterialTheme.typography.bodyMedium)
            }

            notes?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            actions()

            if (status == PlatformStatus.DONE) {
                OutlinedButton(onClick = { onSetDone(false) }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.action_mark_pending))
                }
            } else {
                Button(onClick = { onSetDone(true) }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.action_mark_done))
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.updated_on, updatedOn),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                sourceUrl?.let { url ->
                    TextButton(onClick = { Links.openInCustomTab(context, url) }) {
                        Text(stringResource(R.string.action_view_source))
                    }
                }
            }
        }
    }
}

/** Botón "Abrir el ajuste" con aviso si el enlace no está verificado. */
@Composable
private fun SettingButton(url: String, linkVerified: Boolean) {
    val context = LocalContext.current
    Button(
        onClick = { Links.openPreferApp(context, url) },
        modifier = Modifier.fillMaxWidth(),
    ) { Text(stringResource(R.string.action_open_setting)) }
    if (!linkVerified) {
        Text(
            stringResource(R.string.link_unverified),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
