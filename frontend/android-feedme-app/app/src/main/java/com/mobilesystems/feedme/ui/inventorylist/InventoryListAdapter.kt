package com.mobilesystems.feedme.ui.inventorylist

import android.content.Context
import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.mobilesystems.feedme.R
import com.mobilesystems.feedme.databinding.InventoryItemBinding
import com.mobilesystems.feedme.ui.common.utils.getTimeDiff
import com.mobilesystems.feedme.domain.model.Product
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

/**
 * Tutorial: https://developer.android.com/guide/topics/ui/layout/recyclerview
 *          https://developersbreach.com/navigation-with-architecture-components-android/
 * @author Janina Mattes
 */
class InventoryListAdapter(
    private val context: Context,
    private val dataSet: List<Product>?,
    private val itemClickListener: ProductAdapterClickListener
) : RecyclerView.Adapter<InventoryListAdapter.ProductViewHolder>() {

    //view binding
    private var _itemBinding: InventoryItemBinding? = null
    private val itemBinding get() = _itemBinding!!

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    inner class ProductViewHolder(itemBinding: InventoryItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        // bind views
        val cardView: CardView = itemBinding.inventoryCardView
        val imageView: CircleImageView = itemBinding.productImage
        val productName: TextView = itemBinding.productName
        val productQuantity: TextView = itemBinding.productQuantity
        val productExpiration: TextView = itemBinding.productExpirationStatus
        val productExpirationDate: TextView = itemBinding.productExpirationDate

        init {
            // initialize clicklistener and pass clicked product for listitem position
            cardView.setOnClickListener{ v ->
                if (dataSet != null) {
                    itemClickListener.passData(dataSet[bindingAdapterPosition], v)
                }
            }
        }
    }

    interface ProductAdapterClickListener {
        fun passData(product: Product, itemView: View)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        // Create a view which defines the UI of the list item
        _itemBinding = InventoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(itemBinding)
    }

    override fun onBindViewHolder(viewHolder: ProductViewHolder, position: Int) {

        if (dataSet != null) {
            // get selected product
            val currentItem = dataSet[position]
            // pass values to view items
            val bitmap = currentItem.productImage?.bitmap
            if (bitmap != null){
                viewHolder.imageView.setImageBitmap(bitmap)
            }else {
                val imageUrl = currentItem.productImage?.imageUrl ?:
                    "https://cdn.pixabay.com/photo/2017/06/06/22/37/italian-cuisine-2378729_960_720.jpg"
                Picasso.get().load(imageUrl).fit().centerCrop().into(viewHolder.imageView)
            }
            viewHolder.productName.text = currentItem.productName
            viewHolder.productQuantity.text = currentItem.quantity

            val expDays = getTimeDiff(currentItem.expirationDate)
            if(expDays <= 3){
                viewHolder.productExpiration.setTextColor(ContextCompat.getColor(context, R.color.red_200))
                viewHolder.productExpirationDate.setTextColor(ContextCompat.getColor(context, R.color.red_200))
            } else {
                viewHolder.productExpiration.setTextColor(ContextCompat.getColor(context, R.color.black))
                viewHolder.productExpirationDate.setTextColor(ContextCompat.getColor(context, R.color.black))
            }
            val productExpText: String

            if(expDays > 1){
                productExpText = "$expDays days"
            } else if (expDays == 1L){
                productExpText = "$expDays day"
            }
            else{
                productExpText = "Today"
            }
            viewHolder.productExpiration.text = productExpText
            viewHolder.productExpirationDate.text = currentItem.expirationDate
        }
    }

    override fun getItemCount(): Int {
        return dataSet?.size ?: 0
    }
}