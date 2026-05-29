package com.xtoolbox.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
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
import com.xtoolbox.ui.screen.more.MoreScreen
import com.xtoolbox.ui.screen.script.ScriptScreen
import com.xtoolbox.ui.screen.terminal.TerminalScreen

sealed class Screen(val route: String, val labelResId: Int, val icon: ImageVector) {
    data object Home : Screen("home", R.string.tab_home, androidx.compose.material.icons.Icons.Filled.Home)
    data object Script : Screen("script", R.string.tab_script, androidx.compose.material.icons.Icons.Filled.Description)
    data object Module : Screen("module", R.string.tab_module, androidx.compose.material.icons.Icons.Filled.Extension)
    data object File : Screen("file", R.string.tab_file, androidx.compose.material.icons.Icons.Filled.Folder)
    data object Terminal : Screen("terminal", R.string.tab_terminal, androidx.compose.material.icons.Icons.Filled.Terminal)
    data object More : Screen("more", R.string.tab_more, androidx.compose.material.icons.Icons.Filled.MoreHoriz)
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

    Scaffold(
        bottomBar = {
            NavigationBar {
                screens.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = stringResource(screen.labelResId)) },
                        label = { Text(stringResource(screen.labelResId)) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.Script.route) { ScriptScreen() }
            composable(Screen.Module.route) { ModuleScreen() }
            composable(Screen.File.route) { FileManagerScreen() }
            composable(Screen.Terminal.route) { TerminalScreen() }
            composable(Screen.More.route) { MoreScreen() }
        }
    }
}
