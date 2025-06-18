package org.torproject.vpn.ui.approuting.data

import androidx.recyclerview.widget.RecyclerView
import android.util.Log

fun <T> RecyclerView.Adapter<*>.getChangedItemRanges(
    oldList: List<T>,
    newList: List<T>
): List<Pair<Int, Int>> {
    Log.d("TAG", "getChangedItemRanges: $oldList \n$newList \nsize: ${oldList.size} vs. ${newList.size}")
    val changedRanges = mutableListOf<Pair<Int, Int>>()
    val maxSize = Math.max(oldList.size, newList.size)

    var start = -1
    var length = 0

    for (i in 0 until maxSize) {
        val oldItem = if (i < oldList.size) oldList[i] else null
        val newItem = if (i < newList.size) newList[i] else null

        if (oldItem != newItem) {
            if (start == -1) {
                // Start a new range
                start = i
                length = 1
            } else {
                // Extend the current range
                length++
            }
        } else {
            if (start != -1) {
                // End the current range
                changedRanges.add(Pair(start, length))
                start = -1
                length = 0
            }
        }
    }

    // If we ended with an open range, add it
    if (start != -1) {
        changedRanges.add(Pair(start, length))
    }

    Log.d("TAG", "returned ranges: $changedRanges")
    return changedRanges
}
