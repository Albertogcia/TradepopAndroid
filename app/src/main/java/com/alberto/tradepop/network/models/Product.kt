package com.alberto.tradepop.network.models

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firestore.v1.Document
import java.io.Serializable

class Product(
    val uuid: String ,
    val title: String?,
    val description: String?,
    val owner: String?,
    val ownerName: String?,
    val price: Double?,
    val categoryId: Int?,
    val coverImageUrl: String?
): Serializable {
    companion object {
        fun fromFirestoreDocument(document: DocumentSnapshot): Product{
            with(document.data){
                val uuid = document.id
                val title = this?.get("title") as? String
                val description = this?.get("description") as? String
                val owner = this?.get("owner") as? String
                val ownerName = this?.get("ownerName") as? String
                val price = this?.get("price") as? Double
                val categoryId = (this?.get("categoryId") as? Number)?.toInt()
                val coverImageUrl = this?.get("coverImageUrl") as? String
                return Product(uuid, title, description, owner, ownerName, price, categoryId, coverImageUrl)
            }
        }
    }
}