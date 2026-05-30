package com.xtoolbox.ui.screen.module

import android.content.Intent
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.xtoolbox.core.module.ModuleInfo
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleScreen(viewModel: ModuleViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "模块管理",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "刷新")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: file picker for module zip */ }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "安装模块")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(32.dp)
                )
            } else if (uiState.modules.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Extension,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MiuixTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "暂无已安装模块",
                        style = MiuixTheme.textStyles.body.large,
                        color = MiuixTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    items(uiState.modules, key = { it.id }) { module ->
                        ModuleCard(
                            module = module,
                            onToggle = { viewModel.toggleModule(module) },
                            onUninstall = { viewModel.uninstallModule(module) },
                            onWebUI = {
                                val intent = Intent(context, WebUIActivity::class.java).apply {
                                    putExtra("webui_path", module.webUIPath)
                                    putExtra("module_id", module.id)
                                }
                                context.startActivity(intent)
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ModuleCard(
    module: ModuleInfo,
    onToggle: () -> Unit,
    onUninstall: () -> Unit,
    onWebUI: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Extension,
                    contentDescription = null,
                    tint = if (module.isEnabled) MiuixTheme.colorScheme.primary
                    else MiuixTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = module.name,
                        style = MiuixTheme.textStyles.title.small,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "v${module.version} | ${module.author}",
                        style = MiuixTheme.textStyles.body.small,
                        color = MiuixTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = module.isEnabled,
                    onCheckedChange = { onToggle() }
                )
            }

            if (module.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = module.description,
                    style = MiuixTheme.textStyles.body.small,
                    color = MiuixTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (module.hasWebUI) {
                    FilledTonalButton(
                        onClick = onWebUI,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            Icons.Filled.Language,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("WebUI")
                    }
                }
                OutlinedButton(
                    onClick = onUninstall,
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        contentColor = MiuixTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("卸载")
                }
            }
        }
    }
}
