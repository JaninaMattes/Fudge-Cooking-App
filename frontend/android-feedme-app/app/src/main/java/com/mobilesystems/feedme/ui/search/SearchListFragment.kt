package com.mobilesystems.feedme.ui.search

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobilesystems.feedme.databinding.FragmentSearchListBinding
import com.mobilesystems.feedme.domain.model.Product
import dagger.hilt.android.AndroidEntryPoint
/**
 * A simple [Fragment] subclass.
 * Use the [SearchListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

@AndroidEntryPoint
class SearchListFragment : Fragment() {

    private val viewModel: SearchSearchViewModel by activityViewModels()

    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var searchRecyclerView: RecyclerView
    private lateinit var adapter: SearchResultAdapter

    //view binding
    private var _binding: FragmentSearchListBinding? = null
    private val binding get() = _binding!!

    // This property is only valid between onCreateView and onDestroyView
    private val listener = object: SearchResultAdapter.SearchResultAdapterClickListener {

        override fun passData(product: Product, itemView: View) {
            // pass data and navigate to inventory-fragment or recipe-fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSearchListBinding.inflate(inflater, container, false)

        // Setup recycler view
        searchRecyclerView = binding.recyclerviewSearchList
        linearLayoutManager = LinearLayoutManager(context)
        searchRecyclerView.layoutManager = linearLayoutManager

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Create the observer which updates the UI.
        val searchResultObserver = Observer<List<Any>?> { searchResult: List<Any>? ->
            // Update the UI and update the adapter for recycelerview
            if (searchResult != null) {
                val context = activity?.applicationContext
                if(context != null) {
                    adapter = SearchResultAdapter(context, searchResult, listener)
                    searchRecyclerView.adapter = adapter
                }
            }
        }

        // update adapter after data is loaded
        viewModel.searchResult.observe(viewLifecycleOwner, searchResultObserver)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "SearchListFragment"
        fun newInstance() = SearchListFragment()
    }
}