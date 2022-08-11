package com.mobilesystems.feedme.ui.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobilesystems.feedme.databinding.DashboardExpiringProductsFragmentBinding
import com.mobilesystems.feedme.domain.model.Product
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashboardExpiringProductListFragment : Fragment() {

    // delegate to main activity so that ViewModel is preserved
    private val sharedViewModel: SharedDashboardViewModel by activityViewModels()

    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var expiringListRecyclerView: RecyclerView
    private lateinit var adapter: DashboardExpiringProductListAdapter

    //view binding
    private var _binding: DashboardExpiringProductsFragmentBinding? = null
    private val binding get() = _binding!!

    private val listener = object: DashboardExpiringProductListAdapter.ExpiringProductsAdapterClickListener {
        override fun passData(product: Product, itemView: View) {
            // pass data and navigate to product detail view
            Log.d(TAG, "Product ${product.productName} selected.")
            sharedViewModel.selectProduct(product)
            val action = DashboardFragmentDirections.actionNavigationDashboardToProductFragment(product)
            Navigation.findNavController(itemView).navigate(action)
        }
    }

    override fun onCreate(savedInstance: Bundle?){
        super.onCreate(savedInstance)
        sharedViewModel.loadExpiringProducts()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "Called onCreateView.")

        // Inflate layout for this fragment
        _binding = DashboardExpiringProductsFragmentBinding.inflate(inflater, container, false)

        // Setup recycler view
        expiringListRecyclerView = binding.recyclerviewExpiringProducts
        linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        expiringListRecyclerView.layoutManager = linearLayoutManager

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Create the observer which updates the UI.
        val productListObserver = Observer<List<Product>?> { productList: List<Product>? ->
            // Update the UI and update the adapter for recyclerview
            if (productList != null) {
                val context = activity?.applicationContext
                if(context != null) {
                    adapter = DashboardExpiringProductListAdapter(context, productList, listener)
                    expiringListRecyclerView.adapter = adapter
                }else{
                    Log.d(TAG, "App context is empty.")
                }
            }else{
                Log.d(TAG, "Productlist is empty..")
            }
        }

        // update adapter after data is loaded
        sharedViewModel.expProductList.observe(viewLifecycleOwner, productListObserver)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "DashboardExpiringProductListFragment"
        fun newInstance() = DashboardExpiringProductListFragment()
    }
}