package org.torproject.vpn.ui.generalsettings.model

import org.torproject.vpn.R

object LauncherSet {

    val list: List<LauncherModel> = listOf(
        LauncherModel(LauncherDefault::class.java.name, R.mipmap.ic_launcher_round, R.string.app_name, false),
        LauncherModel(LauncherVariant1::class.java.name, R.mipmap.ic_launcher_round, R.string.app_name_variant_1, false),
        LauncherModel(LauncherVariant2::class.java.name, R.mipmap.ic_launcher_round, R.string.app_name_variant_2, false),
        LauncherModel(LauncherVariant3::class.java.name, R.mipmap.ic_launcher_round, R.string.app_name_variant_3, false),
    )

}

class LauncherDefault
class LauncherVariant1
class LauncherVariant2
class LauncherVariant3
