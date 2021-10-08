package com.alberto.tradepop.network.userDataManager

import android.util.Log
import com.alberto.tradepop.network.models.User
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.lang.Error
import java.lang.Exception

class UserDataManagerImp :
    UserDataManager {
    override suspend fun login(email: String, password: String): Boolean {
        return try {
            Firebase.auth.signInWithEmailAndPassword(email, password).await()
            true
        } catch (ex: Exception) {
            false
        }
    }

    override suspend fun register(username: String, email: String, password: String): Boolean {
        return try {
            val result = Firebase.auth.createUserWithEmailAndPassword(email, password).await()
            val profileUpdates = userProfileChangeRequest {
                displayName = username
            }
            result.user?.updateProfile(profileUpdates)?.await()
            true
        } catch (ex: Exception) {
            false
        }
    }

    override suspend fun logout(): Boolean {
        return try {
            Firebase.auth.signOut()
            true
        } catch (ex: Exception) {
            false
        }
    }

    override suspend fun changePassword(email: String): Boolean {
        return try {
            Firebase.auth.sendPasswordResetEmail(email).await()
            true
        } catch (ex: Exception) {
            false
        }
    }

    override fun getCurrentUser(): User? {
        val firebaseUser = Firebase.auth.currentUser ?: return null
        return User(firebaseUser.uid, firebaseUser.displayName, firebaseUser.email)
    }
}