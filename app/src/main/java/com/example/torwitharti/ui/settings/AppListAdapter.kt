package com.example.torwitharti.ui.settings

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.torwitharti.databinding.AppSwitchItemViewBinding
import com.example.torwitharti.databinding.AppTitleViewBinding
import com.example.torwitharti.databinding.HorizontalRecyclerViewItemBinding

class AppListAdapter(list: List<AppItemModel>,
                     var torAppsAdapter: TorAppsAdapter,
                     var torAppsLayoutManager: LinearLayoutManager
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val HEADER_VIEW = 0
        const val CELL = 1
        const val HORIZONTAL_RECYCLER_VIEW = 2
    }

    var items: MutableList<AppItemModel> = list.toMutableList()
    var onItemModelChanged: ((pos: Int, item: AppItemModel) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val viewHolder: RecyclerView.ViewHolder = when (viewType) {
            CELL -> AppListItemViewHolder(AppSwitchItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            HORIZONTAL_RECYCLER_VIEW -> HorizontalRecyclerViewItemViewHolder(HorizontalRecyclerViewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> AppListTitleViewHolder(AppTitleViewBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(items[position].viewType) {
            HEADER_VIEW -> (holder as AppListTitleViewHolder).bind(items[position])
            HORIZONTAL_RECYCLER_VIEW -> (holder as HorizontalRecyclerViewItemViewHolder).bind(items[position], torAppsAdapter, torAppsLayoutManager)
            CELL -> (holder as AppListItemViewHolder).bind(items[position], position)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return items[position].viewType
    }

    fun update(list: List<AppItemModel>) {
        items = list.toMutableList()
        notifyDataSetChanged()
    }

    fun updateItem(pos: Int, model: AppItemModel) {
        items.set(pos, model)
        notifyItemChanged(pos)
    }

    internal class AppListTitleViewHolder(binding: AppTitleViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val binding: AppTitleViewBinding

        init {
            this.binding = binding
        }

        fun bind(appItem: AppItemModel) {
            binding.tvTitle.text = appItem.text
        }
    }

    inner class AppListItemViewHolder(binding: AppSwitchItemViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val TAG = "AppListItemViewHolder";
        val binding: AppSwitchItemViewBinding
        private var pos = -1

        init {
            this.binding = binding
        }

        fun bind(appItem: AppItemModel, pos: Int) {
            this.pos = pos
            binding.ivAppImage.setImageDrawable(appItem.icon)
            binding.tvTitle.text = appItem.text
            binding.smItemSwitch.isChecked = appItem.isRoutingEnabled == true
            binding.tvTitle.setOnClickListener(View.OnClickListener {
                Log.d(TAG, "TODO: open detail screen for " + binding.tvTitle.text)
            })
            binding.smItemSwitch.setOnCheckedChangeListener(
                CompoundButton.OnCheckedChangeListener { switchBtn, isChecked ->
                    if (switchBtn.isPressed) {
                        val itemModel = items[pos]
                        itemModel.isRoutingEnabled = isChecked
                        onItemModelChanged?.invoke(pos, itemModel)
                    }
                }
            )
        }
    }

    internal class HorizontalRecyclerViewItemViewHolder(binding: HorizontalRecyclerViewItemBinding) :
            RecyclerView.ViewHolder(binding.root) {
        private val TAG = "HorizontalRecyclerViewItemViewHolder";
        val binding: HorizontalRecyclerViewItemBinding
        init {
            this.binding = binding
        }

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
}