package com.alberto.tradepop.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alberto.tradepop.R
import com.alberto.tradepop.network.dataManager.DataManager
import com.alberto.tradepop.network.models.Product
import com.alberto.tradepop.network.models.User
import com.alberto.tradepop.network.userDataManager.UserDataManager
import com.alberto.tradepop.profile.ProfileViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val userDataManager: UserDataManager,
    private val dataManager: DataManager
) : ViewModel() {

    private val userStateFlow: MutableStateFlow<FavoritesUserState> = MutableStateFlow(
        FavoritesUserState.empty()
    )
    val userState: StateFlow<FavoritesUserState>
        get() = userStateFlow

    private val favoritesStateFlow: MutableStateFlow<FavoritesState> = MutableStateFlow(
        FavoritesState.empty()
    )
    val favoritesState: StateFlow<FavoritesState>
        get() = favoritesStateFlow

    private var previousUserUuid: String = ""

    fun checkUserStatus() {
        val user = userDataManager.getCurrentUser()
        user?.let {
            userStateFlow.value = FavoritesUserState(user = user)
            if (previousUserUuid != it.uuid) {
                previousUserUuid = it.uuid
                getProducts()
            }
        } ?: run {
            previousUserUuid = ""
            getProducts()
            userStateFlow.value = FavoritesUserState(user = user)
        }
    }

    fun getProducts() {
        viewModelScope.launch {
            val products = dataManager.getProductsFromFavorites(previousUserUuid ?: "")
            products?.let {
                favoritesStateFlow.value = FavoritesState(
                    products = it,
                    showMessage = false,
                    messageData = null
                )
            } ?: run {
                favoritesStateFlow.value = FavoritesState(
                    products = favoritesState.value.products,
                    showMessage = true,
                    messageData = FavoritesState.MessageData(
                        R.string.generic_error,
                        R.string.new_product_upload_product_error_message
                    )
                )
            }
        }
    }

    fun messageDisplayed() {
        favoritesStateFlow.value = FavoritesState(
            products = favoritesStateFlow.value.products,
            showMessage = false,
            messageData = null
        )
    }

    data class FavoritesUserState(val user: User?) {
        companion object {
            fun empty() = FavoritesUserState(
                user = null
            )
        }
    }

    data class FavoritesState(
        val products: List<Product>,
        val showMessage: Boolean,
        val messageData: MessageData?
    ) {
        data class MessageData(val messageTitle: Int, val messageDescription: Int)
        companion object {
            fun empty() = FavoritesState(
                products = emptyList(),
                showMessage = false,
                messageData = null
            )
        }
    }
}