package com.example.flashcard.quiz.timedFlashCardGame

import androidx.recyclerview.widget.DiffUtil
import com.example.flashcard.backend.Model.ImmutableCard

class CardStackCallback(
    private val old: List<ImmutableCard>,
    private val baru: List<ImmutableCard>
): DiffUtil.Callback() {
    override fun getOldListSize() = old.size

    override fun getNewListSize() = baru.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return old.get(oldItemPosition).backgroundImg == baru.get(newItemPosition).backgroundImg
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return old.get(oldItemPosition) == baru.get(newItemPosition)
    }
}