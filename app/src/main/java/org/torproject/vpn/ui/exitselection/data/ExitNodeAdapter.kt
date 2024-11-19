package org.torproject.vpn.ui.exitselection.data

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import org.torproject.vpn.databinding.ExitnodeHeaderViewBinding
import org.torproject.vpn.databinding.ExitnodeItemViewBinding
import org.torproject.vpn.ui.exitselection.model.ExitNodeCellModel
import org.torproject.vpn.ui.exitselection.model.ExitNodeTableHeaderModel
import org.torproject.vpn.ui.exitselection.model.ViewTypeDependentModel
import org.torproject.vpn.utils.PreferenceHelper
import org.torproject.vpn.utils.getFlagByCountryCode

class ExitNodeAdapter(liveDataList: LiveData<List<ViewTypeDependentModel>>, viewLifecycleOwner: LifecycleOwner): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var items: List<ViewTypeDependentModel> = mutableListOf()
    var onExitNodeSelected: ((pos: Int, item: ExitNodeCellModel) -> Unit)? = null
    var onAutomaticExitNodeChanged: ((item: ExitNodeTableHeaderModel) -> Unit)? = null


    private lateinit var preferenceHelper: PreferenceHelper

    companion object {
        const val TABLE_HEADER_VIEW = 0
        const val CELL = 1
    }

    init {
        liveDataList.observe(viewLifecycleOwner) { list ->
            items = list.toMutableList()
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val viewHolder: RecyclerView.ViewHolder = when (viewType) {
            TABLE_HEADER_VIEW -> ExitNodeHeaderviewViewHolder(ExitnodeHeaderViewBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> ExitNodeCellViewHolder(ExitnodeItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
        preferenceHelper = PreferenceHelper(parent.context.applicationContext)
        return viewHolder
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return items[position].getViewType()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (items[position].getViewType()) {
            TABLE_HEADER_VIEW -> (holder as ExitNodeHeaderviewViewHolder).bind(items[position] as ExitNodeTableHeaderModel)
            CELL -> (holder as ExitNodeCellViewHolder).bind(items[position] as ExitNodeCellModel, position)
        }
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

    inner class ExitNodeHeaderviewViewHolder(val binding: ExitnodeHeaderViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ExitNodeTableHeaderModel) {
            binding.smProtectAllApps.isChecked = item.selected
            binding.automaticContainer.setOnClickListener {
                item.selected = item.selected
                onAutomaticExitNodeChanged?.invoke(item)
            }
        }
    }
}