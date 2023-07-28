package org.torproject.vpn.ui.exitselection.model

import org.torproject.vpn.ui.exitselection.data.ExitNodeAdapter
import java.text.Collator
import java.util.Locale

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
        val coll = Collator.getInstance(Locale.getDefault())
        return compareValuesBy(this, other, { !it.selected }, { coll.compare(it.countryName, countryName) })
    }
}

class ExitNodeTableHeaderModel(var selected: Boolean) : ViewTypeDependentModel {
    override fun getViewType(): Int {
        return ExitNodeAdapter.TABLE_HEADER_VIEW
    }
}