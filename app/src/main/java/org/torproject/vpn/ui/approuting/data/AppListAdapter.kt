package org.torproject.vpn.ui.approuting.data

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.torproject.vpn.R
import org.torproject.vpn.databinding.AppRoutingTableHeaderBinding
import org.torproject.vpn.databinding.AppShowAppsViewBinding
import org.torproject.vpn.databinding.AppSwitchItemViewBinding
import org.torproject.vpn.databinding.AppTitleViewBinding
import org.torproject.vpn.databinding.HorizontalRecyclerViewItemBinding
import org.torproject.vpn.ui.approuting.AppRoutingFragmentDirections
import org.torproject.vpn.ui.approuting.model.AppItemModel
import org.torproject.vpn.ui.glide.ApplicationInfoModel
import org.torproject.vpn.utils.PreferenceHelper
import org.torproject.vpn.utils.navigateSafe
import java.lang.reflect.Type
import kotlin.math.exp

class AppListAdapter(
    list: List<AppItemModel>,
    private var torAppsAdapter: TorAppsAdapter,
    var preferenceHelper: PreferenceHelper
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val SECTION_HEADER_VIEW = 0
        const val CELL = 1
        const val HORIZONTAL_RECYCLER_VIEW = 2
        const val TABLE_HEADER_VIEW = 3
        const val SHOW_APPS_VIEW = 4

        val TAG: String = AppListAdapter::class.java.simpleName
    }

    var items: MutableList<AppItemModel> = list.toMutableList()
    var onItemModelChanged: ((pos: Int, item: AppItemModel) -> Unit)? = null
    var onProtectAllAppsChanged: ((isChecked: Boolean) -> Unit)? = null
    private var isExpanded = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val viewHolder: RecyclerView.ViewHolder = when (viewType) {
            CELL -> AppListItemViewHolder(AppSwitchItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            HORIZONTAL_RECYCLER_VIEW -> {
                Log.d(TAG, "setting horizontal RV adapter and LLM")
                val binding = HorizontalRecyclerViewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                binding.rvHorizontalAppIcons.adapter = torAppsAdapter
                HorizontalRecyclerViewItemViewHolder(binding)
            }
            TABLE_HEADER_VIEW -> TableHeaderViewHolder(AppRoutingTableHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            SHOW_APPS_VIEW -> ShowAppsViewHolder(AppShowAppsViewBinding.inflate(LayoutInflater.from(parent.context), parent, false))
           else -> AppListTitleViewHolder(AppTitleViewBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
        return viewHolder
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        onBindViewHolder(holder, position)
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (items[position].viewType) {
            SECTION_HEADER_VIEW -> (holder as AppListTitleViewHolder).bind(items[position])
            HORIZONTAL_RECYCLER_VIEW -> (holder as HorizontalRecyclerViewItemViewHolder).bind(items[position], torAppsAdapter)
            CELL -> (holder as AppListItemViewHolder).bind(items[position], position)
            TABLE_HEADER_VIEW -> (holder as TableHeaderViewHolder).bind(items[position])
            SHOW_APPS_VIEW -> (holder as ShowAppsViewHolder).bind(items[position])
        }
    }

    override fun getItemCount(): Int {
        return if (isExpanded) items.size else items.indexOfFirst { it.viewType == SHOW_APPS_VIEW } + 1
    }

    override fun getItemViewType(position: Int): Int {
        return items[position].viewType
    }

    fun update(list: List<AppItemModel>) {
        val mutableList = list.toMutableList()
        var protectAllAppsEntryChanged = false

        if (mutableList.isNotEmpty()) {
            mutableList.add(0, AppItemModel(TABLE_HEADER_VIEW, (mutableList.first { it.viewType == CELL }).protectAllApps))
        } else {
            mutableList.add(0, AppItemModel(TABLE_HEADER_VIEW, preferenceHelper.protectAllApps))
        }
        // The following comparison depends on AppItemModels equals() implementation
        // Hence, dataSetChanged remains false if protectAllApps of AppItemModel changed
        // b/c protectAllApps is excluded from AppItemModel's equals() method
        val dataSetChanged = mutableList != items
        if (items.isNotEmpty()) {
            protectAllAppsEntryChanged = items[0].protectAllApps != mutableList[0].protectAllApps
        }
        items = ArrayList(mutableList.map { it.copy() })
        if (dataSetChanged) {
            notifyDataSetChanged()
        } else if (protectAllAppsEntryChanged) {
            notifyItemChanged(0)
        }
    }

    inner class ShowAppsViewHolder(val binding: AppShowAppsViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(appItem: AppItemModel) {
            val ctx = binding.root.context
            if (isExpanded) {
                binding.tvTitle.text = ctx.getString(R.string.app_routing_hide_apps, appItem.text)
            } else {
                binding.tvTitle.text = ctx.getString(R.string.app_routing_show_apps, appItem.text)
            }
            binding.root.setOnClickListener {
                isExpanded = !isExpanded
                if (isExpanded) {
                    binding.ivIcon.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.carat_up))
                } else {
                    binding.ivIcon.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.carat_down))
                }
                notifyDataSetChanged()
            }
        }
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
            appItem.appId?.also {
                Log.d("--->", "load icn for ${appItem.appId}")
                Glide.with(binding.root.context)
                    .load(ApplicationInfoModel(appItem.appId))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .dontAnimate()
                    .let { requestBuilder ->
                        binding.ivAppImage.drawable?.let {
                            requestBuilder.placeholder(it)
                        } ?: run {
                            requestBuilder
                        }
                    }
                    .into(binding.ivAppImage)
            } ?: run {
                binding.ivAppImage.setImageDrawable(null)
            }
            binding.tvTitle.text = appItem.text
            binding.smItemSwitch.isChecked = appItem.isRoutingEnabled == true
            binding.tvTitle.setOnClickListener(View.OnClickListener {
                if (appItem.appId != null && appItem.uid != null) {
                    val action = AppRoutingFragmentDirections.actionNavigationAppRoutingToAppDetailFragment(
                        argAppUID = appItem.uid,
                        argAppId = appItem.appId,
                        argAppName = appItem.text,
                        argIsBrowser = appItem.isBrowserApp ?: false,
                        argHasTorSupport = appItem.hasTorSupport ?: false)
                    binding.root.findNavController().navigateSafe(action)
                }
            })
            binding.smItemSwitch.setOnCheckedChangeListener { switchBtn, isChecked ->
                if (switchBtn.isPressed) {
                    val appItemModelListType: Type = object : TypeToken<ArrayList<AppItemModel?>?>() {}.type
                    val allConfigurableApps = (Gson().fromJson<List<AppItemModel>>(preferenceHelper.cachedApps, appItemModelListType) ?: emptyList()).filter { it.viewType == CELL }
                    val itemModel = items[pos]
                    itemModel.isRoutingEnabled = isChecked
                    val protectedAppsSize = preferenceHelper.protectedApps?.size
                    if (!isChecked) {
                        itemModel.protectAllApps = false
                    } else if (protectedAppsSize == allConfigurableApps.size - 1) {
                        // isChecked == true and thus all apps will be protected now
                        itemModel.protectAllApps = true
                    }
                    // pos - 1: the first item is the header view, which is manually added only here in AppListAdapter
                    onItemModelChanged?.invoke(pos - 1, itemModel)
                }
            }
        }
    }

    internal class HorizontalRecyclerViewItemViewHolder(val binding: HorizontalRecyclerViewItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            appItem: AppItemModel,
            adapter: TorAppsAdapter
        ) {
            appItem.appList?.also {
                Log.d("--", "updateAppList $it")
                adapter.update(it)
            }
        }
    }

    inner class TableHeaderViewHolder(val binding: AppRoutingTableHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AppItemModel) {
            binding.smProtectAllApps.isChecked = item.protectAllApps == true
            binding.smProtectAllApps.setOnCheckedChangeListener { switchBtn, isChecked ->
                if (switchBtn.isPressed) {
                    onProtectAllAppsChanged?.invoke(isChecked)
                }
            }
        }
    }
}