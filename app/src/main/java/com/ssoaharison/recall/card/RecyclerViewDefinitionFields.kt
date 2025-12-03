package com.ssoaharison.recall.card

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.doOnNextLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputEditText
import com.ssoaharison.recall.R

class RecyclerViewDefinitionFields(
    private val context: Context,
    private val fields: List<AddCardItemModel>,
    private val onDeleteDefinitionField: (String) -> Unit,
    private val onEditDefinitionField: (AddCardItemModel.DefinitionFieldModel, Int) -> Unit,
    private val onEditContentField: (AddCardItemModel.ContentFieldModel) -> Unit,
    private val onEditContentLanguage: () -> Unit,
    private val onEditDefinitionLanguage: () -> Unit,
    private val onFocused: (Int) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var onBindEditText: ((editText: TextInputEditText, position: Int) -> Unit)? = null

    companion object {
        private const val TYPE_CONTENT = 0
        private const val TYPE_DEFINITION = 1
        private const val TYPE_LANGUAGE = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (fields[position]) {
            is AddCardItemModel.ContentFieldModel -> TYPE_CONTENT
            is AddCardItemModel.DefinitionFieldModel -> TYPE_DEFINITION
            is AddCardItemModel.LanguageModel -> TYPE_LANGUAGE
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_CONTENT -> {
                val view = inflater.inflate(R.layout.ly_add_card_content_field, parent, false)
                ContentViewHolder(view)
            }

            TYPE_DEFINITION -> {
                val view = inflater.inflate(R.layout.ly_add_card_definition_field, parent, false)
                DefinitionViewHolder(view)
            }

            TYPE_LANGUAGE -> {
                val view = inflater.inflate(R.layout.ly_add_card_language_selector, parent, false)
                LanguageViewHolder(view)
            }

            else -> throw IllegalArgumentException("Invalide view type")
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        when (val field = fields[position]) {
            is AddCardItemModel.ContentFieldModel -> {
                (holder as ContentViewHolder).bind(
                    context = context,
                    field = field,
                    onEditContentField = onEditContentField,
                    onFocused = onFocused,
                )
            }

            is AddCardItemModel.DefinitionFieldModel -> {
                (holder as DefinitionViewHolder).bind(
                    context = context,
                    field = field,
                    onDeleteField = onDeleteDefinitionField,
                    onEditField = onEditDefinitionField,
                    onFocused = onFocused
                )
            }

            is AddCardItemModel.LanguageModel -> {
                (holder as LanguageViewHolder).bind(
                    context = context,
                    language = field,
                    onEditContentLanguage = onEditContentLanguage,
                    onEditDefinitionLanguage = onEditDefinitionLanguage
                )
            }
        }
    }

    override fun getItemCount() = fields.size

    class ContentViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val tieContent: TextInputEditText = view.findViewById(R.id.tie_content_text)
        val llContentContainerAudio: LinearLayout = view.findViewById(R.id.ll_content_container_audio)
        val btContentPlayAudio: MaterialButton = view.findViewById(R.id.bt_content_play_audio)
        val sliderContentAudio: Slider = view.findViewById(R.id.slider_content_audio)
        val clContentContainerImage: ConstraintLayout =
            view.findViewById(R.id.cl_content_container_image)
        val imgContentPhoto: ImageView = view.findViewById(R.id.img_content_photo)
        val btContentDeleteImage: MaterialButton = view.findViewById(R.id.bt_content_delete_image)

        private var textWatcher: TextWatcher? = null
        var imm: InputMethodManager? = null

        fun bind(
            context: Context,
            field: AddCardItemModel.ContentFieldModel,
            onEditContentField: (AddCardItemModel.ContentFieldModel) -> Unit,
            onFocused: (Int) -> Unit
        ) {

            imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            val contentImage = field.contentImage
            if (contentImage == null) {
                clContentContainerImage.visibility = View.GONE
            } else {
                clContentContainerImage.visibility = View.VISIBLE
                imgContentPhoto.setImageBitmap(contentImage.bmp)
            }

            // TODO: Bind audio
            val contentAudio = null
            if (contentAudio == null) {
                llContentContainerAudio.visibility = View.GONE
            } else {
                llContentContainerAudio.visibility = View.VISIBLE
            }

            val contentText = field.contentText
            if (contentText == null) {

            } else {
                tieContent.setText(contentText)
            }
            tieContent.onFocusChangeListener = null
            textWatcher?.let { tieContent.removeTextChangedListener(it) }
            val watcher = object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {}
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val currentPos = bindingAdapterPosition
                    if (currentPos != RecyclerView.NO_POSITION) {
                        onEditContentField(field.copy(contentText = s?.toString()))
                    }
                }
            }
            tieContent.addTextChangedListener(watcher)
            textWatcher = watcher

            tieContent.setOnFocusChangeListener { _, hasFocus ->
                val currentPos = bindingAdapterPosition
                if (currentPos != RecyclerView.NO_POSITION && hasFocus) {
//                    onFocused(currentPos)
                }
            }

            if (field.hasFocus) {
                tieContent.requestFocus()
                tieContent.post {
                    imm?.showSoftInput(tieContent, InputMethodManager.SHOW_IMPLICIT)
                }
            } else {
                tieContent.clearFocus()
                imm?.hideSoftInputFromWindow(tieContent.windowToken, 0)
            }
        }
    }

    class DefinitionViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val textField: TextInputEditText = view.findViewById(R.id.tie_text)
        val imageField: ImageView = view.findViewById(R.id.img_photo)
        val btDeleteImage: MaterialButton = view.findViewById(R.id.bt_delete_image)
        val clImageContainer: ConstraintLayout = view.findViewById(R.id.cl_container_image)
        val llAudioContainer: LinearLayout = view.findViewById(R.id.ll_container_audio)
        val chip: TextView = view.findViewById(R.id.bt_is_true)
        val btDeleteField: MaterialButton = view.findViewById(R.id.bt_delete_field)

        private var textWatcher: TextWatcher? = null

        var imm: InputMethodManager? = null


        fun bind(
            context: Context,
            field: AddCardItemModel.DefinitionFieldModel,
            onDeleteField: (String) -> Unit,
            onEditField: (AddCardItemModel.DefinitionFieldModel, Int) -> Unit,
            onFocused: (Int) -> Unit
        ) {
            imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            val pos = bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                if (pos == 0) {
                    btDeleteField.visibility = View.GONE
                } else {
                    btDeleteField.visibility = View.VISIBLE
                }
            }

            textWatcher?.let { textField.removeTextChangedListener(it) }
            field.definitionText?.let { textField.setText(it) }
            val watcher = object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {}
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val currentPos = bindingAdapterPosition
                    if (currentPos != RecyclerView.NO_POSITION) {
                        onEditField(field.copy(definitionText = s?.toString()), currentPos)
                    }
                }
            }
            textField.addTextChangedListener(watcher)
            textWatcher = watcher
            textField.setOnFocusChangeListener { _, hasFocus ->
                val currentPos = bindingAdapterPosition
                if (currentPos != RecyclerView.NO_POSITION && hasFocus) {
                    onFocused(currentPos)
                }
            }

            field.definitionImage?.let { image ->
                clImageContainer.visibility = View.VISIBLE
                imageField.setImageBitmap(image.bmp)
            }
            if (field.hasFocus) {
                textField.requestFocus()
                textField.post {
                    imm?.showSoftInput(textField, InputMethodManager.SHOW_IMPLICIT)
                }
            } else {
                textField.clearFocus()
                imm?.hideSoftInputFromWindow(textField.windowToken, 0)
            }

            onClickChip(field.isCorrectDefinition, chip)
            chip.setOnClickListener {
                onClickChip(!field.isCorrectDefinition, chip)
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onEditField(field.copy(isCorrectDefinition = !field.isCorrectDefinition), pos)
                }
            }
            btDeleteField.setOnClickListener {
                onDeleteField(field.definitionId)
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

    }

    class LanguageViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val tvType: TextView = view.findViewById(R.id.tv_type)
        val btLanguage: MaterialButton = view.findViewById(R.id.bt_language)

        fun bind(
            context: Context,
            language: AddCardItemModel.LanguageModel,
            onEditDefinitionLanguage: () -> Unit,
            onEditContentLanguage: () -> Unit
        ) {
            tvType.text = language.type
            btLanguage.setOnClickListener {
                when (language.type) {
                    context.getString(R.string.text_content) -> {
                        onEditContentLanguage()
                    }

                    context.getString(R.string.text_definition) -> {
                        onEditDefinitionLanguage()
                    }
                }
            }
        }
    }

}