package com.xtoolbox.ui.screen.filemanager

import com.xtoolbox.core.file.FileItem

data class FileManagerUiState(
    val leftPath: String = "/sdcard",
    val rightPath: String = "/data",
    val leftFiles: List<FileItem> = emptyList(),
    val rightFiles: List<FileItem> = emptyList(),
    val activePane: Pane = Pane.LEFT,
    val selectedFiles: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val clipboard: ClipboardData? = null
)

enum class Pane { LEFT, RIGHT }

data class ClipboardData(
    val files: List<String>,
    val operation: ClipboardOperation
)

enum class ClipboardOperation { COPY, MOVE }
