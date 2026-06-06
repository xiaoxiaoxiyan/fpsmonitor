package com.fpsmonitor

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fpsmonitor.core.RootChecker
import com.fpsmonitor.core.SettingsManager
import com.fpsmonitor.service.FpsOverlayService
import com.fpsmonitor.ui.screen.HistoryScreen
import com.fpsmonitor.ui.screen.MonitorScreen
import com.fpsmonitor.ui.screen.SettingsScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SettingsManager.init(this)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF4CAF50),
                    secondary = Color(0xFF2196F3),
                    background = Color(0xFF1A1A2E),
                    surface = Color(0xFF16213E),
                    onBackground = Color.White,
                    onSurface = Color.White
                )
            ) {
                FpsMonitorApp()
            }
        }
    }
}

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun FpsMonitorApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()

    var rootStatus by remember { mutableStateOf("检测中...") }
    var hasRoot by remember { mutableStateOf(false) }
    var isOverlayRunning by remember { mutableStateOf(false) }

    // Check root on launch
    LaunchedEffect(Unit) {
        scope.launch {
            val root = RootChecker.checkRoot()
            hasRoot = root
            rootStatus = if (root) "已 Root (${RootChecker.rootMethod})" else "未 Root"
            // Auto grant overlay permission if root
            if (root) {
                val pkg = context.packageName
                RootChecker.grantOverlayPermission(pkg)
            }
        }
    }

    val navItems = listOf(
        BottomNavItem("实时监测", Icons.Default.Speed, "monitor"),
        BottomNavItem("历史记录", Icons.Default.DateRange, "history"),
        BottomNavItem("设置", Icons.Default.Settings, "settings")
    )

    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> MonitorScreen(
                    hasRoot = hasRoot,
                    rootStatus = rootStatus,
                    isOverlayRunning = isOverlayRunning,
                    onStartOverlay = {
                        val intent = Intent(context, FpsOverlayService::class.java)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            context.startForegroundService(intent)
                        } else {
                            context.startService(intent)
                        }
                        isOverlayRunning = true
                    },
                    onStopOverlay = {
                        val intent = Intent(context, FpsOverlayService::class.java)
                        context.stopService(intent)
                        isOverlayRunning = false
                    }
                )
                1 -> HistoryScreen()
                2 -> SettingsScreen()
            }
        }
    }
}