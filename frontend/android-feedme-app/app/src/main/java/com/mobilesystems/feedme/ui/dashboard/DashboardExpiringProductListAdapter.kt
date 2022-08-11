package com.mobilesystems.feedme.ui.dashboard

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mobilesystems.feedme.R
import com.mobilesystems.feedme.databinding.DashboardExpiringProductItemBinding
import com.mobilesystems.feedme.ui.common.utils.getTimeDiff
import com.mobilesystems.feedme.domain.model.Product

class DashboardExpiringProductListAdapter (
    private val context: Context,
    private val dataSet: List<Product>?,
    private val itemClickListener: ExpiringProductsAdapterClickListener
) : RecyclerView.Adapter<DashboardExpiringProductListAdapter.ProductViewHolder>() {

    private lateinit var imageUrl: String

    //view binding
    private var _itemBinding: DashboardExpiringProductItemBinding? = null
    private val itemBinding get() = _itemBinding!!

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    inner class ProductViewHolder(itemBinding: DashboardExpiringProductItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        val cardView: CardView = itemBinding.expiringProductItemCardView
        val productNameTextView: TextView = itemBinding.textExpiringProductName
        val productExpirationDate: TextView = itemBinding.textExpiringProductDate

        init {

            // initialize clicklistener and pass clicked product for listitem position
            cardView.setOnClickListener{ v ->
                if (dataSet != null) {
                    itemClickListener.passData(dataSet[bindingAdapterPosition], v)
                }
            }
        }
    }

    interface ExpiringProductsAdapterClickListener {
        fun passData(product: Product, itemView: View)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        // Create a view which defines the UI of the list item
        _itemBinding = DashboardExpiringProductItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(itemBinding)
    }

    override fun onBindViewHolder(productViewHolder: ProductViewHolder, position: Int) {
        if (dataSet != null) {
            // get selected product
            val currentItem = dataSet[position]
            // pass values to view items
            imageUrl = currentItem.productImage?.imageUrl ?:
                "https://cdn.pixabay.com/photo/2017/06/06/22/37/italian-cuisine-2378729_960_720.jpg"// dummy image
            // Calculate difference between current date to expiration date
            val expDays = getTimeDiff(currentItem.expirationDate)
            if(expDays <= 3){
                productViewHolder.productExpirationDate.setTextColor(ContextCompat.getColor(context, R.color.red_200))
            } else {
                productViewHolder.productExpirationDate.setTextColor(ContextCompat.getColor(context, R.color.black))
            }
            val expText: String = when {
                expDays <= 0 -> {
                    "Today"
                }
                expDays == 1L -> {
                    "$expDays day"
                }
                else -> {
                    "$expDays days"
                }
            }
            productViewHolder.productExpirationDate.text = expText
            productViewHolder.productNameTextView.text = currentItem.productName
        }
    }

    override fun getItemCount(): Int {
        return dataSet?.size ?: 0
    }
}