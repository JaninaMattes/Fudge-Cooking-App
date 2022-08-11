package com.mobilesystems.feedme.ui.productdetail

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mobilesystems.feedme.databinding.ProductDetailTagItemBinding
import com.mobilesystems.feedme.domain.model.Label

class ProductTagListAdapter(
    private val dataSet: List<Label>?
) : RecyclerView.Adapter<ProductTagListAdapter.ProductTagViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */

    //view binding
    private var _itemBinding: ProductDetailTagItemBinding? = null
    private val itemBinding get() = _itemBinding!!

    inner class ProductTagViewHolder(itemBinding: ProductDetailTagItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        val productTagLabelTextView: TextView = itemBinding.textDietryTagLabel

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductTagViewHolder {
        // Create a view which defines the UI of the list item
        _itemBinding = ProductDetailTagItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductTagViewHolder(itemBinding)
    }

    override fun onBindViewHolder(tagViewHolder: ProductTagViewHolder, position: Int) {
        if (dataSet != null) {
            // get selected product
            val currentItem = dataSet[position]
            // pass values to view items
            tagViewHolder.productTagLabelTextView.text = currentItem.label
        }
    }

    override fun getItemCount(): Int {
        return dataSet?.size ?: 0
    }

}