package com.example.torwitharti.utils

import com.example.torwitharti.ui.settings.AppItemModel

class AppManager {
     fun queryInstalledApps() : List<AppItemModel>  {
         // TODO: implement me!
         val arrayList = mutableListOf<AppItemModel>().apply {
             add(AppItemModel("Bitmask", "se.leap.bitmaskclient", false, false, false))
             add(AppItemModel("RiseupVPN", "se.leap.riseupvpn", false, false, false))
             add(AppItemModel("F-Droid", "org.fdroid.fdroid", false, false, false))
         }
        return arrayList
    }
}