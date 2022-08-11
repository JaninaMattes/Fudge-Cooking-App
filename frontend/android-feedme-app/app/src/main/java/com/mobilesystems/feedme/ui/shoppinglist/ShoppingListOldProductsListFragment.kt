package com.mobilesystems.feedme.ui.shoppinglist

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobilesystems.feedme.databinding.ShoppingListOldFragmentBinding
import com.mobilesystems.feedme.domain.model.Product
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShoppingListOldProductsGridFragment : Fragment() {

    // delegate to main activity so that ViewModel is preserved
    private val sharedViewModel: SharedShoppingListViewModel by activityViewModels()

    private lateinit var productListRecyclerView: RecyclerView
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var adapter: ShoppingListOldProductsListAdapter

    //view binding
    private var _binding: ShoppingListOldFragmentBinding? = null
    private val binding get() = _binding!!

    private val listener = object: ShoppingListOldProductsListAdapter.ProductAdapterClickListener {

        override fun passData(product: Product, itemView: View) {
            // pass data and navigate to product detail view
            sharedViewModel.addProductToCurrentShoppingList(product)
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
                val product = sharedViewModel.deleteOldProductByPosition(viewHolder.absoluteAdapterPosition)
                adapter.notifyItemRemoved(viewHolder.absoluteAdapterPosition)
                if(product != null) {
                    Log.d("Ausgewähltes Produkt", product.toString())
                    sharedViewModel.removeProductFromOldShoppingList(product)
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate layout for this fragment
        _binding = ShoppingListOldFragmentBinding.inflate(inflater, container, false)
        // Setup recycler view
        productListRecyclerView = binding.recyclerviewOldShoppingList
        linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false )
        productListRecyclerView.layoutManager = linearLayoutManager

        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(productListRecyclerView)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Create the observer which updates the UI.
        val productListObserver = Observer<List<Product>?> { productList: List<Product>? ->
            // Update the UI and update the adapter for recycelerview
            if (productList != null) {
                adapter = ShoppingListOldProductsListAdapter(context, productList, listener)
                productListRecyclerView.adapter = adapter
            }
        }
        // update adapter after data is loaded
        sharedViewModel.oldShoppingList.observe(viewLifecycleOwner, productListObserver)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "ShoppingListOldProductsGridFragment"
        fun newInstance() = ShoppingListOldProductsGridFragment()
    }
}