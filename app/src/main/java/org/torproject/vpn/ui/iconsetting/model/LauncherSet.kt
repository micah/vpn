package org.torproject.vpn.ui.iconsetting.model

import org.torproject.vpn.R

object LauncherSet {

    val list: List<LauncherModel> = listOf(
        LauncherModel(LauncherDefault::class.java.name, R.mipmap.ic_launcher_round, R.string.app_name, false),
        LauncherModel(LauncherGarden::class.java.name, R.mipmap.ic_launcher_garden_round, R.string.app_name_garden, false),
        LauncherModel(LauncherWeather::class.java.name, R.mipmap.ic_launcher_weather_round, R.string.app_name_weather, false),
        LauncherModel(LauncherPingPong::class.java.name, R.mipmap.ic_launcher_pingpong_round, R.string.app_name_pingpong, false),
    )

}

class LauncherDefault
class LauncherGarden
class LauncherWeather
class LauncherPingPong
