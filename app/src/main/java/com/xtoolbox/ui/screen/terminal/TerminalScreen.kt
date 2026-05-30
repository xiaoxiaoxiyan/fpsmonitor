package com.xtoolbox.ui.screen.terminal

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.theme.MiuixTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalScreen(viewModel: TerminalViewModel = viewModel()) {
    val activeOutput = viewModel.getActiveOutput()
    val fontSize = viewModel.fontSize
    var commandInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "终端",
                        fontWeight = FontWeight.Bold,
                        style = MiuixTheme.textStyles.title.large,
                        color = MiuixTheme.colorScheme.onSurface
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.zoomOut() }) {
                        Icon(Icons.Filled.Remove, contentDescription = "缩小")
                    }
                    Text(
                        text = "${fontSize.value.toInt()}",
                        style = MiuixTheme.textStyles.label.medium,
                        color = MiuixTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(onClick = { viewModel.zoomIn() }) {
                        Icon(Icons.Filled.Add, contentDescription = "放大")
                    }
                    IconButton(onClick = { viewModel.newSession() }) {
                        Icon(Icons.Filled.Add, contentDescription = "新会话")
                    }
                }
            )
        },
        bottomBar = {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    if (viewModel.tabs.size > 1) {
                        ScrollableTabRow(
                            selectedTabIndex = viewModel.activeTabIndex,
                            modifier = Modifier.fillMaxWidth(),
                            edgePadding = 0.dp
                        ) {
                            viewModel.tabs.forEachIndexed { index, tab ->
                                Tab(
                                    selected = index == viewModel.activeTabIndex,
                                    onClick = { viewModel.setActiveTab(index) },
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                tab.session.name,
                                                style = MiuixTheme.textStyles.label.small,
                                                color = if (index == viewModel.activeTabIndex)
                                                    MiuixTheme.colorScheme.primary
                                                else MiuixTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            IconButton(
                                                onClick = { viewModel.closeSession(index) },
                                                modifier = Modifier.size(16.dp)
                                            ) {
                                                Icon(
                                                    Icons.Filled.Close,
                                                    contentDescription = "关闭",
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        OutlinedTextField(
                            value = commandInput,
                            onValueChange = { commandInput = it },
                            modifier = Modifier.weight(1f),
                            placeholder = {
                                Text(
                                    "输入命令…",
                                    color = MiuixTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            singleLine = true,
                            textStyle = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = fontSize
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    if (commandInput.isNotBlank()) {
                                        viewModel.executeCommand(commandInput)
                                        commandInput = ""
                                    }
                                }
                            )
                        )
                        FilledTonalButton(
                            onClick = {
                                if (commandInput.isNotBlank()) {
                                    viewModel.executeCommand(commandInput)
                                    commandInput = ""
                                }
                            },
                            modifier = Modifier.align(Alignment.CenterVertically)
                        ) {
                            Text(
                                "发送",
                                style = MiuixTheme.textStyles.label.medium
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ShortcutKey("Ctrl+C", onClick = { viewModel.executeCommand("\u0003") })
                        ShortcutKey("Ctrl+D", onClick = { viewModel.executeCommand("\u0004") })
                        ShortcutKey("Tab", onClick = { viewModel.executeCommand("\t") })
                        ShortcutKey("↑", onClick = { viewModel.executeCommand("!!") })
                        ShortcutKey("clear", onClick = { viewModel.executeCommand("clear") })
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF1E1E1E))
        ) {
            val scrollState = rememberScrollState()
            val horizontalScrollState = rememberScrollState()

            Text(
                text = parseAnsiColors(activeOutput),
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = fontSize,
                    color = Color(0xFFD4D4D4)
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .verticalScroll(scrollState)
                    .horizontalScroll(horizontalScrollState)
            )
        }
    }
}

@Composable
private fun ShortcutKey(label: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.height(32.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
    ) {
        Text(
            label,
            style = MiuixTheme.textStyles.label.small,
            color = MiuixTheme.colorScheme.onSurface
        )
    }
}

private fun parseAnsiColors(text: String): AnnotatedString {
    val builder = AnnotatedString.Builder()
    val ansiRegex = Regex("\u001B\\[([0-9;]*)m")
    var lastIndex = 0
    var currentColor: Color? = null

    for (match in ansiRegex.findAll(text)) {
        if (match.range.first > lastIndex) {
            val segment = text.substring(lastIndex, match.range.first)
            if (currentColor != null) {
                builder.pushStyle(SpanStyle(color = currentColor))
                builder.append(segment)
                builder.pop()
            } else {
                builder.append(segment)
            }
        }

        val code = match.groupValues[1]
        currentColor = when {
            code.contains("31") -> Color(0xFFFF6B6B)
            code.contains("32") -> Color(0xFF98C379)
            code.contains("33") -> Color(0xFFE5C07B)
            code.contains("34") -> Color(0xFF61AFEF)
            code.contains("35") -> Color(0xFFC678DD)
            code.contains("36") -> Color(0xFF56B6C2)
            code.contains("37") -> Color(0xFFD4D4D4)
            code.contains("0") -> null
            else -> currentColor
        }

        lastIndex = match.range.last + 1
    }

    if (lastIndex < text.length) {
        val remaining = text.substring(lastIndex)
        if (currentColor != null) {
            builder.pushStyle(SpanStyle(color = currentColor))
            builder.append(remaining)
            builder.pop()
        } else {
            builder.append(remaining)
        }
    }

    return builder.toAnnotatedString()
}
