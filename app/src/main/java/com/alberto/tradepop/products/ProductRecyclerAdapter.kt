package com.alberto.tradepop.products

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.alberto.tradepop.R
import com.alberto.tradepop.databinding.ItemProductBinding
import com.alberto.tradepop.network.models.Product

class ProductRecyclerAdapter(private val onClickListener: (Product) -> Unit): RecyclerView.Adapter<ProductViewHolder>(){

    var productsList: List<Product> = emptyList()
        set(value){
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(productsList[position], onClickListener)
    }

    override fun getItemCount(): Int = productsList.size
}

data class ProductViewHolder(val binding: ItemProductBinding) : RecyclerView.ViewHolder(binding.root){

    fun bind(product: Product, onClickListener: (Product) -> Unit){
        with(binding){
            binding.mainLayout.setOnClickListener { onClickListener(product) }
            priceTextView.text = "${product.price.toString().replace(".", ",")}€"
            descriptionTextView.text = product.title
            coverImage.load(product.coverImageUrl){
                error(R.drawable.no_image_placeholder)
            }
        }
    }

}