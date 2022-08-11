package com.mobilesystems.feedme.ui.shoppinglist

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mobilesystems.feedme.R
import com.mobilesystems.feedme.databinding.ShoppingListFragmentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShoppingListFragment : Fragment() {

    private val sharedViewModel: SharedShoppingListViewModel by activityViewModels()

    //view binding
    private var _binding: ShoppingListFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedViewModel.loadAllCurrentShoppingListProducts()
        sharedViewModel.loadAllOldShoppingListProducts()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate layout for this fragment
        _binding = ShoppingListFragmentBinding.inflate(inflater, container, false)
        val createNewProductButton: FloatingActionButton = binding.buttonCreateNewProductForShoppingList

        createNewProductButton.setOnClickListener {
            Log.d(TAG, "Add a new product to the shopping list.")
            val action = ShoppingListFragmentDirections.actionNavigationShoppingListToAddProductToShoppingListFragment()
            findNavController().navigate(action)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Add nested child fragment
        val currentShoppingListFragment = ShoppingListCurrentProductsGridFragment()
        val oldShoppingListFragment = ShoppingListOldProductsGridFragment()

        addChildFragment(R.id.current_shopping_list_fragment, currentShoppingListFragment)
        addChildFragment(R.id.old_shopping_list_fragment, oldShoppingListFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Persist current state
        sharedViewModel.saveCurrentState()
    }

    private fun addChildFragment(viewId: Int, childFragment: Fragment){
        // nest child fragment into parent fragment
        // https://developer.android.com/about/versions/android-4.2#NestedFragments
        Log.d(TAG, "Add child fragment to parent.")

        val child = childFragmentManager.findFragmentById(viewId)

        if(child == null) {
            childFragmentManager.beginTransaction().apply {
                add(viewId, childFragment)
                addToBackStack(null)
                commit()
            }
        }
    }

    companion object {
        const val TAG = "ShoppingListFragment"
        fun newInstance() = ShoppingListFragment()
    }

}

