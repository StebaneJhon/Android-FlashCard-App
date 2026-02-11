package com.soaharisonstebane.mneme.card

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.soaharisonstebane.mneme.R

class SearchResultHeaderAdapter(
    private val title: String
) : RecyclerView.Adapter<SearchResultHeaderAdapter.ViewHolder>(){
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.ly_search_result_header, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val layoutParams = holder.itemView.layoutParams
        if (layoutParams is StaggeredGridLayoutManager.LayoutParams) {
            layoutParams.isFullSpan = true
        }
        holder.itemView.findViewById<TextView>(R.id.tv_search_result_header).text = title
    }

    override fun getItemCount() = 1

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
}