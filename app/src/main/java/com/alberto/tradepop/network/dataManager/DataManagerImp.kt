package com.alberto.tradepop.network.dataManager

import android.net.Uri
import android.os.ParcelUuid
import android.util.Log
import com.alberto.tradepop.network.models.Product
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
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
private const val USERS_COLLECTION_KEY = "users"

class DataManagerImp : DataManager {

    private val db = Firebase.firestore
    private val storage = Firebase.storage

    private var userFavorites: MutableList<String> = mutableListOf()

    override fun getUserFavorites(): MutableList<String>{
        return this.userFavorites
    }

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

    override suspend fun getAllProducts(userUuid: String?): List<Product>?{
        return try {
            val querySnapshot =
                db.collection(PRODUCTS_COLLECTION_KEY).whereNotEqualTo("owner", userUuid)
                    .orderBy("owner").orderBy("date", Query.Direction.DESCENDING).get().await()
            val products = querySnapshot.documents.map {
                Product.fromFirestoreDocument(it)
            }
            userUuid?.let {
                getUserFavoritesList(userUuid)?.let {
                    this.userFavorites = it
                }
            }
            products
        } catch (ex: Exception) {
            null
        }
    }

    override suspend fun getProductsFromFavorites(userUuid: String): List<Product>? {
        return try {
            val userFavorites = getUserFavoritesList(userUuid)
            userFavorites?.let {
                if(userFavorites.isNotEmpty()){
                    val querySnapshot =
                        db.collection(PRODUCTS_COLLECTION_KEY).whereIn(FieldPath.documentId(), userFavorites).get().await()
                    val products = querySnapshot.documents.map {
                        Product.fromFirestoreDocument(it)
                    }
                    products.sortedByDescending { it.date }
                }
                else{
                    listOf()
                }

            } ?: run {
                this.userFavorites = mutableListOf()
                null
            }
        } catch (ex: Exception) {
            Log.d("kkk", ex.localizedMessage)
            null
        }
    }

    override suspend fun getUserFavoritesList(userUuid: String): MutableList<String>? {
        return try {
            val snapshot = db.collection(USERS_COLLECTION_KEY).document(userUuid).get().await()
            snapshot.data?.get("favorites") as? MutableList<String>
        } catch (ex: Exception) {
            this.userFavorites = mutableListOf()
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
            userUuid?.let {
                getUserFavoritesList(userUuid)?.let {
                    this.userFavorites = it
                }
            }
            products
        } catch (ex: Exception) {
            null
        }
    }

    override suspend fun addToFavorites(productUuid: String, userUuid: String): Boolean {
        return try {
            val data = hashMapOf(
                "favorites" to FieldValue.arrayUnion(productUuid)
            )
            db.collection(USERS_COLLECTION_KEY).document(userUuid).set(data, SetOptions.merge())
                .await()
            true
        } catch (ex: Exception) {
            false
        }
    }

    override suspend fun removeFromFavorites(productUuid: String, userUuid: String): Boolean {
        return try {
            val data = hashMapOf(
                "favorites" to FieldValue.arrayRemove(productUuid)
            )
            db.collection(USERS_COLLECTION_KEY).document(userUuid).set(data, SetOptions.merge())
                .await()
            true
        } catch (ex: Exception) {
            false
        }
    }
}