package com.alberto.tradepop.transactions

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.alberto.tradepop.R
import com.alberto.tradepop.databinding.ItemProductBinding
import com.alberto.tradepop.databinding.ItemTransactionBinding
import com.alberto.tradepop.network.models.Product
import com.alberto.tradepop.network.models.Transaction
import com.alberto.tradepop.products.ProductViewHolder

class TransactionRecyclerAdapter : RecyclerView.Adapter<TransactionViewHolder>() {

    var transactionsList: List<Transaction> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(transactionsList[position])
    }

    override fun getItemCount(): Int = transactionsList.size

}

data class TransactionViewHolder(val binding: ItemTransactionBinding) : RecyclerView.ViewHolder(binding.root){

    fun bind(transaction: Transaction){
        with(binding){
            productTitle.text = transaction.productName
            transactionPrice.text = "${transaction.price.toString()}€"
            if(transaction.isPurchase){
                binding.indicatorView.setBackgroundColor(Color.RED)
            }
            else{
                binding.indicatorView.setBackgroundColor(Color.GREEN)
            }
        }
    }

}