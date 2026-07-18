package com.wwwescape.photoslideshow.ui.screens.sources

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Deselect
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.SelectAll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.wwwescape.photoslideshow.R
import com.wwwescape.photoslideshow.data.photos.PhotoSource
import com.wwwescape.photoslideshow.data.photos.PhotoSourceType
import com.wwwescape.photoslideshow.ui.components.EmptyStateNotice
import com.wwwescape.photoslideshow.ui.theme.PillShape

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SourcesScreen(
    onConnectGooglePhotos: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SourcesViewModel = viewModel(),
) {
    val sources by viewModel.sources.collectAsStateWithLifecycle()
    val folders = sources.filter { it.type == PhotoSourceType.FOLDER_TREE }
    val photos = sources.filter { it.type == PhotoSourceType.PICKED_ITEM || it.type == PhotoSourceType.GOOGLE_PHOTOS_ITEM }

    var selectedUris by remember { mutableStateOf(emptySet<Uri>()) }
    var showBulkDeleteConfirm by remember { mutableStateOf(false) }
    val isSelectionMode = selectedUris.isNotEmpty()

    // Sources can disappear out from under an active selection (e.g. removed elsewhere), so
    // only ever treat still-existing ones as selected.
    val validSourceUris = remember(sources) { sources.map { it.uri }.toSet() }
    if (selectedUris.any { it !in validSourceUris }) {
        selectedUris = selectedUris.intersect(validSourceUris)
    }

    fun toggleSelection(uri: Uri) {
        selectedUris = if (uri in selectedUris) selectedUris - uri else selectedUris + uri
    }

    val pickPhotosLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = viewModel::addPickedItems,
    )
    val pickFolderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        onResult = viewModel::addFolderTree,
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (isSelectionMode) {
            SelectionToolbar(
                selectedCount = selectedUris.size,
                allSelected = selectedUris.size == sources.size,
                onSelectAllToggle = {
                    selectedUris = if (selectedUris.size == sources.size) emptySet() else validSourceUris
                },
                onCancel = { selectedUris = emptySet() },
                onDeleteClick = { showBulkDeleteConfirm = true },
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        pickPhotosLauncher.launch(
                            PickVisualMediaRequest(mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    },
                    shape = PillShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                ) {
                    Icon(Icons.Rounded.PhotoLibrary, contentDescription = null)
                    Text(stringResource(R.string.action_add_photos), modifier = Modifier.padding(start = 8.dp))
                }
                OutlinedButton(
                    onClick = { pickFolderLauncher.launch(null) },
                    shape = PillShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                ) {
                    Icon(Icons.Rounded.CreateNewFolder, contentDescription = null)
                    Text(stringResource(R.string.action_add_folder), modifier = Modifier.padding(start = 8.dp))
                }
                CloudSourceRow(onConnectGooglePhotos = onConnectGooglePhotos)
            }
        }

        if (sources.isEmpty()) {
            EmptyStateNotice(
                title = stringResource(R.string.sources_empty_title),
                body = stringResource(R.string.sources_empty_body),
                icon = Icons.Rounded.PhotoLibrary,
            )
            return@Column
        }

        if (folders.isNotEmpty()) {
            SectionLabel(stringResource(R.string.section_folders))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                folders.forEach { source ->
                    FolderRow(
                        source = source,
                        isSelectionMode = isSelectionMode,
                        isSelected = source.uri in selectedUris,
                        onToggleSelect = { toggleSelection(source.uri) },
                        onLongPress = { toggleSelection(source.uri) },
                        onRemove = { viewModel.removeSource(source) },
                    )
                }
            }
        }

        if (photos.isNotEmpty()) {
            SectionLabel(stringResource(R.string.section_photos))
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 96.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .size(((photos.size / 3 + 1) * 104).dp),
            ) {
                items(photos, key = { it.uri }) { source ->
                    PhotoThumbnail(
                        source = source,
                        isSelectionMode = isSelectionMode,
                        isSelected = source.uri in selectedUris,
                        onToggleSelect = { toggleSelection(source.uri) },
                        onLongPress = { toggleSelection(source.uri) },
                        onRemove = { viewModel.removeSource(source) },
                    )
                }
            }
        }
    }

    if (showBulkDeleteConfirm) {
        val count = selectedUris.size
        // Default focus lands on Cancel, not the destructive action, so a D-pad/remote user
        // can't delete by reflexively pressing OK when the dialog opens.
        val cancelFocusRequester = remember { FocusRequester() }
        AlertDialog(
            onDismissRequest = { showBulkDeleteConfirm = false },
            title = { Text(pluralStringResource(R.plurals.dialog_remove_sources_title, count, count)) },
            text = { Text(stringResource(R.string.dialog_remove_sources_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        sources.filter { it.uri in selectedUris }.forEach(viewModel::removeSource)
                        selectedUris = emptySet()
                        showBulkDeleteConfirm = false
                    },
                ) {
                    Text(stringResource(R.string.action_remove))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showBulkDeleteConfirm = false },
                    modifier = Modifier.focusRequester(cancelFocusRequester),
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
        LaunchedEffect(Unit) {
            cancelFocusRequester.requestFocus()
        }
    }
}

/** Cloud photo-source picker row — only Google Photos is wired up so far; the others are shown
 * disabled as a preview of what's coming. */
@Composable
private fun CloudSourceRow(onConnectGooglePhotos: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        CloudSourceButton(
            icon = painterResource(R.drawable.ic_google_photos),
            contentDescription = stringResource(R.string.action_connect_google_photos),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            enabled = true,
            onClick = onConnectGooglePhotos,
        )
        CloudSourceButton(
            icon = painterResource(R.drawable.ic_google_drive),
            contentDescription = stringResource(R.string.source_google_drive),
            containerColor = Color(0xFF2684FC),
            contentColor = Color.White,
            enabled = false,
            onClick = {},
        )
        CloudSourceButton(
            icon = painterResource(R.drawable.ic_onedrive),
            contentDescription = stringResource(R.string.source_onedrive),
            containerColor = Color(0xFF0078D4),
            contentColor = Color.White,
            enabled = false,
            onClick = {},
        )
        CloudSourceButton(
            icon = painterResource(R.drawable.ic_dropbox),
            contentDescription = stringResource(R.string.source_dropbox),
            containerColor = Color(0xFF0061FF),
            contentColor = Color.White,
            enabled = false,
            onClick = {},
        )
    }
}

