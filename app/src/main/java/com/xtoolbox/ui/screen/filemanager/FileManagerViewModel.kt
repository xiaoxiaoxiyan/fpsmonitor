package com.xtoolbox.ui.screen.filemanager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xtoolbox.core.file.ArchiveHelper
import com.xtoolbox.core.file.FileItem
import com.xtoolbox.core.file.FileOperationEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FileManagerViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(FileManagerUiState())
    val uiState: StateFlow<FileManagerUiState> = _uiState.asStateFlow()

    init {
        loadDirectory(Pane.LEFT, _uiState.value.leftPath)
        loadDirectory(Pane.RIGHT, _uiState.value.rightPath)
    }

    fun loadDirectory(pane: Pane, path: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val files = FileOperationEngine.listFiles(path)
            _uiState.value = when (pane) {
                Pane.LEFT -> _uiState.value.copy(leftPath = path, leftFiles = files)
                Pane.RIGHT -> _uiState.value.copy(rightPath = path, rightFiles = files)
            }
        }
    }

    fun setActivePane(pane: Pane) {
        _uiState.value = _uiState.value.copy(activePane = pane)
    }

    fun navigateTo(pane: Pane, path: String) {
        loadDirectory(pane, path)
    }

    fun navigateUp(pane: Pane) {
        val currentPath = when (pane) {
            Pane.LEFT -> _uiState.value.leftPath
            Pane.RIGHT -> _uiState.value.rightPath
        }
        val parentPath = currentPath.substringBeforeLast("/")
        if (parentPath.isNotBlank() && parentPath != currentPath) {
            loadDirectory(pane, parentPath)
        }
    }

    fun copyFiles(files: List<String>) {
        _uiState.value = _uiState.value.copy(clipboard = ClipboardData(files, ClipboardOperation.COPY))
    }

    fun moveFiles(files: List<String>) {
        _uiState.value = _uiState.value.copy(clipboard = ClipboardData(files, ClipboardOperation.MOVE))
    }

    fun pasteToActivePane() {
        val clipboard = _uiState.value.clipboard ?: return
        val activePane = _uiState.value.activePane
        val destPath = when (activePane) {
            Pane.LEFT -> _uiState.value.leftPath
            Pane.RIGHT -> _uiState.value.rightPath
        }

        viewModelScope.launch(Dispatchers.IO) {
            for (src in clipboard.files) {
                val fileName = src.substringAfterLast("/")
                val dest = "$destPath/$fileName"
                when (clipboard.operation) {
                    ClipboardOperation.COPY -> FileOperationEngine.copyFile(src, dest)
                    ClipboardOperation.MOVE -> FileOperationEngine.moveFile(src, dest)
                }
            }
            _uiState.value = _uiState.value.copy(clipboard = null)
            loadDirectory(Pane.LEFT, _uiState.value.leftPath)
            loadDirectory(Pane.RIGHT, _uiState.value.rightPath)
        }
    }

    fun deleteFiles(files: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            for (path in files) {
                FileOperationEngine.deleteFile(path)
            }
            loadDirectory(Pane.LEFT, _uiState.value.leftPath)
            loadDirectory(Pane.RIGHT, _uiState.value.rightPath)
        }
    }

    fun compressFiles(files: List<String>, destZip: String) {
        viewModelScope.launch(Dispatchers.IO) {
            for (src in files) {
                ArchiveHelper.compressToZip(src, destZip)
            }
            loadDirectory(Pane.LEFT, _uiState.value.leftPath)
            loadDirectory(Pane.RIGHT, _uiState.value.rightPath)
        }
    }

    fun extractArchive(zipPath: String, destDir: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val lower = zipPath.lowercase()
            when {
                lower.endsWith(".tar.gz") || lower.endsWith(".tgz") -> ArchiveHelper.extractTarGz(zipPath, destDir)
                lower.endsWith(".tar") -> ArchiveHelper.extractTar(zipPath, destDir)
                else -> ArchiveHelper.extractZip(zipPath, destDir)
            }
            loadDirectory(Pane.LEFT, _uiState.value.leftPath)
            loadDirectory(Pane.RIGHT, _uiState.value.rightPath)
        }
    }

    fun createDirectory(path: String) {
        viewModelScope.launch(Dispatchers.IO) {
            FileOperationEngine.createDirectory(path)
            loadDirectory(Pane.LEFT, _uiState.value.leftPath)
            loadDirectory(Pane.RIGHT, _uiState.value.rightPath)
        }
    }

    fun executeScript(path: String) {
        viewModelScope.launch(Dispatchers.IO) {
            FileOperationEngine.executeScript(path)
        }
    }
}
