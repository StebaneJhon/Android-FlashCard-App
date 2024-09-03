package com.example.flashcard.profile

import android.content.Context
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flashcard.R
import com.example.flashcard.backend.FlashCardApplication
import com.example.flashcard.backend.Model.ImmutableWeeklyReviewModel
import com.example.flashcard.databinding.FragmentProfileBinding
import com.example.flashcard.util.UiState
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private var appContext: Context? = null
    private val profileViewModel by lazy {
        val repository = (requireActivity().application as FlashCardApplication).repository
        ViewModelProvider(this, ProfileViewModelFactory(repository))[ProfileViewModel::class.java]
    }

    private lateinit var profileDeckSectionRecyclerViewAdapter: ProfileFragmentDecksSectionRecyclerViewAdapter
    private lateinit var profileCardSectionRecyclerViewAdapter: ProfileFragmentCardsSectionRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        appContext = container?.context
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.profileTopAppBar.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_deckFragment)
        }

        lifecycleScope.launch {
            profileViewModel.getWeeklyReview()
            profileViewModel.weeklyReview
                .collect {
                    when (it) {
                        is UiState.Loading -> {

                        }
                        is UiState.Error -> {
                            Toast.makeText(appContext, it.errorMessage, Toast.LENGTH_LONG).show()
                        }
                        is UiState.Success -> {
                            bindWeeklyReview(it.data)
                        }
                    }
                }
        }

        showCardList()
        showDeckList()


    }

    private fun showCardList() {
        lifecycleScope.launch {
            profileViewModel.getAllCards()
            profileViewModel.allCards
                .collect {
                    when (it) {
                        is UiState.Loading -> {

                        }

                        is UiState.Error -> {
                            Toast.makeText(appContext, it.errorMessage, Toast.LENGTH_LONG).show()
                        }

                        is UiState.Success -> {
                            profileCardSectionRecyclerViewAdapter = ProfileFragmentCardsSectionRecyclerViewAdapter(
                                appContext!!,
                                it.data,
                                profileViewModel.getBoxLevels()!!)
                            binding.rvCard.apply {
                                layoutManager = LinearLayoutManager(
                                    appContext,
                                    LinearLayoutManager.HORIZONTAL,
                                    false
                                )
                                adapter = profileCardSectionRecyclerViewAdapter
                            }
                            binding.tvCardNumber.text = it.data.size.toString()
                            binding.tvKnownCardNumber.text = profileViewModel.getKnownCardsSum(it.data).toString()
                        }
                    }
                }
        }
    }

    private fun showDeckList() {
        lifecycleScope.launch {
            profileViewModel.getAllDecks()
            profileViewModel.allDecks
                .collect {
                    when (it) {
                        is UiState.Loading -> {

                        }

                        is UiState.Error -> {
                            Toast.makeText(appContext, it.errorMessage, Toast.LENGTH_LONG).show()
                        }

                        is UiState.Success -> {
                            profileDeckSectionRecyclerViewAdapter = ProfileFragmentDecksSectionRecyclerViewAdapter(it.data, appContext!!) {
                                //TODO: Open card list fragment
                            }
                            binding.rvDeck.apply {
                                layoutManager = LinearLayoutManager(
                                    appContext,
                                    LinearLayoutManager.HORIZONTAL,
                                    false
                                )
                                adapter = profileDeckSectionRecyclerViewAdapter
                            }
                            binding.tvDeckNumber.text = it.data.size.toString()
                        }
                    }
                }
        }
    }

    private fun bindWeeklyReview(data: ImmutableWeeklyReviewModel) {
        binding.mondayLY.background.setColorFilter(getCellBrightnessColor(data.monday?.colorGrade!!), PorterDuff.Mode.SRC_ATOP)
        binding.tuesdayLY.background.setColorFilter(getCellBrightnessColor(data.tuesday?.colorGrade!!), PorterDuff.Mode.SRC_ATOP)
        binding.wednesdayLY.background.setColorFilter(getCellBrightnessColor(data.wednesday?.colorGrade!!), PorterDuff.Mode.SRC_ATOP)
        binding.thursdayLY.background.setColorFilter(getCellBrightnessColor(data.thursday?.colorGrade!!), PorterDuff.Mode.SRC_ATOP)
        binding.fridayLY.background.setColorFilter(getCellBrightnessColor(data.friday?.colorGrade!!), PorterDuff.Mode.SRC_ATOP)
        binding.saturdayLY.background.setColorFilter(getCellBrightnessColor(data.saturday?.colorGrade!!), PorterDuff.Mode.SRC_ATOP)
        binding.sundayLY.background.setColorFilter(getCellBrightnessColor(data.sunday?.colorGrade!!), PorterDuff.Mode.SRC_ATOP)

    }

    private fun getCellBrightnessColor(brightness: Int): Int {
        return when (brightness)
        {
             700 -> {
                val typedValue = TypedValue()
                appContext?.theme?.resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true)
                typedValue.data
            }
            200 -> {
                val typedValue = TypedValue()
                appContext?.theme?.resolveAttribute(com.google.android.material.R.attr.colorSurfaceContainerHighest, typedValue, true)
                typedValue.data
            }
            100 -> {
                val typedValue = TypedValue()
                appContext?.theme?.resolveAttribute(com.google.android.material.R.attr.colorSurfaceContainerHigh, typedValue, true)
                typedValue.data
            }
            25 -> {
                val typedValue = TypedValue()
                appContext?.theme?.resolveAttribute(com.google.android.material.R.attr.colorSurfaceContainer, typedValue, true)
                typedValue.data
            }
            else -> {
                val typedValue = TypedValue()
                appContext?.theme?.resolveAttribute(com.google.android.material.R.attr.colorSurfaceContainerLow, typedValue, true)
                typedValue.data
            }
        }
    }
}