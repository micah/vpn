package org.torproject.vpn.ui.generalsettings.data

import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.torproject.vpn.R
import org.torproject.vpn.databinding.AppItemViewBinding
import org.torproject.vpn.ui.generalsettings.model.LauncherModel

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
            val backgroundResource = if (launcherModel.selected) R.drawable.app_item_background_selected else R.drawable.app_item_background
            binding.ivAppImageBackground.setBackgroundResource(backgroundResource)
            binding.root.setOnClickListener {
                onAppIconSelected?.invoke(launcherModel)
            }
            binding.tvAppTitle.text = binding.root.context.getText(launcherModel.appNameResId)
            val textColor = if (launcherModel.selected) R.color.secondary else R.color.white
            binding.tvAppTitle.setTextColor(ContextCompat.getColor(binding.root.context, textColor))
        }
    }
}