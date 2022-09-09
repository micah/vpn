package com.example.torwitharti.ui.settings

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.torwitharti.databinding.AppItemViewBinding

class TorAppsAdapter(list: List<AppItemModel>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var items: List<AppItemModel> = list

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return AppListViewHolder(AppItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val applistHolder = holder as AppListViewHolder
        applistHolder.bind(items[position], position)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun update(list: List<AppItemModel>) {
        items = list
        notifyDataSetChanged()
    }


    internal class AppListViewHolder(val binding: AppItemViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private var pos = -1

        fun bind(appItem: AppItemModel, pos: Int) {
            this.pos = pos
            binding.ivAppImage.setImageDrawable(appItem.icon)
            binding.tvAppTitle.text = appItem.text
            binding.root.setOnClickListener {
                Log.d("TorAppsAdapter", "TODO: open  detail screen for "  + appItem.text)
            }
        }
    }
}