package org.torproject.vpn.ui.appearancesettings.data

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.torproject.vpn.databinding.WallpaperViewBinding
import org.torproject.vpn.ui.appearancesettings.model.WallpaperModel

class WallpaperAdapter(
    private var list: List<WallpaperModel>,
    var onWallpaperSelected: ((item: WallpaperModel) -> Unit)? = null

) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = WallpaperViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WallpaperViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as WallpaperAdapter.WallpaperViewHolder).bind(list[position])
    }

    fun update(list: List<WallpaperModel>) {
        this.list = list
        notifyDataSetChanged()
    }

    inner class WallpaperViewHolder(val binding: WallpaperViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(wallpaperModel: WallpaperModel) {
            binding.ivWallpaper.setImageResource(wallpaperModel.drawableResId)
            binding.root.setOnClickListener {
                onWallpaperSelected?.invoke(wallpaperModel)
            }
        }
    }
}