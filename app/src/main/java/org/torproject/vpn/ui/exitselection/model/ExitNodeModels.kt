package org.torproject.vpn.ui.exitselection.model

import org.torproject.vpn.ui.exitselection.data.ExitNodeAdapter

interface ViewTypeDependentModel {
    fun getViewType(): Int
}

data class ExitNodeCellModel (
    val countryCode: String,
    val countryName: String,
    var selected: Boolean
) : ViewTypeDependentModel, Comparable<ExitNodeCellModel> {

    override fun getViewType(): Int {
        return ExitNodeAdapter.CELL
    }

    override fun compareTo(other: ExitNodeCellModel): Int {
        return compareValuesBy(this, other,  { !other.selected } , { it.countryName })
    }
}

class ExitNodeTableHeaderModel(var selected: Boolean) : ViewTypeDependentModel {
    override fun getViewType(): Int {
        return ExitNodeAdapter.TABLE_HEADER_VIEW
    }
}