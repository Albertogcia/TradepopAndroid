package com.alberto.tradepop.network.userDataManager

import com.alberto.tradepop.network.models.User

interface UserDataManager {

    suspend fun login(email: String, password: String): Boolean

    suspend fun register(username: String, email: String, password: String): Boolean

    suspend fun logout(): Boolean

    suspend fun changePassword(email: String): Boolean

    fun getCurrentUser(): User?
}