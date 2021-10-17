package com.alberto.tradepop.productDetails

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alberto.tradepop.R
import com.alberto.tradepop.network.dataManager.DataManager
import com.alberto.tradepop.network.models.Product
import com.alberto.tradepop.network.models.User
import com.alberto.tradepop.network.userDataManager.UserDataManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

class ProductDetailsViewModel(
    private val userDataManager: UserDataManager,
    private val dataManager: DataManager
) : ViewModel() {

    private val userStateFlow: MutableStateFlow<ProductDetailsUserState> = MutableStateFlow(
        ProductDetailsUserState.empty()
    )
    val userState: StateFlow<ProductDetailsUserState>
        get() = userStateFlow

    private val productDetailsStateFlow: MutableStateFlow<ProductDetailsState> = MutableStateFlow(
        ProductDetailsState.empty()
    )
    val productDetailsState: StateFlow<ProductDetailsState>
        get() = productDetailsStateFlow

    private lateinit var product: Product
    var coverImageFile: File? = null
    var selectedCategoryId: Int? = null
    private var user: User? = null

    fun checkUserState() {
        this.user = userDataManager.getCurrentUser()
        userStateFlow.value = ProductDetailsUserState(user = this.user)
    }

    fun isProductFavorite(uuid: String): Boolean{
        return dataManager.getUserFavorites().contains(uuid)
    }

    fun changeFavorite(){
        user?.let {
            if(!isProductFavorite(product.uuid)){
                viewModelScope.launch {
                    val success = dataManager.addToFavorites(product.uuid, it.uuid)
                    if(!success) showErrorFav(false) else addToLocalFavorites()
                }
            }
            else{
                viewModelScope.launch {
                    val success = dataManager.removeFromFavorites(product.uuid, it.uuid)
                    if(!success) showErrorFav(true) else removeFromLocalFavorites()
                }
            }
        }

    }

    fun buyProduct() {
        user?.let {
            viewModelScope.launch {
                val success = dataManager.buyProduct(
                    product.uuid,
                    it.uuid,
                    it.username ?: "",
                    product.owner ?: "",
                    product.ownerName ?: "",
                    product.price ?: 0.0,
                    product.title ?: ""
                )
                if (success) closeActivity() else showErrorMessage()
            }
        } ?: run {
            showErrorMessage()
        }
    }

    fun deleteProduct() {
        user?.let {
            viewModelScope.launch {
                val success = dataManager.deleteProduct(product.uuid)
                if (success) closeActivity() else showErrorMessage()
            }
        } ?: run {
            showErrorMessage()
        }
    }

    fun checkFields(title: String, description: String, price: String) {
        if (title.isEmpty() || description.isEmpty() || price.isEmpty() || selectedCategoryId == null) {
            productDetailsStateFlow.value = ProductDetailsState(
                showMessage = true,
                messageData = ProductDetailsState.MessageData(
                    R.string.generic_error,
                    R.string.product_details_empty_fields_error_message
                ),
                finish = false,
                errorFavorite = false,
                isFavorite = false
            )
            return
        }
        editProduct(title, description, price.toDouble())
    }

    private fun editProduct(title: String, description: String, price: Double) {
        user?.let {
            viewModelScope.launch {
                var imageUrl = product.coverImageUrl ?: ""
                coverImageFile?.let {
                    val uploadUrl = dataManager.uploadImage(it)
                    uploadUrl?.let {
                        imageUrl = uploadUrl
                    }
                }
                val success = dataManager.updateProduct(
                    product.uuid,
                    title,
                    description,
                    selectedCategoryId ?: 0,
                    price,
                    imageUrl
                )
                if (success) closeActivity() else showErrorMessage()
            }
        } ?: run {
            showErrorMessage()
        }
    }

    private fun closeActivity() {
        productDetailsStateFlow.value = ProductDetailsState(
            showMessage = false,
            messageData = null,
            finish = true,
            errorFavorite = false,
            isFavorite = false
        )
    }

    private fun showErrorMessage() {
        productDetailsStateFlow.value = ProductDetailsState(
            showMessage = true,
            messageData = ProductDetailsState.MessageData(
                R.string.generic_error,
                R.string.product_details_error_message
            ),
            finish = false,
            errorFavorite = false,
            isFavorite = false
        )
    }

    fun messageDisplayed() {
        productDetailsStateFlow.value = ProductDetailsState.empty()
    }

    private fun showErrorFav(favorite: Boolean) {
        productDetailsStateFlow.value = ProductDetailsState(
            showMessage = false,
            messageData = null,
            finish = false,
            errorFavorite = true,
            isFavorite = favorite
        )
    }

    private fun addToLocalFavorites(){
        dataManager.getUserFavorites().add(product.uuid)
    }

    private fun removeFromLocalFavorites(){
        dataManager.getUserFavorites().remove(product.uuid)
    }

    fun setProduct(product: Product) {
        this.product = product
        this.selectedCategoryId = product.categoryId?.toInt()
    }

    data class ProductDetailsState(
        val showMessage: Boolean,
        val messageData: MessageData?,
        val finish: Boolean,
        val errorFavorite: Boolean,
        val isFavorite: Boolean,
    ) {
        data class MessageData(val messageTitle: Int, val messageDescription: Int)
        companion object {
            fun empty() = ProductDetailsState(
                showMessage = false,
                messageData = null,
                finish = false,
                errorFavorite = false,
                isFavorite = false,
            )
        }
    }

    data class ProductDetailsUserState(val user: User?) {
        companion object {
            fun empty() = ProductDetailsUserState(
                user = null
            )
        }
    }
}