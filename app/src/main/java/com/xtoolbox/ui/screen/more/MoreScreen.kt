package com.xtoolbox.ui.screen.more

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.navigation.NavHostController

data class FeatureItem(
    val name: String,
    val description: String,
    val icon: ImageVector,
    val route: String
)

val features = listOf(
    FeatureItem("AK3 刷写", "AnyKernel3 内核刷入", Icons.Filled.FlashOn, "ak3"),
    FeatureItem("分区刷写", "备份/刷入分区镜像", Icons.Filled.Storage, "partition"),
    FeatureItem("过检测工具箱", "绕过各类检测", Icons.Filled.Security, "antidetect"),
    FeatureItem("设备清理", "改ID/标识清理", Icons.Filled.CleaningServices, "cleanup"),
    FeatureItem("密钥配置", "keybox配置管理", Icons.Filled.Key, "keystore"),
    FeatureItem("模块管理", "安装/卸载模块", Icons.Filled.Extension, "module"),
    FeatureItem("一键隐藏", "模块安装+隐藏", Icons.Filled.VisibilityOff, "hide"),
    FeatureItem("调度中心", "CPU性能/温控管理", Icons.Filled.Speed, "scheduler"),
)

@Composable
fun MoreScreen(navController: NavHostController? = null) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "更多功能",
            style = MiuixTheme.textStyles.headline.large,
            fontWeight = FontWeight.Bold,
            color = MiuixTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(4.dp)
            ) {
                features.forEachIndexed { index, feature ->
                    FeatureRow(
                        feature = feature,
                        onClick = {
                            navController?.navigate("more/${feature.route}")
                        }
                    )
                    if (index < features.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MiuixTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureRow(feature: FeatureItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = feature.icon,
            contentDescription = null,
            tint = MiuixTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = feature.name,
                style = MiuixTheme.textStyles.body.primary,
                fontWeight = FontWeight.Medium,
                color = MiuixTheme.colorScheme.onSurface
            )
            Text(
                text = feature.description,
                style = MiuixTheme.textStyles.body.tertiary,
                color = MiuixTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MiuixTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}
