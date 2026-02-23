package com.soaharisonstebane.mneme.appView

import android.app.Dialog
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Html.FROM_HTML_MODE_LEGACY
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.soaharisonstebane.mneme.R
import com.soaharisonstebane.mneme.backend.models.ExternalCardContent
import com.soaharisonstebane.mneme.backend.models.ExternalCardDefinition
import com.soaharisonstebane.mneme.databinding.LyAudioPlayerBinding
import com.soaharisonstebane.mneme.databinding.LyCardFaceViewBinding
import com.soaharisonstebane.mneme.helper.AppMath
import com.soaharisonstebane.mneme.helper.AudioModel
import com.soaharisonstebane.mneme.helper.PhotoModel
import com.soaharisonstebane.mneme.helper.playback.AndroidAudioPlayer
import com.soaharisonstebane.mneme.home.AudioRecorderDialog
import com.soaharisonstebane.mneme.quiz.flashCardGame.FlashcardContentLyModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class CardFaceDialog: AppCompatDialogFragment() {

    private var _binding: LyCardFaceViewBinding? = null
    private val binding get() = _binding!!

    private val definitionList: List<ExternalCardDefinition>? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireArguments().getParcelableArrayList(ARG_DEFINITION_LIST, ExternalCardDefinition::class.java)
        } else {
            requireArguments().getParcelableArrayList<ExternalCardDefinition>(ARG_DEFINITION_LIST)
        }
    }

    private val content: ExternalCardContent? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(ARG_CONTENT, ExternalCardContent::class.java)
        } else {
            arguments?.getParcelable<ExternalCardContent>(ARG_CONTENT)
        }

    }

    private val player by lazy {
        AndroidAudioPlayer(requireContext())
    }

    var lastPlayedAudioFile: AudioModel? = null
    var lastPlayedAudioViw: LyAudioPlayerBinding? = null


    companion object {
        const val TAG = "CardFaceDialog"
        const val ARG_DEFINITION_LIST = "definition_list"
        const val ARG_CONTENT = "content"

        fun newInstance(definitionList: List<ExternalCardDefinition>?, content: ExternalCardContent?) =
            CardFaceDialog().apply {
                arguments = bundleOf(
                    ARG_DEFINITION_LIST to definitionList,
                    ARG_CONTENT to content
                )
            }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = LyCardFaceViewBinding.inflate(LayoutInflater.from(context))
        val builder = MaterialAlertDialogBuilder(
            requireActivity(),
            R.style.ThemeOverlay_App_MaterialAlertDialog
        )

        val itemViews = listOf(
            FlashcardContentLyModel(container = binding.container1, view = binding.in1),
            FlashcardContentLyModel(container = binding.container2, view = binding.in2),
            FlashcardContentLyModel(container = binding.container3, view = binding.in3),
            FlashcardContentLyModel(container = binding.container4, view = binding.in4),
            FlashcardContentLyModel(container = binding.container5, view = binding.in5),
            FlashcardContentLyModel(container = binding.container6, view = binding.in6),
            FlashcardContentLyModel(container = binding.container7, view = binding.in7),
            FlashcardContentLyModel(container = binding.container8, view = binding.in8),
            FlashcardContentLyModel(container = binding.container9, view = binding.in9),
            FlashcardContentLyModel(container = binding.container10, view = binding.in10),
        )

        if (content != null) {
            for (i in 0..itemViews.size.minus(1)) {
                if (i == 0) {
                    val view = itemViews[i].container
                    view.visibility = View.VISIBLE
                    bindItem(content?.contentText, content?.contentImage, content?.contentAudio, itemViews[i])
                } else {
                    itemViews[i].container.visibility = View.GONE
                }
            }
        }

        if (definitionList != null) {
            itemViews.forEachIndexed { index, itemView ->
                if (index < definitionList!!.size) {
                    itemView.container.visibility = View.VISIBLE
                    val definition = definitionList!![index]
                    bindItem(definition.definitionText, definition.definitionImage, definition.definitionAudio, itemView)
                } else {
                    itemView.container.visibility = View.VISIBLE
                }
            }
        }

        binding.btExit.setOnClickListener {
            dismiss()
        }

        builder.setView(binding.root)
        return builder.create()
    }

    private fun bindItem(
        text: String?,
        image: PhotoModel?,
        audio: AudioModel?,
        itemViews: FlashcardContentLyModel,
    ) {
        if (text != null) {
            val spannableString = Html.fromHtml(text, FROM_HTML_MODE_LEGACY).trim()
            itemViews.view.tvText.text = spannableString
            itemViews.view.tvText.visibility = View.VISIBLE
        } else {
            itemViews.view.tvText.visibility = View.GONE
        }
        if (image != null) {
            val photoFile = File(requireContext().filesDir, image.name)
            val photoBytes = photoFile.readBytes()
            val photoBtm = BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.size)
            itemViews.view.imgPhoto.apply {
                visibility = View.VISIBLE
                setImageBitmap(photoBtm)
            }
        } else {
            itemViews.view.imgPhoto.visibility = View.GONE
        }
        if (audio != null) {
            itemViews.view.llAudioContainer.visibility = View.VISIBLE
            itemViews.view.inAudioPlayer.btPlay.setOnClickListener {
                playPauseAudio(itemViews.view.inAudioPlayer, audio)
            }
        } else {
            itemViews.view.llAudioContainer.visibility = View.GONE
        }
    }

    private fun playPauseAudio(
        view: LyAudioPlayerBinding,
        audio: AudioModel
    ) {
        when {
            player.hasPlayed() && !player.isPlaying() -> {
                // Resume audio
                view.btPlay.setIconResource(R.drawable.icon_pause)
                player.play()
                lifecycleScope.launch {
                    while (player.isPlaying()) {
                        val progress = AppMath().normalize(
                            player.getCurrentPosition(),
                            player.getDuration()
                        )
                        view.lpiAudioProgression.progress = progress
                        delay(100L)
                    }
                }
            }

            player.hasPlayed() && player.isPlaying() -> {
                // Pause audio
                view.btPlay.setIconResource(R.drawable.icon_play)
                player.pause()
            }

            !player.hasPlayed() && !player.isPlaying() -> {
                // Play audio
                view.btPlay.setIconResource(R.drawable.icon_pause)
                val audioFile = File(requireContext().filesDir, audio.name)
                player.playFile(audioFile)
                lifecycleScope.launch {
                    while (player.isPlaying()) {
                        val progress = AppMath().normalize(
                            player.getCurrentPosition(),
                            player.getDuration()
                        )
                        view.lpiAudioProgression.progress = progress
                        delay(100L)
                    }
                }
                player.onCompletion {
                    lastPlayedAudioFile = null
                    lastPlayedAudioViw = null
                    view.btPlay.setIconResource(R.drawable.icon_play)
                }
            }
        }
    }


}