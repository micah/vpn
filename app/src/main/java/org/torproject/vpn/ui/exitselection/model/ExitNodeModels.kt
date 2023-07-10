package org.torproject.vpn.ui.exitselection.model

import org.torproject.vpn.ui.exitselection.data.ExitNodeAdapter

interface ViewTypeDependentModel {
    fun getViewType(): Int
}

data class ExitNodeCellModel(
    val countryCode: String,
    val countryName: String,
    var selected: Boolean
) : ViewTypeDependentModel {

    override fun getViewType(): Int {
        return ExitNodeAdapter.CELL
    }
}

class ExitNodeTableHeaderModel(var selected: Boolean) : ViewTypeDependentModel {
    override fun getViewType(): Int {
        return ExitNodeAdapter.TABLE_HEADER_VIEW
    }
}