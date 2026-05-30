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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.xtoolbox.core.file.ApkSigner
import com.xtoolbox.core.file.FileItem
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.theme.MiuixTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text

@Composable
fun FileManagerScreen(viewModel: FileManagerViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedFiles by remember { mutableStateOf(setOf<String>()) }

    Column(
        modifier = Modifier.fillMaxSize()
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
                selectedFiles = selectedFiles,
                onFileClick = { file ->
                    if (file.isDirectory) {
                        viewModel.navigateTo(Pane.LEFT, file.path)
                    } else {
                        selectedFiles = if (file.path in selectedFiles) {
                            selectedFiles - file.path
                        } else {
                            selectedFiles + file.path
                        }
                    }
                },
                onFileLongClick = { file ->
                    selectedFiles = if (file.path in selectedFiles) {
                        selectedFiles - file.path
                    } else {
                        selectedFiles + file.path
                    }
                },
                onNavigateUp = { viewModel.navigateUp(Pane.LEFT) },
                onActivate = {
                    viewModel.setActivePane(Pane.LEFT)
                    selectedFiles = emptySet()
                },
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
                selectedFiles = selectedFiles,
                onFileClick = { file ->
                    if (file.isDirectory) {
                        viewModel.navigateTo(Pane.RIGHT, file.path)
                    } else {
                        selectedFiles = if (file.path in selectedFiles) {
                            selectedFiles - file.path
                        } else {
                            selectedFiles + file.path
                        }
                    }
                },
                onFileLongClick = { file ->
                    selectedFiles = if (file.path in selectedFiles) {
                        selectedFiles - file.path
                    } else {
                        selectedFiles + file.path
                    }
                },
                onNavigateUp = { viewModel.navigateUp(Pane.RIGHT) },
                onActivate = {
                    viewModel.setActivePane(Pane.RIGHT)
                    selectedFiles = emptySet()
                },
                modifier = Modifier.weight(1f)
            )
        }

        FileOperationBar(
            onCopy = {
                if (selectedFiles.isNotEmpty()) {
                    viewModel.copyFiles(selectedFiles.toList())
                    selectedFiles = emptySet()
                }
            },
            onMove = {
                if (selectedFiles.isNotEmpty()) {
                    viewModel.moveFiles(selectedFiles.toList())
                    selectedFiles = emptySet()
                }
            },
            onDelete = {
                if (selectedFiles.isNotEmpty()) {
                    viewModel.deleteFiles(selectedFiles.toList())
                    selectedFiles = emptySet()
                }
            },
            onCompress = {
                if (selectedFiles.isNotEmpty()) {
                    val activePath = if (uiState.activePane == Pane.LEFT) uiState.leftPath else uiState.rightPath
                    viewModel.compressFiles(selectedFiles.toList(), "$activePath/archive.zip")
                    selectedFiles = emptySet()
                }
            },
            onExtract = {
                val zipFiles = selectedFiles.filter {
                    it.lowercase().endsWith(".zip") || it.lowercase().endsWith(".tar.gz") || it.lowercase().endsWith(".tar")
                }
                val activePath = if (uiState.activePane == Pane.LEFT) uiState.leftPath else uiState.rightPath
                for (zip in zipFiles) {
                    viewModel.extractArchive(zip, activePath)
                }
                selectedFiles = emptySet()
            },
            onNewFolder = {
                val activePath = if (uiState.activePane == Pane.LEFT) uiState.leftPath else uiState.rightPath
                viewModel.createDirectory("$activePath/new_folder")
            },
            onPaste = { viewModel.pasteToActivePane() },
            hasClipboard = uiState.clipboard != null,
            onSignApk = {
                val apkFiles = selectedFiles.filter { it.lowercase().endsWith(".apk") }
                for (apk in apkFiles) {
                    val activePath = if (uiState.activePane == Pane.LEFT) uiState.leftPath else uiState.rightPath
                    ApkSigner.signApk(apk, activePath)
                }
                selectedFiles = emptySet()
            },
            selectedFiles = selectedFiles
        )
    }
}

@Composable
private fun FilePane(
    path: String,
    files: List<FileItem>,
    isActive: Boolean,
    selectedFiles: Set<String>,
    onFileClick: (FileItem) -> Unit,
    onFileLongClick: (FileItem) -> Unit,
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
                style = MiuixTheme.textStyles.label.small,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
                color = if (isActive) MiuixTheme.colorScheme.primary
                else MiuixTheme.colorScheme.onSurfaceVariant
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(files, key = { it.path }) { file ->
                FileRow(
                    file = file,
                    isSelected = file.path in selectedFiles,
                    onClick = { onFileClick(file) },
                    onLongClick = { onFileLongClick(file) }
                )
            }
        }
    }
}

@Composable
private fun FileRow(
    file: FileItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick, onLongClick = onLongClick)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (file.isDirectory) Icons.Filled.Folder else Icons.Filled.InsertDriveFile,
                contentDescription = null,
                tint = when {
                    isSelected -> MiuixTheme.colorScheme.primary
                    file.isDirectory -> MiuixTheme.colorScheme.primary
                    else -> MiuixTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    style = MiuixTheme.textStyles.body.small,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isSelected) MiuixTheme.colorScheme.primary
                    else MiuixTheme.colorScheme.onSurface
                )
                if (!file.isDirectory) {
                    Text(
                        text = formatFileSize(file.size),
                        style = MiuixTheme.textStyles.label.small,
                        color = MiuixTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (file.name.lowercase().endsWith(".apk")) {
                OutlinedButton(
                    onClick = { },
                    modifier = Modifier.height(28.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                ) {
                    Text(
                        text = "重签名",
                        style = MiuixTheme.textStyles.label.small
                    )
                }
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
    hasClipboard: Boolean,
    onSignApk: () -> Unit = {},
    selectedFiles: Set<String> = emptySet()
) {
    Card(
        modifier = Modifier.fillMaxWidth()
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
                Text("ZIP", style = MiuixTheme.textStyles.label.small)
            }
            IconButton(onClick = onExtract, modifier = Modifier.size(40.dp)) {
                Text("解压", style = MiuixTheme.textStyles.label.small)
            }
            IconButton(onClick = onNewFolder, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Filled.CreateNewFolder, contentDescription = "新建", modifier = Modifier.size(20.dp))
            }
            if (selectedFiles.any { it.lowercase().endsWith(".apk") }) {
                IconButton(onClick = onSignApk, modifier = Modifier.size(40.dp)) {
                    Text("签名", style = MiuixTheme.textStyles.label.small)
                }
            }
            if (hasClipboard) {
                OutlinedButton(
                    onClick = onPaste,
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("粘贴", style = MiuixTheme.textStyles.label.small)
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
