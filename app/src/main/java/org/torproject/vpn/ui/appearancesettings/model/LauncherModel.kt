package org.torproject.vpn.ui.appearancesettings.model

data class LauncherModel(
    val launcherClass: String,
    val drawableResId: Int,
    val appNameResId: Int,
    var selected: Boolean)
