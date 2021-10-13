package com.alberto.tradepop.newProduct

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alberto.tradepop.R
import com.alberto.tradepop.loginRegister.LoginRegisterViewModel
import com.alberto.tradepop.network.models.User
import com.alberto.tradepop.network.dataManager.DataManager
import com.alberto.tradepop.network.userDataManager.UserDataManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

class NewProductViewModel(
    private val userDataManager: UserDataManager,
    private val dataManager: DataManager
) : ViewModel() {

    private val userStateFlow: MutableStateFlow<NewProductUserState> = MutableStateFlow(
        NewProductUserState.empty()
    )
    val userState: StateFlow<NewProductUserState>
        get() = userStateFlow

    private val stateFlow: MutableStateFlow<NewProductState> = MutableStateFlow(
        NewProductState.empty()
    )
    val state: StateFlow<NewProductState>
        get() = stateFlow

    var coverImageFile: File? = null
    var selectedCategoryId: Int? = null

    fun checkUserStatus() {
        viewModelScope.launch {
            val user = userDataManager.getCurrentUser()
            userStateFlow.value = NewProductUserState(user = user)
        }
    }

    fun checkFields(title: String, description: String, price: String) {
        if (title.isEmpty() || description.isEmpty() || price.isEmpty() || coverImageFile == null || selectedCategoryId == null) {
            stateFlow.value = NewProductState(
                showMessage = true,
                reloadData = false,
                messageData = NewProductState.MessageData(
                    R.string.generic_error,
                    R.string.new_product_empty_fields_error_message
                )
            )
            return
        }
        uploadProduct(title, description, price.toDouble())
    }

    fun messageDisplayed(){
        stateFlow.value = NewProductState.empty()
    }

    fun resetViewModel(){
        stateFlow.value = NewProductState.empty()
        coverImageFile = null
        selectedCategoryId = null
    }

    private fun uploadProduct(title: String, description: String, price: Double) {
        viewModelScope.launch {
            userDataManager.getCurrentUser()?.let { currentUser ->
                coverImageFile?.let { imageFile ->
                    dataManager.uploadImage(imageFile)?.let { imageUrl ->
                        val success = dataManager.uploadProduct(
                            title,
                            description,
                            selectedCategoryId ?: 1,
                            price,
                            imageUrl,
                            currentUser.uuid,
                            currentUser.username ?: " "
                        )
                        if (success) {
                            stateFlow.value = NewProductState(
                                showMessage = false,
                                reloadData = true,
                                messageData = null
                            )
                        } else {
                            showError()
                        }
                    }
                } ?: showError()
            } ?: showError()
        }
    }

    private fun showError() {
        stateFlow.value = NewProductState(
            showMessage = true,
            reloadData = false,
            messageData = NewProductState.MessageData(
                R.string.generic_error,
                R.string.new_product_upload_product_error_message
            )
        )
    }

    data class NewProductUserState(val user: User?) {
        companion object {
            fun empty() = NewProductUserState(
                user = null
            )
        }
    }

    data class NewProductState(
        val showMessage: Boolean,
        val reloadData: Boolean,
        val messageData: MessageData?
    ) {
        data class MessageData(val messageTitle: Int, val messageDescription: Int)
        companion object {
            fun empty() = NewProductState(
                showMessage = false,
                reloadData = false,
                messageData = null
            )
        }
    }
}