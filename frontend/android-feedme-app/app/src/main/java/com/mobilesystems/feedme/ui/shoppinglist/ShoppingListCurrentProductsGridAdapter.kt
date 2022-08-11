package com.mobilesystems.feedme.ui.shoppinglist

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.mobilesystems.feedme.R
import com.mobilesystems.feedme.domain.model.Product
import com.mobilesystems.feedme.databinding.ShoppingListCurrentItemBinding

class ShoppingListCurrentProductsGridAdapter(
    private val context: Context?,
    private val dataSet: List<Product>?,
    private val itemClickListener: ProductAdapterClickListener,
    private val itemLongClickListener: ProductAdapterLongClickListener
) : BaseAdapter() {

    //view binding
    private var _itemBinding: ShoppingListCurrentItemBinding? = null
    private val itemBinding get() = _itemBinding!!

    interface ProductAdapterClickListener {
        fun passData(product: Product, itemView: View)
    }

    interface ProductAdapterLongClickListener {
        fun passData(product: Product, itemView: View):Boolean?
    }

    override fun getView(position:Int, convertView: View?, parent: ViewGroup?): View{
        // Inflate the custom view
        val inflater = parent?.context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        _itemBinding = ShoppingListCurrentItemBinding.inflate(inflater, parent,false)

        val cardView: CardView = itemBinding.cardViewShoppingListItem
        val shoppingListColour: FrameLayout = itemBinding.shoppinglistItemColour
        val shoppingListIcon: ImageView = itemBinding.shoppingItemFoodIcon
        val shoppingListItemName: TextView = itemBinding.shoppingListItemName
        val shoppingListItemQuantity: TextView = itemBinding.shoppingListItemQuantity

        if(dataSet != null){
            val currentItem = dataSet[position]
            // place colour and correct icon programmatically
            if(context != null) {
                shoppingListColour.setBackgroundColor(ContextCompat.getColor(context,
                    R.color.theme_creme_50
                ))
                shoppingListIcon.setImageDrawable(ContextCompat.getDrawable(context, R.mipmap.ic_food_dummy))
            }
            shoppingListItemName.text = currentItem.productName
            shoppingListItemQuantity.text = currentItem.quantity
        }

        // initialize clicklistener, pass clicked product for listitem position
        cardView.setOnClickListener { v ->
            if (dataSet != null) {
                if(context != null) {
                    shoppingListColour.setBackgroundColor(
                        ContextCompat.getColor(context, R.color.red_200))
                }
                itemClickListener.passData(dataSet[position], v)
            }
        }

        // use longclick for delete
        cardView.setOnLongClickListener { v ->
            if (dataSet != null) {
                itemLongClickListener.passData(dataSet[position], v)
            }
            true
        }
        return itemBinding.root
    }

    override fun getItem(position: Int): Any {
        // TODO: Correct this workaround with dummy data
        var currentItem = Product(0, "", "", mutableListOf(),
            "", "", "", null)
        if (dataSet != null) {
            // get selected product
            currentItem = dataSet[position]
        }
        return currentItem
    }

    override fun getItemId(position: Int): Long {
        var itemId: Long = 0
        if (dataSet != null) {
            // get selected product
            itemId = dataSet[position].productId.toLong()
        }
        return itemId
    }

    override fun getCount(): Int {
        return dataSet?.size ?: 0
    }
}