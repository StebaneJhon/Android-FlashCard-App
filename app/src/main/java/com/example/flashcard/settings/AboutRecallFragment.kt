package com.example.flashcard.settings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.navigation.fragment.findNavController
import com.example.flashcard.R
import com.example.flashcard.databinding.FragmentAboutRecallBinding

class AboutRecallFragment : Fragment() {

    private var _binding: FragmentAboutRecallBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val LINK_TO_RECALL_ABOUT = "https://sites.google.com/view/recall-mobileapp/accueil"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutRecallBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.AboutTopAppBar.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_aboutRecallFragment_to_settingsFragment)
        }
        weViewSetup()
    }

    private fun weViewSetup() {
        binding.wvAbout.apply {
            webViewClient = WebViewClient()
            loadUrl("https://sites.google.com/view/recall-mobileapp/accueil")
            settings.safeBrowsingEnabled = true
        }
    }

}