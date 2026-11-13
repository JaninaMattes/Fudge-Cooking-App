package com.mobilesystems.feedme.ui.inventorylist

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mobilesystems.feedme.R
import com.mobilesystems.feedme.databinding.InventoryItemBinding
import com.mobilesystems.feedme.domain.model.Product
import com.mobilesystems.feedme.ui.common.utils.ExpiryStatus
import com.mobilesystems.feedme.ui.common.utils.expiryLabel
import com.mobilesystems.feedme.ui.common.utils.getTimeDiff
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

/**
 * Shows the products of the inventory list in a list.
 *
 * Each row gets the colour of its state, so the user sees in one moment which
 * products expire soon.
 *
 * @property context context that gives the colours
 * @property dataSet products to show. It can be `null` while the application
 *                   loads the data.
 * @property itemClickListener receives the product that the user selects
 *
 * @author Janina Mattes
 * @see <a href="https://developer.android.com/guide/topics/ui/layout/recyclerview">RecyclerView</a>
 @see <a href="https://developersbreach.com/navigation-with-architecture-components-android/">Architectural Android Components</a>
 */
class InventoryListAdapter(
    private val context: Context,
    private val dataSet: List<Product>?,
    private val itemClickListener: ProductAdapterClickListener
) : RecyclerView.Adapter<InventoryListAdapter.ProductViewHolder>() {

    /** Image that a product without an own image gets. */
    private companion object {
        const val PLACEHOLDER_IMAGE_URL =
            "https://cdn.pixabay.com/photo/2017/06/06/22/37/italian-cuisine-2378729_960_720.jpg"
    }

    private var itemBinding: InventoryItemBinding? = null

    /**
     * Holds the views of one row.
     *
     * @param itemBinding views of the row
     */
    inner class ProductViewHolder(itemBinding: InventoryItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        val cardView: CardView = itemBinding.inventoryCardView
        val imageView: CircleImageView = itemBinding.productImage
        val productName: TextView = itemBinding.productName
        val productQuantity: TextView = itemBinding.productQuantity
        val productExpiration: TextView = itemBinding.productExpirationStatus
        val productExpirationDate: TextView = itemBinding.productExpirationDate

        init {
            cardView.setOnClickListener { view ->
                dataSet?.let { products ->
                    itemClickListener.passData(products[bindingAdapterPosition], view)
                }
            }
        }
    }

    /** Receives the product that the user selects in the list. */
    interface ProductAdapterClickListener {

        /**
         * Gives the selected product to the fragment.
         *
         * @param product product that the user selected
         * @param itemView row that the user touched
         */
        fun passData(product: Product, itemView: View)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = InventoryItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        itemBinding = binding
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ProductViewHolder, position: Int) {
        val product = dataSet?.getOrNull(position) ?: return

        showImage(viewHolder, product)
        viewHolder.productName.text = product.productName
        viewHolder.productQuantity.text = product.quantity
        showExpiryState(viewHolder, product)
    }

    override fun getItemCount(): Int = dataSet?.size ?: 0

    /**
     * Shows the image of the product. A product without an image gets a
     * placeholder, so that each row has the same height.
     */
    private fun showImage(viewHolder: ProductViewHolder, product: Product) {
        val bitmap = product.productImage?.bitmap
        if (bitmap != null) {
            viewHolder.imageView.setImageBitmap(bitmap)
            return
        }
        val imageUrl = product.productImage?.imageUrl ?: PLACEHOLDER_IMAGE_URL
        Picasso.get().load(imageUrl).fit().centerCrop().into(viewHolder.imageView)
    }

    /**
     * Writes the state of the product and gives it the colour of the state.
     */
    private fun showExpiryState(viewHolder: ProductViewHolder, product: Product) {
        val daysLeft = getTimeDiff(product.expirationDate)
        val status = ExpiryStatus.fromDaysLeft(daysLeft)
        val color = ContextCompat.getColor(context, status.colorRes)

        viewHolder.productExpiration.setTextColor(color)
        viewHolder.productExpiration.text = context.getString(status.labelRes)

        // A product that is good for a long time keeps the normal text colour,
        // so that the coloured rows stay easy to find.
        val dateColor = if (status.needsAttention()) {
            color
        } else {
            ContextCompat.getColor(context, R.color.text_secondary)
        }
        viewHolder.productExpirationDate.setTextColor(dateColor)
        viewHolder.productExpirationDate.text = expiryLabel(context, daysLeft)
        viewHolder.cardView.contentDescription = context.getString(
            R.string.expiry_content_description, context.getString(status.labelRes)
        )
    }
}
