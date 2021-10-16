package com.alberto.tradepop.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alberto.tradepop.R
import com.alberto.tradepop.network.dataManager.DataManager
import com.alberto.tradepop.network.models.Product
import com.alberto.tradepop.network.userDataManager.UserDataManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProductsViewModel(
    private val userDataManager: UserDataManager,
    private val dataManager: DataManager
) : ViewModel() {

    private val productsStateFlow: MutableStateFlow<ProductsState> = MutableStateFlow(
        ProductsState.reset()
    )
    val productsState: StateFlow<ProductsState>
        get() = productsStateFlow

    private var previousUserUuid: String = ""
    var textToSearch: String = ""
    var allProducts: List<Product> = emptyList()

    fun checkUserStatus() {
        val user = userDataManager.getCurrentUser()
        user?.let {
            if (previousUserUuid != it.uuid) {
                previousUserUuid = it.uuid
                getProducts()
            }
        } ?: run {
            previousUserUuid = ""
            getProducts()
        }
    }

    fun getProducts() {
        var userUuid: String? = null
        if (previousUserUuid != "") {
            userUuid = previousUserUuid
        }
        viewModelScope.launch {
            val products = dataManager.getAllProducts(userUuid)
            products?.let {
                this@ProductsViewModel.allProducts = it
                filterProducts(this@ProductsViewModel.textToSearch)
            } ?: run {
                productsStateFlow.value = ProductsState(
                    products = productsState.value.products,
                    showMessage = true,
                    messageData = ProductsState.MessageData(
                        R.string.generic_error,
                        R.string.new_product_upload_product_error_message
                    )
                )
            }
        }
    }

    fun messageDisplayed() {
        productsStateFlow.value = ProductsState(
            products = productsState.value.products,
            showMessage = false,
            messageData = null
        )
    }

    fun filterProducts(textToSearch: String){
        this.textToSearch = textToSearch
        val filteredProducts = this.allProducts.filter {
            it.title?.contains(textToSearch, true) ?: false || it.description?.contains(textToSearch, true) ?: false
        }
        productsStateFlow.value = ProductsState(
            products = filteredProducts,
            showMessage = false,
            messageData = null
        )
    }

    data class ProductsState(
        val products: List<Product>,
        val showMessage: Boolean,
        val messageData: MessageData?
    ) {
        data class MessageData(val messageTitle: Int, val messageDescription: Int)
        companion object {
            fun reset() = ProductsState(
                products = emptyList(),
                showMessage = false,
                messageData = null
            )
        }
    }

}