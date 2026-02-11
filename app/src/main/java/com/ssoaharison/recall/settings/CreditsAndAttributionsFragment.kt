package com.ssoaharison.recall.settings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.ssoaharison.recall.R
import com.ssoaharison.recall.databinding.FragmentCreditsAndAttributionsBinding


class CreditsAndAttributionsFragment : Fragment() {

    private var _binding: FragmentCreditsAndAttributionsBinding? = null
    private val binding get() = _binding!!

    companion object {
        const val TAG = "CreditsAndAttributionsFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreditsAndAttributionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_creditsAndAttributionsFragment_to_settingsFragment)
        }
    }

}