package com.alberto.tradepop.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alberto.tradepop.R
import com.alberto.tradepop.loginRegister.LoginRegisterViewModel
import com.alberto.tradepop.network.dataManager.DataManager
import com.alberto.tradepop.network.models.Product
import com.alberto.tradepop.network.models.User
import com.alberto.tradepop.network.userDataManager.UserDataManager
import com.alberto.tradepop.products.ProductsViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userDataManager: UserDataManager,
    private val dataManager: DataManager
) : ViewModel() {

    private val userStateFlow: MutableStateFlow<ProfileUserState> = MutableStateFlow(
        ProfileUserState.empty()
    )
    val userState: StateFlow<ProfileUserState>
        get() = userStateFlow

    private val profileStateFlow: MutableStateFlow<ProfileState> = MutableStateFlow(
        ProfileState.empty()
    )
    val profileState: StateFlow<ProfileState>
        get() = profileStateFlow

    private var previousUserUuid: String = ""

    fun checkUserStatus() {
        val user = userDataManager.getCurrentUser()
        user?.let {
            userStateFlow.value = ProfileUserState(user = user)
            if (previousUserUuid != it.uuid) {
                previousUserUuid = it.uuid
                getProducts()
            }
        } ?: run {
            previousUserUuid = ""
            getProducts()
            userStateFlow.value = ProfileUserState(user = user)
        }
    }

    fun getProducts() {
        var userUuid: String? = null
        if (previousUserUuid != "") {
            userUuid = previousUserUuid
        }
        viewModelScope.launch {
            val products = dataManager.getUserProducts(userUuid)
            products?.let {
                profileStateFlow.value = ProfileState(
                    products = it,
                    showMessage = false,
                    messageData = null
                )
            } ?: run {
                profileStateFlow.value = ProfileState(
                    products = profileState.value.products,
                    showMessage = true,
                    messageData = ProfileState.MessageData(
                        R.string.generic_error,
                        R.string.new_product_upload_product_error_message
                    )
                )
            }
        }
    }

    fun logOut() {
        viewModelScope.launch {
            userDataManager.logout()
            userStateFlow.value = ProfileUserState(user = null)
        }
    }

    fun messageDisplayed() {
        profileStateFlow.value = ProfileState(
            products = profileStateFlow.value.products,
            showMessage = false,
            messageData = null
        )
    }

    data class ProfileUserState(val user: User?) {
        companion object {
            fun empty() = ProfileUserState(
                user = null
            )
        }
    }

    data class ProfileState(
        val products: List<Product>,
        val showMessage: Boolean,
        val messageData: MessageData?
    ) {
        data class MessageData(val messageTitle: Int, val messageDescription: Int)
        companion object {
            fun empty() = ProfileState(
                products = emptyList(),
                showMessage = false,
                messageData = null
            )
        }
    }

}