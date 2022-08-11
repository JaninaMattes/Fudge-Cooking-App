package com.mobilesystems.feedme.ui.profile

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.mobilesystems.feedme.databinding.ProductDetailTagItemBinding
import com.mobilesystems.feedme.domain.model.FoodType

class UserFoodPrefListAdapter(private val dataSet: List<FoodType>?
) : RecyclerView.Adapter<UserFoodPrefListAdapter.UserTagListViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */

    //view binding
    private var _itemBinding: ProductDetailTagItemBinding? = null
    private val itemBinding get() = _itemBinding!!

    inner class UserTagListViewHolder(itemBinding: ProductDetailTagItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        val cardView: CardView = itemBinding.dietryTagCardView
        val tagNameTextView: TextView = itemBinding.textDietryTagLabel

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserTagListViewHolder {
        // Create a view which defines the UI of the list item
        _itemBinding = ProductDetailTagItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserTagListViewHolder(itemBinding)
    }

    override fun onBindViewHolder(userTagViewHolder: UserTagListViewHolder, position: Int) {
        if (dataSet != null) {
            // get selected product
            val currentItem = dataSet[position]
            Log.d("FoodTypeAdapter", "FoodType ${currentItem.label}")
            // pass values to view items
            userTagViewHolder.tagNameTextView.text = currentItem.label
        }
    }

    override fun getItemCount(): Int {
        return dataSet?.size ?: 0
    }
}