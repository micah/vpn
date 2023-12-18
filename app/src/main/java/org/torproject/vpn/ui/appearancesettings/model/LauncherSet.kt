package org.torproject.vpn.ui.appearancesettings.model

import org.torproject.vpn.R

object LauncherSet {

    val list: List<LauncherModel> = listOf(
        LauncherModel(LauncherDefault::class.java.name, R.mipmap.ic_launcher, R.string.app_name, false),
        LauncherModel(LauncherVariant1::class.java.name, R.mipmap.ic_launcher_monochrome, R.string.app_name_variant_1, false),
        LauncherModel(LauncherVariant2::class.java.name, R.mipmap.ic_launcher_monochrome, R.string.app_name_variant_2, false),
        LauncherModel(LauncherVariant3::class.java.name, R.mipmap.ic_launcher_monochrome, R.string.app_name_variant_3, false),
        LauncherModel(LauncherVariant4::class.java.name, R.mipmap.ic_launcher_monochrome, R.string.app_name_variant_4, false),
        LauncherModel(LauncherVariant5::class.java.name, R.mipmap.ic_launcher_monochrome, R.string.app_name_variant_5, false),
        LauncherModel(LauncherVariant6::class.java.name, R.mipmap.ic_launcher_monochrome, R.string.app_name_variant_6, false),
        LauncherModel(LauncherVariant7::class.java.name, R.mipmap.ic_launcher_monochrome, R.string.app_name_variant_7, false)
    )

}

class LauncherDefault
class LauncherVariant1
class LauncherVariant2
class LauncherVariant3
class LauncherVariant4
class LauncherVariant5
class LauncherVariant6
class LauncherVariant7
