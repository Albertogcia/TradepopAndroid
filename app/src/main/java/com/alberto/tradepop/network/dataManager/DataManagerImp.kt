package com.alberto.tradepop.network.dataManager

import android.net.Uri
import android.os.ParcelUuid
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
import java.io.File
import java.lang.Exception
import java.util.*

private val PRODUCTS_COLLECTION_KEY = "products"

class DataManagerImp : DataManager {

    private val db = Firebase.firestore
    private val storage = Firebase.storage

    override suspend fun uploadImage(imageFile: File): String? {
        return try {
            val uri = Uri.fromFile(imageFile)
            val uuid = UUID.randomUUID().toString().lowercase()+".jpeg"
            val childRef = storage.reference.child(uuid)
            childRef.putFile(uri).await()
            var downloadUri = childRef.downloadUrl.await()
            downloadUri.toString()
        } catch (ex: Exception) {
            null
        }
    }

    override suspend fun uploadProduct(
        title: String,
        description: String,
        categoryId: Int,
        price: Double,
        imageUrl: String,
        userUuid: String,
        userName: String
    ): Boolean {
        return try {
            val product = hashMapOf(
                "title" to title,
                "description" to description,
                "categoryId" to categoryId,
                "price" to price,
                "coverImageUrl" to imageUrl,
                "owner" to userUuid,
                "ownerName" to userName,
                "date" to Date()
            )
            db.collection(PRODUCTS_COLLECTION_KEY).add(product).await()
            true
        } catch (ex: Exception) {
            false
        }
    }
}