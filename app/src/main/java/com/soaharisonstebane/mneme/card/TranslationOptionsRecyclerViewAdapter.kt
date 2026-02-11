package com.soaharisonstebane.mneme.card

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.soaharisonstebane.mneme.R

class TranslationOptionsRecyclerViewAdapter(
    private val options: List<String>,
    private val onOptionSelected: (String) -> Unit
): RecyclerView.Adapter<TranslationOptionsRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder.create(parent)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.bind(
            option = options[position],
            onOptionSelected = onOptionSelected
        )
    }

    override fun getItemCount(): Int {
        return options.size
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        private val btOption: Button = view.findViewById(R.id.bt_option)

        fun bind(
            option: String,
            onOptionSelected: (String) -> Unit
        ) {
            btOption.text = option
            btOption.setOnClickListener {
                onOptionSelected(option)
            }
        }

        companion object {
            fun create(parent: ViewGroup): ViewHolder {
                return ViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.ly_item_text,
                        parent, false
                    )
                )
            }
        }

    }

}