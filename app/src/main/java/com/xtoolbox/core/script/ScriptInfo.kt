package com.xtoolbox.core.script

data class ScriptInfo(
    val name: String,
    val url: String,
    val localPath: String? = null,
    val size: Long = 0,
    val lastModified: String = "",
    val isDownloaded: Boolean = false
)
