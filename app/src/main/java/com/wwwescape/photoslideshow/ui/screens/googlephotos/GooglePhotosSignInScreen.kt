package com.wwwescape.photoslideshow.ui.screens.googlephotos

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wwwescape.photoslideshow.R
import com.wwwescape.photoslideshow.ui.googlephotos.GooglePhotosSignInUiState
import com.wwwescape.photoslideshow.ui.googlephotos.GooglePhotosSignInViewModel
import com.wwwescape.photoslideshow.ui.theme.PillShape
import com.wwwescape.photoslideshow.util.generateQrCodeBitmap
import com.wwwescape.photoslideshow.util.openUrl

private val ContentMaxWidth = 400.dp

/** Renders the Google Photos connect flow driven by [GooglePhotosSignInViewModel]. */
@Composable
fun GooglePhotosSignInScreen(
    modifier: Modifier = Modifier,
    viewModel: GooglePhotosSignInViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val authorizationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = { result -> viewModel.onAuthorizationActivityResult(result.data) },
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when (val state = uiState) {
            GooglePhotosSignInUiState.Idle -> {
                Button(
                    onClick = viewModel::start,
                    shape = PillShape,
                    modifier = Modifier.widthIn(max = ContentMaxWidth).fillMaxWidth().height(56.dp),
                ) {
                    Text(stringResource(R.string.action_connect_google_photos))
                }
            }

            GooglePhotosSignInUiState.RequestingAuthorization -> LoadingRow(stringResource(R.string.google_photos_step_requesting_authorization))

            is GooglePhotosSignInUiState.NeedsAuthorization -> {
                LaunchedEffect(state.pendingIntent) {
                    authorizationLauncher.launch(IntentSenderRequest.Builder(state.pendingIntent.intentSender).build())
                }
                LoadingRow(stringResource(R.string.google_photos_step_requesting_authorization))
            }

            GooglePhotosSignInUiState.CreatingPickerSession -> LoadingRow(stringResource(R.string.google_photos_step_creating_session))

            is GooglePhotosSignInUiState.AwaitingPickerSelection -> PickerCodeStep(verificationUrl = state.pickerUri)

            is GooglePhotosSignInUiState.ReadyToImport -> {
                Text(
                    text = pluralStringResource(R.plurals.google_photos_ready_to_import, state.items.size, state.items.size),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Button(
                    onClick = viewModel::confirmImport,
                    shape = PillShape,
                    modifier = Modifier.widthIn(max = ContentMaxWidth).fillMaxWidth().height(56.dp).padding(top = 16.dp),
                ) {
                    Text(stringResource(R.string.action_import))
                }
            }

            GooglePhotosSignInUiState.Importing -> LoadingRow(stringResource(R.string.google_photos_step_importing))

            is GooglePhotosSignInUiState.Imported -> {
                Text(
                    text = pluralStringResource(R.plurals.google_photos_imported, state.count, state.count),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Button(
                    onClick = viewModel::reset,
                    shape = PillShape,
                    modifier = Modifier.widthIn(max = ContentMaxWidth).fillMaxWidth().height(56.dp).padding(top = 16.dp),
                ) {
                    Text(stringResource(R.string.action_done))
                }
            }

            is GooglePhotosSignInUiState.Failed -> {
                Text(
                    text = state.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                )
                Button(
                    onClick = viewModel::reset,
                    shape = PillShape,
                    modifier = Modifier.widthIn(max = ContentMaxWidth).fillMaxWidth().height(56.dp).padding(top = 16.dp),
                ) {
                    Text(stringResource(R.string.action_try_again))
                }
            }
        }
    }
}

@Composable
private fun LoadingRow(label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        CircularProgressIndicator()
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun PickerCodeStep(verificationUrl: String) {
    val context = LocalContext.current
    val qrBitmap = remember(verificationUrl) { generateQrCodeBitmap(verificationUrl) }

    Column(
        modifier = Modifier.widthIn(max = ContentMaxWidth),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = stringResource(R.string.google_photos_picker_title),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.google_photos_picker_body_phone),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Button(
            onClick = { openUrl(context, verificationUrl) },
            shape = PillShape,
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) {
            Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null)
            Text(stringResource(R.string.action_open_in_browser), modifier = Modifier.padding(start = 8.dp))
        }

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.label_or),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }

        Card(
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(containerColor = Color.White),
        ) {
            Image(
                bitmap = qrBitmap,
                contentDescription = null,
                modifier = Modifier
                    .padding(16.dp)
                    .size(140.dp),
            )
        }
        Text(
            text = stringResource(R.string.google_photos_scan_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        LoadingRow(stringResource(R.string.google_photos_step_waiting_status))
    }
}
