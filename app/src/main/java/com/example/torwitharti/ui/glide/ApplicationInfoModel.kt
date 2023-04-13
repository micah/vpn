package com.example.torwitharti.ui.glide

class ApplicationInfoModel(applicationInfo: String) {
    private val applicationInfo: String

    init {
        this.applicationInfo = applicationInfo
    }

    override fun toString(): String {
        return applicationInfo
    }
}