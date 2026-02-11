package com.soaharisonstebane.mneme.help

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.soaharisonstebane.mneme.R
import com.soaharisonstebane.mneme.databinding.FragmentHelpBinding
import com.soaharisonstebane.mneme.util.FAQDataModel

class HelpFragment : Fragment() {

    private var _binding: FragmentHelpBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HelpFragmentViewModel by viewModels()
    private lateinit var recyclerViewAdapter: HelpRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHelpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.helpTopAppBar.setNavigationOnClickListener {
            activity?.findViewById<DrawerLayout>(R.id.mainActivityRoot)?.open()
        }

        showFAQData(viewModel.getFaqData(requireContext()))

    }

    private fun showFAQData(faqDataList: List<FAQDataModel>) {
        recyclerViewAdapter = HelpRecyclerViewAdapter(faqDataList)
        binding.rvFaq.apply {
            adapter = recyclerViewAdapter
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

}