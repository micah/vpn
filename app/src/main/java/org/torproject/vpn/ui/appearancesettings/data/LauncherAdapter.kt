package org.torproject.vpn.ui.appearancesettings.data

import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.torproject.vpn.databinding.AppItemViewBinding
import org.torproject.vpn.ui.appearancesettings.model.LauncherModel

class LauncherAdapter(
    private var list: List<LauncherModel>,
    var onAppIconSelected: ((item: LauncherModel) -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = AppItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AppItemViewItemViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as LauncherAdapter.AppItemViewItemViewHolder).bind(list[position])
    }

    fun update(list: List<LauncherModel>) {
        this.list = list
        notifyDataSetChanged()
    }

    inner class AppItemViewItemViewHolder(val binding: AppItemViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(launcherModel: LauncherModel) {
            binding.ivAppImage.setImageResource(launcherModel.drawableResId)
            binding.ivSelectedIcn.visibility = if (launcherModel.selected) VISIBLE else GONE
            binding.root.setOnClickListener {
                onAppIconSelected?.invoke(launcherModel)
            }
            binding.tvAppTitle.text = binding.root.context.getText(launcherModel.appNameResId)
        }
    }
}