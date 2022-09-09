package com.example.torwitharti.ui.approuting

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.torwitharti.databinding.AppRoutingTableHeaderBinding
import com.example.torwitharti.databinding.AppSwitchItemViewBinding
import com.example.torwitharti.databinding.AppTitleViewBinding
import com.example.torwitharti.databinding.HorizontalRecyclerViewItemBinding
import com.example.torwitharti.utils.PreferenceHelper

class AppListAdapter(list: List<AppItemModel>,
                     var torAppsAdapter: TorAppsAdapter,
                     var torAppsLayoutManager: LinearLayoutManager,
                     var preferenceHelper: PreferenceHelper
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val SECTION_HEADER_VIEW = 0
        const val CELL = 1
        const val HORIZONTAL_RECYCLER_VIEW = 2
        const val TABLE_HEADER_VIEW = 3
    }

    var items: MutableList<AppItemModel> = list.toMutableList()
    var onItemModelChanged: ((pos: Int, item: AppItemModel) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val viewHolder: RecyclerView.ViewHolder = when (viewType) {
            CELL -> AppListItemViewHolder(AppSwitchItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            HORIZONTAL_RECYCLER_VIEW -> HorizontalRecyclerViewItemViewHolder(HorizontalRecyclerViewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            TABLE_HEADER_VIEW -> TableHeaderViewHolder(AppRoutingTableHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> AppListTitleViewHolder(AppTitleViewBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(items[position].viewType) {
            SECTION_HEADER_VIEW -> (holder as AppListTitleViewHolder).bind(items[position])
            HORIZONTAL_RECYCLER_VIEW -> (holder as HorizontalRecyclerViewItemViewHolder).bind(items[position], torAppsAdapter, torAppsLayoutManager)
            CELL -> (holder as AppListItemViewHolder).bind(items[position], position)
            TABLE_HEADER_VIEW -> (holder as TableHeaderViewHolder).bind(preferenceHelper)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return items[position].viewType
    }

    fun update(list: List<AppItemModel>) {
        var mutableList = list.toMutableList()
        mutableList.add(0, AppItemModel(TABLE_HEADER_VIEW))
        items = mutableList
        notifyDataSetChanged()
    }

    internal class AppListTitleViewHolder(val binding: AppTitleViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(appItem: AppItemModel) {
            binding.tvTitle.text = appItem.text
        }
    }

    inner class AppListItemViewHolder(val binding: AppSwitchItemViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val TAG = "AppListItemViewHolder";
        private var pos = -1

        fun bind(appItem: AppItemModel, pos: Int) {
            this.pos = pos
            binding.ivAppImage.setImageDrawable(appItem.icon)
            binding.tvTitle.text = appItem.text
            binding.smItemSwitch.isChecked = appItem.isRoutingEnabled == true
            binding.tvTitle.setOnClickListener(View.OnClickListener {
                Log.d(TAG, "TODO: open detail screen for " + binding.tvTitle.text)
            })
            binding.smItemSwitch.setOnCheckedChangeListener { switchBtn, isChecked ->
                if (switchBtn.isPressed) {
                    val itemModel = items[pos]
                    itemModel.isRoutingEnabled = isChecked
                    // pos - 1: the first item is the header view, which is manually added only here in AppListAdapter
                    onItemModelChanged?.invoke(pos - 1, itemModel)
                }
            }
            binding.smItemSwitch.isEnabled = appItem.protectAllApps == false
        }
    }

    internal class HorizontalRecyclerViewItemViewHolder(val binding: HorizontalRecyclerViewItemBinding) :
            RecyclerView.ViewHolder(binding.root) {
        fun bind(appItem: AppItemModel, adapter: TorAppsAdapter, layoutManager: LinearLayoutManager) {
            binding.rvTorApps.adapter = adapter
            binding.rvTorApps.layoutManager = layoutManager
            appItem.appList.also {
                if (it != null) {
                    adapter.update(it)
                }
            }
        }
    }

    internal class TableHeaderViewHolder(val binding: AppRoutingTableHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(preferenceHelper: PreferenceHelper) {
            binding.smProtectAllApps.isChecked = preferenceHelper.protectAllApps
            binding.smProtectAllApps.setOnCheckedChangeListener { switchBtn, isChecked ->
                if (switchBtn.isPressed) {
                    preferenceHelper.protectAllApps = isChecked
                }
            }
        }
    }
}