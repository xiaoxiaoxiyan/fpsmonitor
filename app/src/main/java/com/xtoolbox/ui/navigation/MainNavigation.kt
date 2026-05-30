package com.xtoolbox.ui.navigation

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.xtoolbox.R
import com.xtoolbox.ui.screen.filemanager.FileManagerScreen
import com.xtoolbox.ui.screen.home.HomeScreen
import com.xtoolbox.ui.screen.module.ModuleScreen
import com.xtoolbox.ui.screen.more.Ak3FlashScreen
import com.xtoolbox.ui.screen.more.AntiDetectScreen
import com.xtoolbox.ui.screen.more.DeviceCleanupScreen
import com.xtoolbox.ui.screen.more.KeyConfigScreen
import com.xtoolbox.ui.screen.more.ModuleManageScreen
import com.xtoolbox.ui.screen.more.MoreScreen
import com.xtoolbox.ui.screen.more.OneClickHideScreen
import com.xtoolbox.ui.screen.more.PartitionScreen
import com.xtoolbox.ui.screen.more.SchedulerScreen
import com.xtoolbox.ui.screen.script.ScriptScreen
import com.xtoolbox.ui.screen.terminal.TerminalScreen
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

sealed class Screen(val route: String, val labelResId: Int, val icon: ImageVector) {
    data object Home : Screen("home", R.string.tab_home, Icons.Filled.Home)
    data object Script : Screen("script", R.string.tab_script, Icons.Filled.Description)
    data object Module : Screen("module", R.string.tab_module, Icons.Filled.Extension)
    data object File : Screen("file", R.string.tab_file, Icons.Filled.Folder)
    data object Terminal : Screen("terminal", R.string.tab_terminal, Icons.Filled.Terminal)
    data object More : Screen("more", R.string.tab_more, Icons.Filled.MoreHoriz)
}

val screens = listOf(
    Screen.Home,
    Screen.Script,
    Screen.Module,
    Screen.File,
    Screen.Terminal,
    Screen.More
)

@Composable
fun XToolboxApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Column(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.weight(1f)
        ) {
            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.Script.route) { ScriptScreen() }
            composable(Screen.Module.route) { ModuleScreen() }
            composable(Screen.File.route) { FileManagerScreen() }
            composable(Screen.Terminal.route) { TerminalScreen() }
            composable(Screen.More.route) { MoreScreen(navController) }
            composable("more/ak3") { Ak3FlashScreen() }
            composable("more/partition") { PartitionScreen() }
            composable("more/antidetect") { AntiDetectScreen() }
            composable("more/cleanup") { DeviceCleanupScreen() }
            composable("more/keystore") { KeyConfigScreen() }
            composable("more/hide") { OneClickHideScreen() }
            composable("more/scheduler") { SchedulerScreen() }
            composable("more/module") { ModuleManageScreen() }
        }

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                screens.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                            .padding(horizontal = 4.dp)
                    ) {
                        Icon(
                            imageVector = screen.icon,
                            contentDescription = stringResource(screen.labelResId),
                            tint = if (selected) MiuixTheme.colorScheme.primary
                            else MiuixTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(screen.labelResId),
                            color = if (selected) MiuixTheme.colorScheme.primary
                            else MiuixTheme.colorScheme.onSurfaceVariant,
                            style = MiuixTheme.textStyles.label.small,
                            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}
