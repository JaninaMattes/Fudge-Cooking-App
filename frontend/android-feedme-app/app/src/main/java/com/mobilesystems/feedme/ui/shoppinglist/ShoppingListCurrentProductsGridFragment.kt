package com.mobilesystems.feedme.ui.shoppinglist

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.GridView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.mobilesystems.feedme.domain.model.Product
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.mobilesystems.feedme.databinding.ShoppingListCurrentFragmentBinding
import com.mobilesystems.feedme.ui.common.listener.GestureListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShoppingListCurrentProductsGridFragment: Fragment() {

    private var detector: GestureDetectorCompat? = null

    // delegate to main activity so that ViewModel is preserved
    private val sharedViewModel: SharedShoppingListViewModel by activityViewModels()
    private lateinit var productListGridView: GridView
    private lateinit var adapter: ShoppingListCurrentProductsGridAdapter

    //view binding
    private var _binding: ShoppingListCurrentFragmentBinding? = null
    private val binding get() = _binding!!

    private val listener = object: ShoppingListCurrentProductsGridAdapter.ProductAdapterClickListener {

        override fun passData(product: Product, itemView: View) {
            // pass data and navigate to product detail
            sharedViewModel.addProductToOldShoppingList(product)
        }
    }

    private val itemLongClickListener = object: ShoppingListCurrentProductsGridAdapter.ProductAdapterLongClickListener {
        override fun passData(product: Product, itemView: View): Boolean{
            sharedViewModel.removeProductFromCurrentShoppingList(product)
            sharedViewModel.refresh()
            Toast.makeText(context,"Product ${product.productName} deleted!", Toast.LENGTH_SHORT).show()
            return true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //sharedViewModel.loadAllCurrentShoppingListProducts()
        val context = activity?.applicationContext
        detector = GestureDetectorCompat(context, GestureListener())
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate layout for this fragment
        _binding = ShoppingListCurrentFragmentBinding.inflate(inflater, container, false)
        // Setup grid view
        productListGridView = binding.shoppingListCurrentGridView

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Create the observer which updates the UI.
        val productListObserver = Observer<List<Product>?> { productList: List<Product>? ->
            // Update the UI and update the adapter for recycelerview
            if (productList != null) {
                val context = activity?.applicationContext
                adapter = ShoppingListCurrentProductsGridAdapter(context, productList, listener, itemLongClickListener)
                productListGridView.adapter = adapter

                // Configure the grid view
                productListGridView.numColumns = 3
                productListGridView.horizontalSpacing = 15
                productListGridView.verticalSpacing = 15
                productListGridView.stretchMode = GridView.STRETCH_COLUMN_WIDTH
            }
        }
        // update adapter after data is loaded
        sharedViewModel.currentShoppingList.observe(viewLifecycleOwner, productListObserver)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "ShoppingListGrid"
        fun newInstance() = ShoppingListCurrentProductsGridFragment()
    }
}