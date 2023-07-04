package org.torproject.vpn.ui.appdetail.data

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import org.torproject.onionmasq.circuit.Circuit
import org.torproject.vpn.R
import org.torproject.vpn.databinding.ViewCircuitsPerAppBinding

class CircuitCardAdapter(val isBrowser: Boolean) : RecyclerView.Adapter<RecyclerView.ViewHolder>()  {
    var items: List<Circuit> = ArrayList()
    var expandedItemPos = -1


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
       return CircuitCardViewHolder(ViewCircuitsPerAppBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as CircuitCardViewHolder).bind(items[position], position)
    }

    fun update(list: List<Circuit>) {
        val mutableList = list.toMutableList()
        if (items != mutableList) {
            if (expandedItemPos != -1 && (mutableList.size <= expandedItemPos || mutableList[expandedItemPos] != items[expandedItemPos])) {
                expandedItemPos = -1
            }
            items = mutableList
            notifyDataSetChanged()
        }
    }

    inner class CircuitCardViewHolder(val binding: ViewCircuitsPerAppBinding) : ViewHolder(binding.root) {

        fun bind(item: Circuit, position: Int) {
            binding.root.setOnClickListener {
                if (expandedItemPos == position) {
                    expandedItemPos = -1
                    notifyItemChanged(position)
                } else if (expandedItemPos == -1) {
                    expandedItemPos = position
                    notifyItemChanged(position)
                } else {
                    val previousExpandedItemPos = expandedItemPos
                    expandedItemPos = position
                    notifyItemChanged(previousExpandedItemPos)
                    notifyItemChanged(position)
                }
            }

            val relayDetails = item.relayDetails
            if (expandedItemPos == position) {
                binding.expandedContainer.tvAppExit.text = binding.root.context.getString(if (isBrowser) R.string.this_browser else R.string.this_app)
                binding.expandedContainer.tvEntryNode.text = if (relayDetails.size >= 1) relayDetails[0].addresses[0] else null
                binding.expandedContainer.tvRelayNode.text = if (relayDetails.size >= 2) relayDetails[1].addresses[0] else null
                binding.expandedContainer.tvExitNode.text = if (relayDetails.size >= 3) relayDetails[2].addresses[0] else null
                binding.expandedContainer.tvCircuitDescription.text  = binding.root.context.getString(R.string.circuits_app_description, "https://wikipedia.org")
            } else {
                binding.collapsedContainer.tvAddress.text = "https://wikipedia.org"
                binding.collapsedContainer.tvRoutingDescription.text = "Routed over XYZ"
                binding.collapsedContainer.ivCountryFlag.setImageDrawable(ContextCompat.getDrawable(binding.root.context, R.drawable.flag_ua))
            }
            binding.collapsedContainer.root.visibility = if (expandedItemPos == position) View.GONE else View.VISIBLE
            binding.expandedContainer.root.visibility = if (expandedItemPos == position) View.VISIBLE else View.GONE
        }
    }
}