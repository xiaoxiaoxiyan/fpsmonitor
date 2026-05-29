package com.xtoolbox.ui.screen.filemanager

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.xtoolbox.core.file.FileItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileManagerScreen(viewModel: FileManagerViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("文件管理", fontWeight = FontWeight.Bold) }
            )
        },
        bottomBar = {
            FileOperationBar(
                onCopy = { /* TODO: copy selected */ },
                onMove = { /* TODO: move selected */ },
                onDelete = { /* TODO: delete selected */ },
                onCompress = { /* TODO: compress selected */ },
                onExtract = { /* TODO: extract selected */ },
                onNewFolder = { /* TODO: create new folder */ },
                onPaste = { viewModel.pasteToActivePane() },
                hasClipboard = uiState.clipboard != null
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                FilePane(
                    path = uiState.leftPath,
                    files = uiState.leftFiles,
                    isActive = uiState.activePane == Pane.LEFT,
                    onFileClick = { file ->
                        if (file.isDirectory) {
                            viewModel.navigateTo(Pane.LEFT, file.path)
                        }
                    },
                    onNavigateUp = { viewModel.navigateUp(Pane.LEFT) },
                    onActivate = { viewModel.setActivePane(Pane.LEFT) },
                    modifier = Modifier.weight(1f)
                )

                Spacer(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxSize()
                )

                FilePane(
                    path = uiState.rightPath,
                    files = uiState.rightFiles,
                    isActive = uiState.activePane == Pane.RIGHT,
                    onFileClick = { file ->
                        if (file.isDirectory) {
                            viewModel.navigateTo(Pane.RIGHT, file.path)
                        }
                    },
                    onNavigateUp = { viewModel.navigateUp(Pane.RIGHT) },
                    onActivate = { viewModel.setActivePane(Pane.RIGHT) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun FilePane(
    path: String,
    files: List<FileItem>,
    isActive: Boolean,
    onFileClick: (FileItem) -> Unit,
    onNavigateUp: () -> Unit,
    onActivate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .clickable { onActivate() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateUp, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Filled.ArrowBack,
                    contentDescription = "上级目录",
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = path,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
                color = if (isActive) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(files, key = { it.path }) { file ->
                FileRow(
                    file = file,
                    onClick = { onFileClick(file) }
                )
            }
        }
    }
}

@Composable
private fun FileRow(file: FileItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (file.isDirectory) Icons.Filled.Folder else Icons.Filled.InsertDriveFile,
            contentDescription = null,
            tint = if (file.isDirectory) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = file.name,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!file.isDirectory) {
                Text(
                    text = formatFileSize(file.size),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun FileOperationBar(
    onCopy: () -> Unit,
    onMove: () -> Unit,
    onDelete: () -> Unit,
    onCompress: () -> Unit,
    onExtract: () -> Unit,
    onNewFolder: () -> Unit,
    onPaste: () -> Unit,
    hasClipboard: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(onClick = onCopy, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Filled.FileCopy, contentDescription = "复制", modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = onMove, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Filled.SwapHoriz, contentDescription = "移动", modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Filled.Delete, contentDescription = "删除", modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = onCompress, modifier = Modifier.size(40.dp)) {
                Text("ZIP", style = MaterialTheme.typography.labelSmall)
            }
            IconButton(onClick = onNewFolder, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Filled.CreateNewFolder, contentDescription = "新建", modifier = Modifier.size(20.dp))
            }
            if (hasClipboard) {
                Button(onClick = onPaste, modifier = Modifier.height(36.dp)) {
                    Text("粘贴", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes >= 1024 * 1024 * 1024 -> "%.1f GB".format(bytes / (1024.0 * 1024 * 1024))
        bytes >= 1024 * 1024 -> "%.1f MB".format(bytes / (1024.0 * 1024))
        bytes >= 1024 -> "%.1f KB".format(bytes / 1024.0)
        else -> "$bytes B"
    }
}
