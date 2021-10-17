package com.alberto.tradepop.transactions

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alberto.tradepop.R
import com.alberto.tradepop.favorites.FavoritesViewModel
import com.alberto.tradepop.network.dataManager.DataManager
import com.alberto.tradepop.network.models.Product
import com.alberto.tradepop.network.models.Transaction
import com.alberto.tradepop.network.models.User
import com.alberto.tradepop.network.userDataManager.UserDataManager
import com.alberto.tradepop.profile.ProfileViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TransactionsViewModel(
    private val userDataManager: UserDataManager,
    private val dataManager: DataManager
) : ViewModel() {

    private val userStateFlow: MutableStateFlow<TransactionsUserState> = MutableStateFlow(
        TransactionsUserState.empty()
    )
    val userState: StateFlow<TransactionsUserState>
        get() = userStateFlow

    private val transactionsStateFlow: MutableStateFlow<TransactionsState> = MutableStateFlow(
        TransactionsState.empty()
    )
    val transactionsState: StateFlow<TransactionsState>
        get() = transactionsStateFlow

    private var previousUserUuid: String = ""

    fun checkUserStatus() {
        val user = userDataManager.getCurrentUser()
        user?.let {
            userStateFlow.value = TransactionsUserState(user = user)
            if (previousUserUuid != it.uuid) {
                previousUserUuid = it.uuid
                getTransactions()
            }
        } ?: run {
            previousUserUuid = ""
            userStateFlow.value = TransactionsUserState(user = user)
        }
    }

    fun getTransactions(){
        viewModelScope.launch {
            val transactions = dataManager.getUserTransactions(previousUserUuid)
            transactions?.let {
                transactionsStateFlow.value = TransactionsState(
                    transactions = it,
                    showMessage = false,
                    messageData = null
                )
            } ?: run {
                transactionsStateFlow.value = TransactionsState(
                    transactions = transactionsState.value.transactions,
                    showMessage = true,
                    messageData = TransactionsState.MessageData(
                        R.string.generic_error,
                        R.string.new_product_upload_product_error_message
                    )
                )
            }
        }
    }

    fun messageDisplayed() {
        transactionsStateFlow.value = TransactionsState(
            transactions = transactionsState.value.transactions,
            showMessage = false,
            messageData = null
        )
    }

    data class TransactionsUserState(val user: User?) {
        companion object {
            fun empty() = TransactionsUserState(
                user = null
            )
        }
    }

    data class TransactionsState(
        val transactions: List<Transaction>,
        val showMessage: Boolean,
        val messageData: MessageData?
    ) {
        data class MessageData(val messageTitle: Int, val messageDescription: Int)
        companion object {
            fun empty() = TransactionsState(
                transactions = emptyList(),
                showMessage = false,
                messageData = null
            )
        }
    }
}