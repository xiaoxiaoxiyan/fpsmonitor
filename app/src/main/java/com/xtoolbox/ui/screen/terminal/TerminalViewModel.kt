package com.xtoolbox.ui.screen.terminal

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import com.xtoolbox.core.terminal.TerminalEngine
import com.xtoolbox.core.terminal.TerminalSession
import java.util.UUID

data class TerminalTab(
    val session: TerminalSession,
    val output: StringBuilder = StringBuilder(),
    val engine: TerminalEngine
)

class TerminalViewModel : ViewModel() {
    private val _tabs = mutableStateListOf<TerminalTab>()
    val tabs: List<TerminalTab> get() = _tabs

    private val _activeTabIndex = mutableStateOf(0)
    val activeTabIndex: Int get() = _activeTabIndex.value

    private val _fontSize = mutableStateOf(14.sp)
    val fontSize: androidx.compose.ui.unit.TextUnit get() = _fontSize.value

    init {
        newSession()
    }

    fun newSession(root: Boolean = true) {
        val id = UUID.randomUUID().toString().take(8)
        val session = TerminalSession(id = id, name = "终端 ${_tabs.size + 1}")
        val output = StringBuilder()

        val engine = TerminalEngine(
            onOutput = { text ->
                output.append(text)
            },
            onExit = {}
        )

        val tab = TerminalTab(session = session, output = output, engine = engine)
        _tabs.add(tab)
        _activeTabIndex.value = _tabs.size - 1

        engine.startSession(root)
    }

    fun executeCommand(command: String) {
        val tab = _tabs.getOrNull(activeTabIndex) ?: return
        tab.engine.executeCommand(command)
    }

    fun closeSession(index: Int) {
        val tab = _tabs.getOrNull(index) ?: return
        tab.engine.closeSession()
        _tabs.removeAt(index)
        if (_activeTabIndex.value >= _tabs.size) {
            _activeTabIndex.value = (_tabs.size - 1).coerceAtLeast(0)
        }
    }

    fun setActiveTab(index: Int) {
        _activeTabIndex.value = index.coerceIn(0, (_tabs.size - 1).coerceAtLeast(0))
    }

    fun getActiveOutput(): String {
        val tab = _tabs.getOrNull(activeTabIndex) ?: return ""
        return tab.output.toString()
    }

    fun zoomIn() {
        _fontSize.value = (_fontSize.value + 2.sp).coerceAtMost(32.sp)
    }

    fun zoomOut() {
        _fontSize.value = (_fontSize.value - 2.sp).coerceAtLeast(10.sp)
    }

    override fun onCleared() {
        super.onCleared()
        _tabs.forEach { it.engine.closeSession() }
    }
}
