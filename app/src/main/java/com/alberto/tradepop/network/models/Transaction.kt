package com.alberto.tradepop.network.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.util.*

class Transaction(
    val uuid: String,
    val buyerName: String?,
    val buyerUuid: String?,
    val price: Double?,
    val productName: String?,
    val sellerName: String?,
    val sellerUuid: String?,
    val date: Date?,
    val isPurchase: Boolean
) {
    companion object {
        fun fromFirestoreDocument(document: DocumentSnapshot, userUuid: String): Transaction{
            with(document.data){
                val uuid = document.id
                val buyerName = this?.get("buyerName") as? String
                val buyerUuid = this?.get("buyerUuid") as? String
                val price = this?.get("price") as? Double
                val productName = this?.get("productName") as? String
                val sellerName = this?.get("sellerName") as? String
                val sellerUuid = this?.get("sellerUuid") as? String
                val date = (this?.get("date") as? Timestamp)?.toDate()
                val isPurchase = userUuid == buyerUuid
                return Transaction(uuid, buyerName, buyerUuid, price, productName, sellerName, sellerUuid, date, isPurchase)
            }
        }
    }
}