package org.torproject.vpn.ui.exitselection.model

import java.text.Collator
import java.util.Locale

data class ExitNodeCellModel (
    val countryCode: String,
    val countryName: String,
    var selected: Boolean
) : Comparable<ExitNodeCellModel> {

    override fun compareTo(other: ExitNodeCellModel): Int {
        val coll = Collator.getInstance(Locale.getDefault())
        return compareValuesBy(this, other, { !it.selected }, { coll.compare(it.countryName, countryName) })
    }
}