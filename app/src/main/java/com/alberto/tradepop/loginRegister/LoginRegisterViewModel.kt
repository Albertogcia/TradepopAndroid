package com.alberto.tradepop.loginRegister

import android.provider.Settings.Global.getString
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alberto.tradepop.R
import com.alberto.tradepop.network.userDataManager.UserDataManager
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginRegisterViewModel(
    private val userDataManager: UserDataManager
): ViewModel() {

    private val stateFlow: MutableStateFlow<LoginRegisterState> = MutableStateFlow(LoginRegisterState.empty())
    val state: StateFlow<LoginRegisterState>
        get() = stateFlow

    fun loginButtonTapped(email: String, password: String){
        if(email.isEmpty() || password.isEmpty()){
            showErrorMessage(R.string.loading_register_empty_fields_error_message)
        }
        else{
            viewModelScope.launch {
                val success = userDataManager.login(email, password)
                if(success){
                    stateFlow.value = LoginRegisterState(
                        showMessage = false,
                        dismissActivity = true,
                        messageData = null
                    )
                }
                else{
                    stateFlow.value = LoginRegisterState(
                        showMessage = true,
                        dismissActivity = false,
                        messageData = LoginRegisterState.MessageData(R.string.generic_error, R.string.loading_register_login_error_message)
                    )
                }
            }
        }
    }

    fun registerButtonTapped(username: String, email: String, password: String){
        if(username.isEmpty() || email.isEmpty() || password.isEmpty()){
            showErrorMessage(R.string.loading_register_empty_fields_error_message)
        }
        else{
            viewModelScope.launch {
                val success = userDataManager.register(username, email, password)
                if(success){
                    stateFlow.value = LoginRegisterState(
                        showMessage = false,
                        dismissActivity = true,
                        messageData = null
                    )
                }else{
                    stateFlow.value = LoginRegisterState(
                        showMessage = true,
                        dismissActivity = false,
                        messageData = LoginRegisterState.MessageData(R.string.generic_error, R.string.loading_register_register_error_message)
                    )
                }
            }
        }
    }

    fun sendResetPasswordEmail(email: String){
        viewModelScope.launch {
            val success = userDataManager.changePassword(email)
            if(success){
                stateFlow.value = LoginRegisterState(
                    showMessage = true,
                    dismissActivity = false,
                    messageData = LoginRegisterState.MessageData(R.string.login_register_recover_password_email_sent_title, R.string.login_register_recover_password_email_sent)
                )
            }
            else{
                stateFlow.value = LoginRegisterState(
                    showMessage = true,
                    dismissActivity = false,
                    messageData = LoginRegisterState.MessageData(R.string.generic_error, R.string.loading_register_register_error_message)
                )
            }
        }
    }

    fun messageDisplayed(){
        stateFlow.value = LoginRegisterState.empty()
    }

    private fun showErrorMessage(errorMessage: Int){
        stateFlow.value = LoginRegisterState(
            showMessage = true,
            dismissActivity = false,
            messageData = LoginRegisterState.MessageData(R.string.generic_error, errorMessage)
        )
    }

    data class LoginRegisterState(val showMessage: Boolean, val dismissActivity: Boolean, val messageData: MessageData?){
        data class MessageData(val messageTitle: Int, val messageDescription: Int)
        companion object{
            fun empty() = LoginRegisterState(
                showMessage = false,
                dismissActivity = false,
                messageData = null
            )
        }
    }


}