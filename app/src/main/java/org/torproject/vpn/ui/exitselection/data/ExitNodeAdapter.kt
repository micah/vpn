package org.torproject.vpn.ui.exitselection.data

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import org.torproject.vpn.databinding.ExitnodeItemViewBinding
import org.torproject.vpn.ui.exitselection.model.ExitNodeCellModel
import org.torproject.vpn.utils.PreferenceHelper
import org.torproject.vpn.utils.getFlagByCountryCode

class ExitNodeAdapter(liveDataList: LiveData<List<ExitNodeCellModel>>, viewLifecycleOwner: LifecycleOwner): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var items: List<ExitNodeCellModel> = mutableListOf()
    var onExitNodeSelected: ((pos: Int, item: ExitNodeCellModel) -> Unit)? = null


    private lateinit var preferenceHelper: PreferenceHelper

    init {
        liveDataList.observe(viewLifecycleOwner) { list ->
            items = list.toMutableList()
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val viewHolder: RecyclerView.ViewHolder = ExitNodeCellViewHolder(ExitnodeItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        preferenceHelper = PreferenceHelper(parent.context.applicationContext)
        return viewHolder
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ExitNodeCellViewHolder).bind(items[position], position)
    }

    inner class ExitNodeCellViewHolder(val binding: ExitnodeItemViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ExitNodeCellModel, pos: Int) {
            binding.tvTitle.text = item.countryName
            binding.ivCountryFlag.setImageDrawable(getFlagByCountryCode(binding.root.context, item.countryCode))
            binding.rbSelected.isChecked = item.selected
            binding.rbSelected.contentDescription = item.countryName
            binding.itemContainer.setOnClickListener {
                onExitNodeSelected?.invoke(pos, item)
            }
        }
    }
}