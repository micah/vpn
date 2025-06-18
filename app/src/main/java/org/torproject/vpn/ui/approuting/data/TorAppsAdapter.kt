package org.torproject.vpn.ui.approuting.data

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import org.torproject.vpn.databinding.AppItemViewBinding
import org.torproject.vpn.ui.approuting.AppRoutingFragmentDirections
import org.torproject.vpn.ui.approuting.model.AppItemModel
import org.torproject.vpn.ui.glide.ApplicationInfoModel
import org.torproject.vpn.utils.navigateSafe

class TorAppsAdapter(list: List<AppItemModel> = emptyList()) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items: List<AppItemModel> = list

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
        if (list == items) {
            return
        }
        items = list
        notifyDataSetChanged()
    }

    internal class AppListViewHolder(val binding: AppItemViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private var pos = -1

        fun bind(appItem: AppItemModel, pos: Int) {
            this.pos = pos
            appItem.appId?.also {
                Glide.with(binding.root.context)
                    .load(ApplicationInfoModel(appItem.appId))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(binding.ivAppImage)
            } ?: run {
                binding.ivAppImage.setImageDrawable(null)
            }
            binding.tvAppTitle.text = appItem.text
            binding.root.setOnClickListener {
                if (appItem.appId != null && appItem.uid != null) {
                    val action = AppRoutingFragmentDirections.actionNavigationAppRoutingToAppDetailFragment(
                        argAppUID = appItem.uid,
                        argAppId = appItem.appId,
                        argAppName = appItem.text,
                        argIsBrowser = appItem.isBrowserApp ?: false,
                        argHasTorSupport = appItem.hasTorSupport ?: false)
                    binding.root.findNavController().navigateSafe(action)
                }
            }
        }
    }
}