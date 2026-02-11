package com.ssoaharison.recall.card

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.ssoaharison.recall.R
import com.ssoaharison.recall.databinding.LyCardListHeaderBinding
import com.ssoaharison.recall.util.ItemLayoutManager.GRID

class CardListHeaderAdapter(
    private val viewMode: String,
    private val sortCards: () -> Unit,
    private val updateViewMode: () -> Unit,
): RecyclerView.Adapter<CardListHeaderAdapter.ViewHolder>(){
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = LyCardListHeaderBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val layoutParams = holder.itemView.layoutParams
        if (layoutParams is StaggeredGridLayoutManager.LayoutParams) {
            layoutParams.isFullSpan = true
        }
        return holder.bind(viewMode, sortCards, updateViewMode)
    }

    override fun getItemCount() = 1

    inner class ViewHolder(private val binding: LyCardListHeaderBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(viewMode: String, sortCards: () -> Unit, updateViewMode: () -> Unit) {
            val icon = if (viewMode == GRID) R.drawable.icon_view_agenda else R.drawable.icon_dashboard
            binding.btViewMode.setIconResource(icon)
            binding.btSortCards.setOnClickListener{
                sortCards()
            }
            binding.btViewMode.setOnClickListener{
                updateViewMode()
            }
        }
    }
}