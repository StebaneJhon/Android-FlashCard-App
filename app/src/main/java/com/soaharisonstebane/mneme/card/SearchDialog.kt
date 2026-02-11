package com.soaharisonstebane.mneme.card

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialogFragment
import com.soaharisonstebane.mneme.R
import com.soaharisonstebane.mneme.backend.models.ExternalDeck
import com.soaharisonstebane.mneme.databinding.DialogSearchBinding
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import com.soaharisonstebane.mneme.backend.FlashCardApplication

class SearchDialog(
    private val deck: ExternalDeck
): AppCompatDialogFragment() {

    private var _binding: DialogSearchBinding? = null
    private  val binding get() = _binding!!
    private var appContext: Context? = null

    private val searchDialogViewModel by lazy {
        val repository = (requireActivity().application as FlashCardApplication).repository
        ViewModelProvider(this, SearchDialogViewModelFactory(repository)) [SearchDialogViewModel:: class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.QuizeoFullscreenDialogTheme)
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window!!.setLayout(width, height)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogSearchBinding.inflate(inflater, container, false)
        appContext = activity?.applicationContext
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btBack.setOnClickListener {
            dismiss()
        }
        binding.sv.apply {
            isIconified = false
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    if (!query.isNullOrEmpty()) {
                        performSearch(query)
                    }
                    return true
                }
                override fun onQueryTextChange(newText: String?): Boolean {
                    if (!newText.isNullOrEmpty()) {
                        performSearch(newText)
                    }
                    return true
                }
            })
        }

    }

    private fun performSearch(query: String) {
        // TODO: Implement search
        Toast.makeText(appContext, getString(R.string.view_in_development), Toast.LENGTH_SHORT).show()
    }

}