package com.alberto.tradepop.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alberto.tradepop.loginRegister.LoginRegisterViewModel
import com.alberto.tradepop.network.models.User
import com.alberto.tradepop.network.userDataManager.UserDataManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(private val userDataManager: UserDataManager) : ViewModel() {

    private val userStateFlow: MutableStateFlow<ProfileUserState> = MutableStateFlow(
        ProfileUserState.empty()
    )
    val userState: StateFlow<ProfileUserState>
        get() = userStateFlow

    fun checkUserStatus() {
        viewModelScope.launch {
            val user = userDataManager.getCurrentUser()
            userStateFlow.value = ProfileUserState(user = user)
        }
    }

    fun logOut(){
        viewModelScope.launch {
            userDataManager.logout()
            userStateFlow.value = ProfileUserState(user = null)
        }
    }

    data class ProfileUserState(val user: User?) {
        companion object {
            fun empty() = ProfileUserState(
                user = null
            )
        }
    }

}