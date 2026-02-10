package com.ssoaharison.recall.onboarding

import android.animation.LayoutTransition
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.ScrollingMovementMethod
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.View.FOCUS_DOWN
import android.view.ViewGroup
import androidx.core.view.ScrollingView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.color.MaterialColors
import com.ssoaharison.recall.R
import com.ssoaharison.recall.databinding.FragmentOnBoardingViewPagerBinding
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.ssoaharison.recall.backend.FlashCardApplication
import com.ssoaharison.recall.util.MainDeck
import kotlinx.coroutines.launch


class OnBoardingViewPagerFragment : Fragment() {

    private var _binding: FragmentOnBoardingViewPagerBinding? = null
    val binding get() = _binding!!

    private var lastShownTextPosition = 0
    private lateinit var introductionTexts: List<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnBoardingViewPagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvAppIntroduction.movementMethod = ScrollingMovementMethod()
        introductionTexts = listOf(
            getString(R.string.content_welcome_to_recall),
            getString(R.string.content_cards),
            getString(R.string.content_quiz),
            getString(R.string.content_space_repetition),
        )

        binding.tvProgressIndicator.text = getString(R.string.text_on_boarding_screen_progression, "${lastShownTextPosition.plus(1)}")

        binding.btNext.setOnClickListener {
            if (lastShownTextPosition < 3) {
                progressOnBoarding()
                if (lastShownTextPosition == 3) {
                    binding.btNext.text = getString(R.string.bt_text_start)
                }
            } else {
                onBoardingFinished()
            }
        }

        binding.topAppBar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.bt_menu_skip) {
                onBoardingFinished()
                true
            } else {
                false
            }
        }

    }


    private fun progressOnBoarding() {
        lastShownTextPosition++
        val lastStringLength = binding.tvAppIntroduction.text.length
        binding.tvAppIntroduction.append(" ${introductionTexts[lastShownTextPosition]}")

        val text = binding.tvAppIntroduction.text

        val spannableText = SpannableString(text)
        val previousTextColor = ForegroundColorSpan(
            MaterialColors.getColor(
                requireContext(),
                com.google.android.material.R.attr.colorSurfaceContainerHighest,
                Color.GRAY
            )
        )
        val newTextColor = ForegroundColorSpan(
            MaterialColors.getColor(
                requireContext(),
                com.google.android.material.R.attr.colorOnSurface,
                Color.BLACK
            )
        )
        binding.tvProgressIndicator.text = getString(
            R.string.text_on_boarding_screen_progression,
            "${lastShownTextPosition.plus(1)}"
        )
        spannableText.setSpan(
            previousTextColor,
            0,
            lastStringLength,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableText.setSpan(
            newTextColor,
            lastStringLength,
            text.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.tvAppIntroduction.text = spannableText

        applyLayoutTransition()
//        binding.svAppIntroduction.fullScroll(View.FOCUS_DOWN)
    }

    private fun onBoardingFinished() {
        val sharedPreferences = requireActivity().getSharedPreferences("onBoarding", Context.MODE_PRIVATE)
        sharedPreferences.edit {
            putBoolean("Finished", true)
        }
//        findNavController().navigate(R.id.action_onBoardingViewPagerFragment_to_deckFragment2)
        lifecycleScope.launch {
            val repository = FlashCardApplication().repository
            val mainDeck = repository.getMainDeck()
            if (mainDeck == null) {
                repository.insertDeck(MainDeck().getMainDeck())
            }
        }
        findNavController().navigate(R.id.action_onBoardingViewPagerFragment_to_cardFragment)
    }

    private fun applyLayoutTransition() {
        val transition = LayoutTransition()
        transition.setDuration(300)
        transition.enableTransitionType(LayoutTransition.CHANGING)
        binding.fmAppIntroduction.setLayoutTransition(transition)
    }

}