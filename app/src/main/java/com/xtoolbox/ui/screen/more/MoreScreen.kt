package com.xtoolbox.ui.screen.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

data class FeatureItem(
    val name: String,
    val description: String,
    val icon: ImageVector,
    val route: String
)

val features = listOf(
    FeatureItem("AK3 刷写", "AnyKernel3 内核刷入", Icons.Filled.FlashOn, "ak3"),
    FeatureItem("分区备份", "备份/刷入分区镜像", Icons.Filled.Storage, "backup"),
    FeatureItem("调度中心", "性能/温控/线程优化", Icons.Filled.Speed, "scheduler"),
    FeatureItem("过检测工具箱", "绕过各类检测", Icons.Filled.Security, "antidetect"),
    FeatureItem("伪装机型", "修改设备指纹", Icons.Filled.PhoneAndroid, "spoof"),
    FeatureItem("隐藏应用", "HMA 应用隐藏", Icons.Filled.VisibilityOff, "hideapp"),
    FeatureItem("设备清理", "改ID/标识清理", Icons.Filled.CleaningServices, "cleanup"),
    FeatureItem("模块管理", "安装/卸载模块", Icons.Filled.Extension, "module"),
    FeatureItem("刷机工具", "ADB/Fastboot操作", Icons.Filled.Build, "flash"),
    FeatureItem("DTBO 工具箱", "设备树操作", Icons.Filled.Folder, "dtbo"),
    FeatureItem("密钥配置", "TrickyStore配置", Icons.Filled.Key, "keystore"),
    FeatureItem("剪贴板解锁", "解除剪贴限制", Icons.Filled.Terminal, "clipboard"),
    FeatureItem("游戏清理", "PUBG/VAL清理", Icons.Filled.CleaningServices, "game"),
    FeatureItem("TG 过验证", "Telegram验证", Icons.Filled.Security, "tgverify"),
    FeatureItem("资源下载", "GKI/APK/内核", Icons.Filled.CloudDownload, "download"),
    FeatureItem("一键隐藏", "模块安装+隐藏", Icons.Filled.VisibilityOff, "hide"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("更多功能", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(features, key = { it.route }) { feature ->
                FeatureCard(feature = feature)
            }
        }
    }
}

@Composable
private fun FeatureCard(feature: FeatureItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: navigate to feature screen */ }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = feature.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = feature.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = feature.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
