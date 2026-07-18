package com.wwwescape.photoslideshow.ui.screens.home

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.wwwescape.photoslideshow.R
import com.wwwescape.photoslideshow.ui.components.EmptyStateNotice
import com.wwwescape.photoslideshow.ui.slideshow.SlideshowActivity
import com.wwwescape.photoslideshow.ui.theme.PillShape

@Composable
fun HomeScreen(
    onManageSources: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.home_hero_tagline),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (uiState.sourceCount == 0) {
            EmptyStateNotice(
                title = stringResource(R.string.home_empty_title),
                body = stringResource(R.string.home_empty_body),
                icon = Icons.Rounded.PhotoLibrary,
            )
        } else {
            Text(
                text = pluralStringResource(R.plurals.home_source_count, uiState.sourceCount, uiState.sourceCount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (uiState.previewUris.isNotEmpty()) {
                PreviewRow(uris = uiState.previewUris)
            }
        }

        Button(
            onClick = { context.startActivity(Intent(context, SlideshowActivity::class.java)) },
            enabled = uiState.sourceCount > 0,
            shape = PillShape,
            colors = ButtonDefaults.buttonColors(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        ) {
            Icon(Icons.Rounded.PlayArrow, contentDescription = null)
            Text(
                text = stringResource(R.string.action_start_slideshow),
                modifier = Modifier.padding(start = 8.dp),
            )
        }

        OutlinedButton(
            onClick = onManageSources,
            shape = PillShape,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        ) {
            Icon(Icons.Rounded.PhotoLibrary, contentDescription = null)
            Text(
                text = stringResource(R.string.action_manage_sources),
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
private fun PreviewRow(uris: List<android.net.Uri>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 4.dp),
    ) {
        items(uris) { uri ->
            AsyncImage(
                model = uri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(16.dp)),
            )
        }
    }
}
