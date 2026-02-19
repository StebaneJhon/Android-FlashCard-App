package com.soaharisonstebane.mneme.helper

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.soaharisonstebane.mneme.home.CardsRecyclerViewAdapter
import com.soaharisonstebane.mneme.util.ItemLayoutManager.GRID

class CardOnlySpacingDecoration(
    private val spacing: Int,
    private val concatAdapter: ConcatAdapter,
    private val cardAdapter: CardsRecyclerViewAdapter,
    private val viewMode: String
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION) return

        val subAdapter = concatAdapter.findRelativeAdapterPositionIn(cardAdapter, parent.getChildViewHolder(view), position)

        if (subAdapter != RecyclerView.NO_POSITION) {
            val params = view.layoutParams as StaggeredGridLayoutManager.LayoutParams
            val spanIndex = params.spanIndex

            if (viewMode == GRID) {
                if (spanIndex == 0) {
                    outRect.left = spacing
                    outRect.right = spacing / 2
                } else {
                    outRect.left = spacing / 2
                    outRect.right = spacing
                }
            } else {
                outRect.left = spacing
                outRect.right = spacing
            }
            outRect.bottom = spacing
        } else {
            outRect.set(0, 0, 0, 0)
        }
    }
}