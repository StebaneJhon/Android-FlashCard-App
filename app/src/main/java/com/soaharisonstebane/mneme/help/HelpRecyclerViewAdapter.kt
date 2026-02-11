package com.soaharisonstebane.mneme.help

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.soaharisonstebane.mneme.R
import com.soaharisonstebane.mneme.util.FAQDataModel

class HelpRecyclerViewAdapter(
    private val faqDataList: List<FAQDataModel>
): RecyclerView.Adapter<HelpRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.create(parent)
    }

    override fun getItemCount() = faqDataList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        return holder.bind(faqDataList[position])
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {

        private val tvQuestion: TextView = view.findViewById(R.id.tv_question)
        private val tvAnswer: TextView = view.findViewById(R.id.tv_answer)
        private val btExpandMinimiseBt: ImageButton = view.findViewById(R.id.bt_expand_minimise)
        private val clItem: ConstraintLayout = view.findViewById(R.id.cl_help_center_item_root)

        fun bind(
            faqDataModel: FAQDataModel
        ) {
            tvQuestion.text = faqDataModel.question
            tvAnswer.text = faqDataModel.answer
            btExpandMinimiseBt.setOnClickListener {
                onShowAnswer()
            }
            clItem.setOnClickListener {
                onShowAnswer()
            }
        }

        private fun onShowAnswer() {
            tvAnswer.isVisible = !tvAnswer.isVisible
            if (tvAnswer.isVisible) {
                btExpandMinimiseBt.setImageResource(R.drawable.icon_expand_less)
            } else {
                btExpandMinimiseBt.setImageResource(R.drawable.icon_expand_more)
            }
        }

        companion object {
            fun create(parent: ViewGroup): ViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.ly_help_center_item, parent, false)
                return ViewHolder(view)
            }
        }

    }

}