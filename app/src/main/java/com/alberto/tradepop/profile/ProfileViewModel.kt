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

    private val stateFlow: MutableStateFlow<ProfileState> = MutableStateFlow(
        ProfileState.empty()
    )
    val state: StateFlow<ProfileState>
        get() = stateFlow

    fun checkUserStatus() {
        viewModelScope.launch {
            val user = userDataManager.getCurrentUser()
            stateFlow.value = ProfileState(user = user)
        }
    }

    fun logOut(){
        viewModelScope.launch {
            userDataManager.logout()
            stateFlow.value = ProfileState(user = null)
        }
    }

    data class ProfileState(val user: User?) {
        companion object {
            fun empty() = ProfileState(
                user = null
            )
        }
    }

}