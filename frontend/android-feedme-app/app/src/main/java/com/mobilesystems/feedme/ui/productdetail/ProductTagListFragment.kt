package com.mobilesystems.feedme.ui.productdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobilesystems.feedme.databinding.ProductDetailTagListFragmentBinding
import com.mobilesystems.feedme.domain.model.Label
import com.mobilesystems.feedme.ui.inventorylist.SharedInventoryViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProductTagListFragment: Fragment()  {

    // delegate to main activity so that ViewModel is preserved
    private val sharedViewModel: SharedInventoryViewModel by activityViewModels()
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var tagRecyclerView: RecyclerView
    private lateinit var adapter: ProductTagListAdapter

    //view binding
    private var _binding: ProductDetailTagListFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate layout for this fragment
        _binding = ProductDetailTagListFragmentBinding.inflate(inflater, container, false)

        // Setup recycler view
        tagRecyclerView = binding.recyclerviewProductTagList
        linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        tagRecyclerView.layoutManager = linearLayoutManager

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Create the observer which updates the UI.
        val tagListObserver = Observer<List<Label>?> { tagList: List<Label>? ->
            // Update the UI and update the adapter for recycelerview
            if (tagList != null) {
                adapter = ProductTagListAdapter(tagList)
                tagRecyclerView.adapter = adapter
            }
        }

        // update adapter after data is loaded
        sharedViewModel.selectedProductTagList.observe(viewLifecycleOwner, tagListObserver)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "ProductTagListFragment"
        fun newInstance() = ProductTagListFragment()
    }
}