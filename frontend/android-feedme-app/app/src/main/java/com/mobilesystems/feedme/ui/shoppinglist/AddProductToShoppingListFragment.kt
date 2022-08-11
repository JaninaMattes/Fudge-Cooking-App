package com.mobilesystems.feedme.ui.shoppinglist

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.mobilesystems.feedme.R
import com.mobilesystems.feedme.databinding.ShoppingListAddProductFragmentBinding
import com.mobilesystems.feedme.domain.model.Label
import com.mobilesystems.feedme.domain.model.Product
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.shopping_list_add_product_fragment.*

@AndroidEntryPoint
class AddProductToShoppingListFragment : Fragment(R.layout.shopping_list_add_product_fragment) {

    // by delegates to main activity to preceive shared viewmodel
    private val sharedViewModel: SharedShoppingListViewModel by activityViewModels()

    private var _binding: ShoppingListAddProductFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout for this fragment
        _binding = ShoppingListAddProductFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_enteraddproducttoshoppinglist.setOnClickListener {
            // check text edit values
            if(checkValues()){
                //add new product to sharedviewmodel
                val productname = binding.editTextAddnewproducttitle.text.toString()
                var productquantity = binding.editTextAddnewproductquantity.text.toString()
                if (productquantity.isDigitsOnly()){
                    val quantity = productquantity.filter { it.isDigit() }
                    productquantity = "$quantity Package"
                }

                val labels : MutableList<Label> = arrayListOf() // empty placeholder list add correct labels later in inventory list
                sharedViewModel.addNewProductToCurrentShoppingList(Product(0, productname,
                    "2022-02-02", labels, productquantity, "", "", null))
                Log.d(TAG, "Add product to current shopping list.")
                //navigate to shoppinglist
                val action = AddProductToShoppingListFragmentDirections.actionAddProductToShoppingListFragmentToNavigationShoppingList()
                findNavController().navigate(action)
            }
        }
    }

    private fun checkValues(): Boolean {
        val context = activity?.applicationContext
        var errorIcon: Drawable? = null
        if(context != null){
            errorIcon = ContextCompat.getDrawable(context, R.drawable.ic_baseline_error_24)
        }
        return when {
            binding.editTextAddnewproducttitle.text.trim().isEmpty() -> {
                binding.editTextAddnewproducttitle.setError(getString(R.string.edit_text_error_productname_not_empty), errorIcon)
                false
            }
            binding.editTextAddnewproductquantity.text.trim().isEmpty() -> {
                binding.editTextAddnewproducttitle.setError(getString(R.string.edit_text_error_product_quantity_not_empty), errorIcon)
                false
            }
            else -> {
                true
            }
        }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "AddProductToShoppingListFragment"
        fun newInstance() = AddProductToShoppingListFragment()
    }
}