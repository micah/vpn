package org.torproject.vpn.ui.appdetail.data

import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import org.torproject.onionmasq.circuit.CircuitCountryCodes
import org.torproject.vpn.R
import org.torproject.vpn.databinding.ViewCircuitsPerAppBinding
import org.torproject.vpn.utils.PreferenceHelper
import org.torproject.vpn.utils.PreferenceHelper.Companion.BridgeType
import org.torproject.vpn.utils.getCountryByCode
import org.torproject.vpn.utils.getFlagByCountryCode
import org.torproject.vpn.utils.navigateSafe

class CircuitCardAdapter(val appName: String, val preferenceHelper: PreferenceHelper) : RecyclerView.Adapter<RecyclerView.ViewHolder>()  {
    var items: List<CircuitCountryCodes> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return CircuitCardViewHolder(ViewCircuitsPerAppBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as CircuitCardViewHolder).bind(items[position])
    }

    fun update(list: List<CircuitCountryCodes>) {
        val mutableList = list.toMutableList()
        if (items != mutableList) {
            items = mutableList
            notifyDataSetChanged()
        }
    }

    inner class CircuitCardViewHolder(val binding: ViewCircuitsPerAppBinding) : ViewHolder(binding.root) {
        var collapse = false

        fun bind(item: CircuitCountryCodes) {
            val context = binding.root.context
            binding.expandedContainer.tvAppExit.text = appName
            if (preferenceHelper.useBridge) {
                val guardNodeText = when (preferenceHelper.bridgeType) {
                    BridgeType.Obfs4,
                    BridgeType.Manual -> context.getString(R.string.obfs4_bridge)
                    BridgeType.Snowflake -> context.getString(R.string.snowflake)
                    BridgeType.None -> getCountryByCode(context, item.countryCodes[0])
                }
                binding.expandedContainer.tvEntryNode.text = guardNodeText
                binding.expandedContainer.tvChange.visibility = if (preferenceHelper.bridgeType != BridgeType.None) VISIBLE else GONE
            } else {
                binding.expandedContainer.tvEntryNode.text = getCountryByCode(context, item.countryCodes[0])
                binding.expandedContainer.tvChange.visibility = GONE
            }
            binding.expandedContainer.tvRelayNode.text = getCountryByCode(context, item.countryCodes[1])
            binding.expandedContainer.tvExitNode.text = getCountryByCode(context, item.countryCodes[2])
            binding.expandedContainer.ivCountryFlagEntryNode.visibility = if ((preferenceHelper.useBridge && preferenceHelper.bridgeType != BridgeType.None) || item.countryCodes[0] == null) GONE else VISIBLE
            binding.expandedContainer.ivCountryFlagEntryNode.setImageDrawable(
                getFlagByCountryCode(context,  item.countryCodes[0])
            )
            binding.expandedContainer.ivCountryFlagRelayNode.visibility = if (item.countryCodes[1] == null) GONE else VISIBLE
            binding.expandedContainer.ivCountryFlagRelayNode.setImageDrawable(
                getFlagByCountryCode(context,  item.countryCodes[1])
            )
            binding.expandedContainer.ivCountryFlagExitNode.visibility = if (item.countryCodes[2] == null) GONE else VISIBLE
            binding.expandedContainer.ivCountryFlagExitNode.setImageDrawable(
                getFlagByCountryCode(context,  item.countryCodes[2])
            )
            binding.expandedContainer.tvChange.setOnClickListener{ _ ->
                binding.root.findNavController().navigateSafe(R.id.action_navigation_appDetails_to_bridgeSettings)
            }
        }
    }
}