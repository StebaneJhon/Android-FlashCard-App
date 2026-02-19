package com.soaharisonstebane.mneme.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.soaharisonstebane.mneme.databinding.LyDeckListHeaderBinding

class DeckListHeaderAdapter(
    private val sortDeck: () -> Unit,
): RecyclerView.Adapter<DeckListHeaderAdapter.ViewHolder>(){
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = LyDeckListHeaderBinding.inflate(layoutInflater, parent, false)
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
        return holder.bind(sortDeck)
    }

    override fun getItemCount() = 1

    inner class ViewHolder(private val binding: LyDeckListHeaderBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(sortDeck: () -> Unit) {
            binding.btSortSubdecks.setOnClickListener{
                sortDeck()
            }
        }
    }
}