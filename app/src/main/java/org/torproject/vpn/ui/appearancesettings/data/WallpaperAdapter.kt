package org.torproject.vpn.ui.appearancesettings.data

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import org.torproject.vpn.R
import org.torproject.vpn.databinding.WallpaperViewBinding
import org.torproject.vpn.ui.appearancesettings.model.WallpaperModel
import org.torproject.vpn.utils.getDpInPx

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
        (holder as WallpaperAdapter.WallpaperViewHolder).bind(list[position], position)
    }

    fun update(list: List<WallpaperModel>) {
        this.list = list
        notifyDataSetChanged()
    }

    inner class WallpaperViewHolder(val binding: WallpaperViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(wallpaperModel: WallpaperModel, position: Int) {
            val marginLayoutParams = binding.root.layoutParams as ViewGroup.MarginLayoutParams
            val context = binding.root.context
            val horizontalMargin = getDpInPx(context, 12f)
            val topPadding = context.resources.getDimensionPixelSize(R.dimen.default_padding)
            val bottomPadding = getDpInPx(context, 4f)
            if (position == 0) {
                if (list.size > 1) {
                    binding.root.background = AppCompatResources.getDrawable(context, R.drawable.bg_rounded_left)
                    marginLayoutParams.setMargins(horizontalMargin,0,0,0)
                    binding.root.setPadding(horizontalMargin, topPadding, 0, bottomPadding)
                } else {
                    binding.root.background = AppCompatResources.getDrawable(context, R.drawable.bg_rounded)
                    marginLayoutParams.setMargins(horizontalMargin,0, horizontalMargin,0)
                    binding.root.setPadding(horizontalMargin, topPadding, horizontalMargin, bottomPadding)

                }
            } else if (position > 0 && position < list.size - 1) {
                binding.root.setBackgroundColor(context.getColor(R.color.purpleStatsBg))
                marginLayoutParams.setMargins(0,0, 0,0)
                binding.root.setPadding(0, topPadding, 0, bottomPadding)
            } else {
                binding.root.background = AppCompatResources.getDrawable(context, R.drawable.bg_rounded_right)
                marginLayoutParams.setMargins(0,0, horizontalMargin,0)
                binding.root.setPadding(0, topPadding, horizontalMargin, bottomPadding)
            }
            binding.ivWallpaper.setImageResource(wallpaperModel.drawableResId)
            binding.ivSelectedIcn.visibility = if (wallpaperModel.selected) VISIBLE else GONE
            binding.tvWallpaperDescription.setText(wallpaperModel.descriptionResName)
            binding.tvWallpaperDescription.gravity = if (wallpaperModel.selected) Gravity.START else Gravity.CENTER_HORIZONTAL
            binding.root.setOnClickListener {
                onWallpaperSelected?.invoke(wallpaperModel)
            }
            binding.root.layoutParams = marginLayoutParams
        }
    }
}