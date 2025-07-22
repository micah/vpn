package org.torproject.vpn.ui.iconsetting.model

data class LauncherModel(
    val launcherClass: String,
    val drawableResId: Int,
    val appNameResId: Int,
    var selected: Boolean)
