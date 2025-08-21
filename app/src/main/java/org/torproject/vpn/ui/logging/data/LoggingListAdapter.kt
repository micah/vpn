package org.torproject.vpn.ui.logging.data

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.torproject.vpn.R
import org.torproject.vpn.databinding.LogItemBinding
import org.torproject.vpn.utils.getFormattedDate
import org.torproject.onionmasq.logging.LogItem
import org.torproject.onionmasq.logging.LogObservable
import java.util.*

class LoggingListAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var items: MutableList<LogItem> = LogObservable.getInstance().logList.toMutableList()
    private var bottomInset = 0
    private var bottomCompactPadding = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        bottomCompactPadding = parent.context.resources.getDimension(R.dimen.compact_padding).toInt()
        val binding = LogItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LogListItemViewHolder(binding)
    }

    fun setBottomInset(insetBottom: Int) {
        bottomInset = insetBottom
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as LogListItemViewHolder).bind(items[position], position)
    }

    fun update(list: List<LogItem>) {
        items = list.toMutableList()
        notifyDataSetChanged()
    }

   inner class LogListItemViewHolder(val binding: LogItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: LogItem, position: Int) {
            if (position == 0) {
                binding.container.background = ContextCompat.getDrawable(binding.root.context, R.drawable.log_item_top_background)
                binding.container.setPadding(binding.container.paddingLeft, binding.container.paddingTop, binding.container.paddingRight, bottomCompactPadding)
            } else if (position == (items.size - 1)) {
                binding.container.background = ContextCompat.getDrawable(binding.root.context, R.drawable.log_item_bottom_background)
                // set extra-padding to the last item
                binding.container.setPadding(
                    binding.container.paddingLeft,
                    binding.container.paddingTop,
                    binding.container.paddingRight,
                    bottomCompactPadding + bottomInset)
            } else {
                binding.container.background = null
                binding.container.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.surface_container_low))
                binding.container.setPadding(binding.container.paddingLeft, binding.container.paddingTop, binding.container.paddingRight, bottomCompactPadding)
            }
            binding.tvTimestamp.text = getFormattedDate(item.timestamp, Locale.getDefault())
            binding.tvLog.text = item.content
        }
   }

}