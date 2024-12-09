package com.ssoaharison.recall.settings

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialogFragment
import com.ssoaharison.recall.R
import com.ssoaharison.recall.backend.Model.ImmutableSpaceRepetitionBox
import com.ssoaharison.recall.backend.entities.SpaceRepetitionBox
import com.ssoaharison.recall.util.CardLevel.L1
import com.ssoaharison.recall.util.CardLevel.L7
import com.ssoaharison.recall.helper.SpaceRepetitionAlgorithmHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class SettingsFragmentEditBoxLevelDialog(
    private val boxLevel: ImmutableSpaceRepetitionBox,
    private val boxLevelList: List<ImmutableSpaceRepetitionBox>
): AppCompatDialogFragment(), TextView.OnEditorActionListener {

    private var appContext: Context? = null
    private var til: TextInputLayout? = null
    private var ti: TextInputEditText? = null
    private var listener: SettingsFragmentEditBoxLevelDialogListener? = null
    private var imm: InputMethodManager? = null
    private lateinit var spaceRepetitionAlgorithmHelper: SpaceRepetitionAlgorithmHelper


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = MaterialAlertDialogBuilder(requireActivity(), R.style.ThemeOverlay_App_MaterialAlertDialog)
        val inflater = activity?.layoutInflater
        val view = inflater?.inflate(R.layout.dl_box_level_update, null)

        appContext = activity?.applicationContext
        imm = appContext?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        spaceRepetitionAlgorithmHelper =  SpaceRepetitionAlgorithmHelper()


        til = view?.findViewById(R.id.til_level_repeat_day)
        ti = view?.findViewById(R.id.ti_level_repeat_day)

        ti?.setText(boxLevel.levelRepeatIn.toString())

        ti?.setOnEditorActionListener(this)
        til?.setEndIconOnClickListener {
            MaterialAlertDialogBuilder(requireActivity(), R.style.ThemeOverlay_App_MaterialAlertDialog)
                .setTitle("Repeat Day")
                .setMessage("Number of days after which, cards in this level will be forgotten. It has to be superior to the previous level and inferior to the nex. Ex. L1 -> 0, L2 -> 1, L3 -> 4, ...")
                .setNeutralButton("Ok") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()

        }

        builder.setView(view)
            .setTitle("Update ${boxLevel.levelName}")
            .setNegativeButton("Cancel") {_, _ -> dismiss()}
            .setPositiveButton("Update") {_, _ ->
                if(onUpdate()) {
                    val newRepeatInDay = ti?.text.toString().toInt()
                    updateBoxLevel(newRepeatInDay)
                } else {
                    Toast.makeText(appContext!!, "Failed to update at box level", Toast.LENGTH_LONG).show()
                }
            }

        return builder.create()
    }

    private fun onUpdate(): Boolean {
        val newRepeatInDay = ti?.text.toString().toInt()
        when (boxLevel.levelName) {
            L1 -> {
                val nextBoxLevelRepeatDay = boxLevelList[1].levelRepeatIn ?: 1
                return if (newRepeatInDay > nextBoxLevelRepeatDay || newRepeatInDay < 0) {
                    til?.error =
                        "Repetition day level must be inferior to the next repetition day level"
                    false
                } else {
                    true
                }
            }
            L7 -> {
                val previousBoxLevelRepeatDay =
                    boxLevelList[boxLevelList.indexOf(boxLevel) - 1].levelRepeatIn
                return if (newRepeatInDay < previousBoxLevelRepeatDay!!) {
                    til?.error =
                        "Repetition day level must be superior to the next previous day level"
                    false
                } else {
                    true
                }
            }
            else -> {
                val previousBoxLevelRepeatDay =
                    boxLevelList[boxLevelList.indexOf(boxLevel) - 1].levelRepeatIn
                val nextBoxLevelRepeatDay =
                    boxLevelList[boxLevelList.indexOf(boxLevel) + 1].levelRepeatIn

                when {
                    newRepeatInDay < previousBoxLevelRepeatDay!! -> {
                        til?.error =
                            "Repetition day level must be superior to the next previous day level"
                        return false
                    }

                    newRepeatInDay > nextBoxLevelRepeatDay!! -> {
                        til?.error =
                            "Repetition day level must be inferior to the next repetition day level"
                        return false
                    }

                    else -> {
                        return true
                    }
                }
            }
        }
    }
    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        return if(v == ti) {
            if(onUpdate()) {
                imm?.hideSoftInputFromWindow(v?.windowToken, 0)
                til?.error = null
            }
            true
        } else {
            false
        }
    }


    private fun updateBoxLevel(newRepeatInDay: Int) {
        val updateBoxLevel = SpaceRepetitionBox(
            boxLevel.levelId,
            boxLevel.levelName,
            boxLevel.levelColor,
            newRepeatInDay,
            spaceRepetitionAlgorithmHelper.revisionMargin(newRepeatInDay)
        )
        listener?.getUpdatedBoxLevel(updateBoxLevel)
        dismiss()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as SettingsFragmentEditBoxLevelDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException((context.toString() +
                    " must implement SettingsFragmentEditBoxLevelDialogListener"))
        }
    }

    interface SettingsFragmentEditBoxLevelDialogListener {
        fun getUpdatedBoxLevel(boxLevel: SpaceRepetitionBox)
    }

}