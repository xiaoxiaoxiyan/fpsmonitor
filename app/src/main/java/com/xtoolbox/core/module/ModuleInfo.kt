package com.xtoolbox.core.module

data class ModuleInfo(
    val id: String,
    val name: String,
    val version: String,
    val author: String,
    val description: String,
    val path: String,
    val isEnabled: Boolean = true,
    val hasWebUI: Boolean = false,
    val webUIPath: String? = null
)
