package com.alberto.tradepop.network.dataManager

import android.net.Uri
import android.os.ParcelUuid
import android.util.Log
import com.alberto.tradepop.network.models.Product
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
import java.io.File
import java.lang.Exception
import java.util.*

private const val PRODUCTS_COLLECTION_KEY = "products"
private const val TRANSACTIONS_COLLECTION_KEY = "transactions"

class DataManagerImp : DataManager {

    private val db = Firebase.firestore
    private val storage = Firebase.storage

    override suspend fun uploadImage(imageFile: File): String? {
        return try {
            val uri = Uri.fromFile(imageFile)
            val uuid = UUID.randomUUID().toString().lowercase() + ".jpeg"
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

    override suspend fun updateProduct(
        uuid: String, title: String, description: String, categoryId: Int,
        price: Double,
        imageUrl: String,
    ): Boolean {
        return try {
            val product = hashMapOf(
                "title" to title,
                "description" to description,
                "categoryId" to categoryId,
                "price" to price,
                "coverImageUrl" to imageUrl
            )
            db.collection(PRODUCTS_COLLECTION_KEY).document(uuid).set(product, SetOptions.merge())
                .await()
            true
        } catch (ex: Exception) {
            false
        }
    }

    override suspend fun deleteProduct(uuid: String): Boolean {
        return try {
            db.collection(PRODUCTS_COLLECTION_KEY).document(uuid).delete().await()
            true
        } catch (ex: Exception) {
            false
        }
    }

    override suspend fun buyProduct(
        uuid: String,
        buyerUuid: String,
        buyerName: String,
        sellerUuid: String,
        sellerName: String,
        price: Double,
        productName: String
    ): Boolean {
        val transaction = hashMapOf(
            "buyerUuid" to buyerUuid,
            "buyerName" to buyerName,
            "sellerUuid" to sellerUuid,
            "sellerName" to sellerName,
            "price" to price,
            "productName" to productName,
            "date" to Date()
        )
        return try {
            db.collection(TRANSACTIONS_COLLECTION_KEY).add(transaction).await()
            db.collection(PRODUCTS_COLLECTION_KEY).document(uuid).delete().await()
            true
        } catch (ex: Exception) {
            false
        }
    }

    override suspend fun getAllProducts(userUuid: String?): List<Product>? {
        return try {
            val querySnapshot =
                db.collection(PRODUCTS_COLLECTION_KEY).whereNotEqualTo("owner", userUuid)
                    .orderBy("owner").orderBy("date", Query.Direction.DESCENDING).get().await()
            val products = querySnapshot.documents.map {
                Product.fromFirestoreDocument(it)
            }
            products
        } catch (ex: Exception) {
            null
        }
    }

    override suspend fun getUserProducts(userUuid: String?): List<Product>? {
        return try {
            val querySnapshot =
                db.collection(PRODUCTS_COLLECTION_KEY).whereEqualTo("owner", userUuid)
                    .orderBy("date", Query.Direction.DESCENDING).get().await()
            val products = querySnapshot.documents.map {
                Product.fromFirestoreDocument(it)
            }
            products
        } catch (ex: Exception) {
            null
        }
    }
}