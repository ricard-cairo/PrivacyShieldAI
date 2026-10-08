package com.example.privacyshieldai.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import android.widget.Toast
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.privacyshieldai.R
import com.example.privacyshieldai.data.Mechanism
import com.example.privacyshieldai.domain.PlatformStatus
import com.example.privacyshieldai.util.Links

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlatformDetailScreen(
    item: PlatformItem,
    onBack: () -> Unit,
    onSetDone: (Boolean) -> Unit,
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
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

            if (item.status == PlatformStatus.REVIEW) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ),
                ) {
                    Text(stringResource(R.string.review_warning), modifier = Modifier.padding(12.dp))
                }
            }

            Text(platform.summary, style = MaterialTheme.typography.bodyLarge)

            if (platform.regions.isNotEmpty()) {
                Text(
                    stringResource(R.string.regions_label, platform.regions.joinToString(", ")),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Text(stringResource(R.string.steps_title), style = MaterialTheme.typography.titleMedium)
            platform.steps.forEachIndexed { index, step ->
                Text("${index + 1}. $step", style = MaterialTheme.typography.bodyMedium)
            }

            platform.notes?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Acción principal según el mecanismo.
            // Los formularios web (FORM) no tienen botón: el usuario llega a ellos desde la propia app siguiendo los pasos.
            val settingUrl = platform.url
            if (settingUrl != null && platform.mechanism == Mechanism.TOGGLE) {
                Button(
                    onClick = { Links.openPreferApp(context, settingUrl) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(R.string.action_open_setting)) }
                if (!platform.linkVerified) {
                    Text(
                        stringResource(R.string.link_unverified),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            platform.email?.let { to ->
                Button(
                    onClick = { Links.sendEmail(context, to, emailSubject, emailBody) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(R.string.action_send_request)) }
            }

            // Marcar estado
            if (item.status == PlatformStatus.DONE) {
                OutlinedButton(onClick = { onSetDone(false) }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.action_mark_pending))
                }
            } else {
                Button(onClick = { onSetDone(true) }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.action_mark_done))
                }
            }

            Text(
                stringResource(R.string.updated_on, platform.updatedOn),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            platform.sourceUrl?.let { url ->
                TextButton(onClick = { Links.openInCustomTab(context, url) }) {
                    Text(stringResource(R.string.action_view_source))
                }
            }
        }
    }
}
