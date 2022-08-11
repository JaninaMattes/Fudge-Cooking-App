package com.mobilesystems.feedme.ui.inventorylist

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.mobilesystems.feedme.domain.model.Product
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.recyclerview.widget.ItemTouchHelper
import com.mobilesystems.feedme.databinding.InventoryListFragmentBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * A fragment representing a list of Items.
 */
@AndroidEntryPoint
class InventoryListFragment : Fragment() {

    // delegate to main activity so that ViewModel is preserved
    private val sharedViewModel: SharedInventoryViewModel by activityViewModels()

    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var inventoryRecyclerView: RecyclerView
    private lateinit var adapter: InventoryListAdapter

    //view binding
    private var _binding: InventoryListFragmentBinding? = null
    private val binding get() = _binding!!

    // This property is only valid between onCreateView and onDestroyView
    private val listener = object: InventoryListAdapter.ProductAdapterClickListener {

        override fun passData(product: Product, itemView: View) {
            // pass data and navigate to product detail view
            sharedViewModel.selectProduct(product)
            val action = InventoryListFragmentDirections.actionNavigationInventorylistToProductFragment(product)
            Navigation.findNavController(itemView).navigate(action)
        }
    }

    // Remove items of list via swipe
    private var simpleItemTouchCallback: ItemTouchHelper.SimpleCallback =
        object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
                val product = sharedViewModel.deleteProductByPosition(viewHolder.absoluteAdapterPosition)
                adapter.notifyItemRemoved(viewHolder.absoluteAdapterPosition)
                if(product != null) {
                    sharedViewModel.deleteProductInInventoryList(product)
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate layout for this fragment
        _binding = InventoryListFragmentBinding.inflate(inflater, container, false)
        val buttonAddProduct: FloatingActionButton = binding.buttonInventoryAddProduct

        buttonAddProduct.setOnClickListener{
            val action = InventoryListFragmentDirections.actionNavigationInventorylistToAddProductToInventoryFragment()
            findNavController().navigate(action)
        }

        // Setup recycler view
        inventoryRecyclerView = binding.recyclerviewInventoryList
        linearLayoutManager = LinearLayoutManager(context)
        inventoryRecyclerView.layoutManager = linearLayoutManager
        inventoryRecyclerView.setHasFixedSize(true)

        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(inventoryRecyclerView)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Create the observer which updates the UI.
        val inventoryListObserver = Observer<List<Product>?> { inventoryList: List<Product>? ->
            // Update the UI and update the adapter for recycelerview
            if (inventoryList != null) {
                val context = activity?.applicationContext
                if(context != null) {
                    adapter = InventoryListAdapter(context, inventoryList, listener)
                    inventoryRecyclerView.adapter = adapter
                }
            }
        }
        // update adapter after data is loaded
        sharedViewModel.inventoryList.observe(viewLifecycleOwner, inventoryListObserver)
    }

    override fun onDestroyView() {
         super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "InventoryListFragment"
        fun newInstance() = InventoryListFragment()
    }

}