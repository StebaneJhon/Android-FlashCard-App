package com.soaharisonstebane.mneme.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.soaharisonstebane.mneme.backend.entities.relations.CardWithContentAndDefinitions
import com.soaharisonstebane.mneme.databinding.ItemImportedCardPreviewBinding

class ImportedCardsFronDevicePreviewRecyclerViewAdapter(
    val cards: List<CardWithContentAndDefinitions>
): RecyclerView.Adapter<ImportedCardsFronDevicePreviewRecyclerViewAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemImportedCardPreviewBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        return holder.bind(cards[position])
    }

    override fun getItemCount() = cards.size

    inner class ViewHolder(
        private val binding: ItemImportedCardPreviewBinding
    ): RecyclerView.ViewHolder(binding.root) {

        fun bind(card: CardWithContentAndDefinitions) {
            binding.tvContent.text = card.contentWithDefinitions.content.contentText
            binding.tvDefinition.text = card.contentWithDefinitions.definitions.first().definitionText
        }

    }

}