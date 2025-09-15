package com.example.kalpeshdemo.ui.portfolio

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kalpeshdemo.R
import com.example.kalpeshdemo.data.model.HoldingData
import com.example.kalpeshdemo.databinding.HoldingListBinding
import com.example.kalpeshdemo.utils.formatAsCurrency

class HoldingAdapter : ListAdapter<HoldingData, HoldingAdapter.ViewHolder>(HoldingDiffCallback()) {

    class ViewHolder(
        private val binding: HoldingListBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: HoldingData) {
            with(binding) {
                val context = itemView.context
                lblSymbol.text = item.symbol
                lblLtp.text = context.formatAsCurrency(item.ltp)
                lblNetQty.text = item.quantity.toString()
                lblPnL.text = context.formatAsCurrency(item.pnl)
                val colorRes = if (item.pnl > 0) R.color.green else R.color.red
                lblPnL.setTextColor(ContextCompat.getColor(context, colorRes))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = HoldingListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    private class HoldingDiffCallback : DiffUtil.ItemCallback<HoldingData>() {
        override fun areItemsTheSame(oldItem: HoldingData, newItem: HoldingData): Boolean {
            return oldItem.symbol == newItem.symbol
        }

        override fun areContentsTheSame(oldItem: HoldingData, newItem: HoldingData): Boolean {
            return oldItem == newItem
        }
    }
}