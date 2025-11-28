package com.ssoaharison.recall.card

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.ssoaharison.recall.R

class RecyclerViewDefinitionFields(
    private val context: Context,
    private val definitionFields: ArrayList<DefinitionFieldModel>,
    private val onDeleteField: (DefinitionFieldModel) -> Unit,
    private val onEditField: (DefinitionFieldModel, Int) -> Unit,
    private val onFocused: (Int) -> Unit,
): RecyclerView.Adapter<RecyclerViewDefinitionFields.ViewHolder>() {
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
        return holder.bind(
            position,
            definitionFields[position],
            onDeleteField,
            onEditField,
            onFocused
        )
    }

    override fun getItemCount(): Int {
        return definitionFields.size
    }


    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {

        val textField: TextInputEditText = view.findViewById(R.id.tie_text)
        val imageField: ImageView = view.findViewById(R.id.img_photo)
        val btDeleteImage: MaterialButton = view.findViewById(R.id.bt_delete_image)
        val clImageContainer: ConstraintLayout = view.findViewById(R.id.cl_container_image)
        val llAudioContainer: LinearLayout = view.findViewById(R.id.ll_container_audio)
        val chip: TextView = view.findViewById(R.id.bt_is_true)
        val btDeleteField: MaterialButton = view.findViewById(R.id.bt_delete_field)

        fun bind(
            position: Int,
            field: DefinitionFieldModel,
            onDeleteField: (DefinitionFieldModel) -> Unit,
            onEditField: (DefinitionFieldModel, Int) -> Unit,
            onFocused: (Int) -> Unit
        ) {
            if (position == 0) {
                btDeleteField.visibility = View.GONE
            }
            field.definitionText?.let { text ->
                textField.setText(text)
            }
            field.definitionImage?.let { image ->
                clImageContainer.visibility = View.VISIBLE
                imageField.setImageBitmap(image.bmp)
            }
            chip.setOnClickListener {
                onClickChip(!field.isCorrectDefinition, chip)
                onEditField(field.copy(isCorrectDefinition = !field.isCorrectDefinition), position)
            }
            btDeleteField.setOnClickListener {
                onDeleteField(field)
            }
            textField.apply {
                setOnFocusChangeListener {v, hasFocus ->
                    onFocused(position)
                }
                addTextChangedListener { text ->
                    onEditField(field.copy(definitionText = text.toString()), position)
                }
            }
        }

        private fun onClickChip(state: Boolean, chip: TextView) {
            if (state) {
                checkChip(chip)
            } else {
                unCheckChip(chip)
            }
        }

        private fun checkChip(chip: TextView) {
            chip.apply {
                background.setTint(ContextCompat.getColor(context, R.color.green200))
                setTextColor(ContextCompat.getColor(context, R.color.green500))
                text = context.getString(R.string.cp_true_text)
            }
        }

        private fun unCheckChip(chip: TextView) {
            chip.apply {
                background.setTint(ContextCompat.getColor(context, R.color.red200))
                setTextColor(ContextCompat.getColor(context, R.color.red500))
                text = context.getString(R.string.cp_false_text)
            }
        }

        companion object {
            fun create(parent: ViewGroup): ViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.ly_add_card_field, parent, false)
                return ViewHolder(view)
            }
        }

    }

}