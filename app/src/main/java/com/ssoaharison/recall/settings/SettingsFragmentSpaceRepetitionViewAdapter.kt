package com.ssoaharison.recall.settings

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.ssoaharison.recall.R
import com.ssoaharison.recall.backend.Model.ImmutableSpaceRepetitionBox

class SettingsFragmentSpaceRepetitionViewAdapter(
    private val context: Context,
    private val boxLevelList: List<ImmutableSpaceRepetitionBox>,
    private val boxLevelClickList: (ImmutableSpaceRepetitionBox) -> Unit
    ): RecyclerView.Adapter<SettingsFragmentSpaceRepetitionViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.create(parent)
    }

    override fun getItemCount(): Int {
        return boxLevelList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        return holder.bind(
            context,
            boxLevelList[position],
            boxLevelClickList
        )
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {

        private val tvSpaceRepetitionForgettingDay: TextView = view.findViewById(R.id.tv_space_repetition_forgetting_day)
        private val tvSpaceRepetitionLevel: TextView = view.findViewById(R.id.tv_space_repetition_level_name)
        private val llSpaceRepetitionSectionItem: ConstraintLayout = view.findViewById(R.id.ll_space_repetition_section_item)

        fun bind(
            context: Context,
            boxLevel: ImmutableSpaceRepetitionBox,
            boxLevelClickList: (ImmutableSpaceRepetitionBox) -> Unit
        ) {

            tvSpaceRepetitionForgettingDay.text = boxLevel.levelRepeatIn.toString()
            tvSpaceRepetitionLevel.text = boxLevel.levelName
            llSpaceRepetitionSectionItem.setOnClickListener {
                boxLevelClickList(boxLevel)
            }
        }

        companion object {
            fun create(parent: ViewGroup): ViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.ly_settings_space_repetition_card, parent, false)
                return ViewHolder(view)
            }
        }

    }



}