@Composable
private fun CloudSourceButton(
    icon: androidx.compose.ui.graphics.painter.Painter,
    contentDescription: String,
    containerColor: Color,
    contentColor: Color,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    FilledIconButton(
        onClick = onClick,
        enabled = enabled,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        modifier = Modifier.size(56.dp),
    ) {
        Icon(
            painter = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
private fun SelectionToolbar(
    selectedCount: Int,
    allSelected: Boolean,
    onSelectAllToggle: () -> Unit,
    onCancel: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onCancel) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = stringResource(R.string.action_cancel),
                )
            }
            Text(
                text = pluralStringResource(R.plurals.selection_count, selectedCount, selectedCount),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onSelectAllToggle) {
                Icon(
                    imageVector = if (allSelected) Icons.Rounded.Deselect else Icons.Rounded.SelectAll,
                    contentDescription = stringResource(R.string.action_select_all),
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = stringResource(R.string.action_remove),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp),
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FolderRow(
    source: PhotoSource,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onLongPress: () -> Unit,
    onRemove: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { if (isSelectionMode) onToggleSelect() },
                onLongClick = onLongPress,
            ),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            },
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isSelectionMode) {
                    Icon(
                        imageVector = if (isSelected) Icons.Rounded.CheckCircleOutline else Icons.Rounded.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                } else {
                    Icon(Icons.Rounded.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
                Text(
                    text = source.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 12.dp),
                )
            }
            if (!isSelectionMode) {
                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = stringResource(R.string.action_remove),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PhotoThumbnail(
    source: PhotoSource,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onLongPress: () -> Unit,
    onRemove: () -> Unit,
) {
    val badgeSize = 24.dp
    val badgeIconSize = 18.dp
    val removeIconSize = 14.dp

    Box(
        modifier = Modifier
            .size(96.dp)
            .combinedClickable(
                onClick = { if (isSelectionMode) onToggleSelect() },
                onLongClick = onLongPress,
            ),
    ) {
        AsyncImage(
            model = source.uri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .then(
                    if (isSelected) {
                        Modifier.background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.35f))
                    } else {
                        Modifier
                    },
                ),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(badgeSize)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center,
        ) {
            if (isSelectionMode) {
                Icon(
                    imageVector = if (isSelected) Icons.Rounded.CheckCircleOutline else Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(badgeIconSize),
                )
            } else {
                IconButton(onClick = onRemove, modifier = Modifier.size(badgeSize)) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.action_remove),
                        tint = Color.White,
                        modifier = Modifier.size(removeIconSize),
                    )
                }
            }
        }
    }
}
