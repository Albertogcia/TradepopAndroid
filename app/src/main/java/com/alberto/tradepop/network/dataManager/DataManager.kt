package com.alberto.tradepop.network.dataManager

import com.alberto.tradepop.network.models.Product
import java.io.File

interface DataManager {

    suspend fun uploadImage(imageFile: File): String?

    suspend fun uploadProduct(
        title: String,
        description: String,
        categoryId: Int,
        price: Double,
        imageUrl: String,
        userUuid: String,
        userName: String
    ): Boolean

    suspend fun updateProduct(
        uuid: String, title: String, description: String, categoryId: Int,
        price: Double,
        imageUrl: String,
    ): Boolean

    suspend fun deleteProduct(uuid: String): Boolean

    suspend fun buyProduct(
        uuid: String,
        buyerUuid: String,
        buyerName: String,
        sellerUuid: String,
        sellerName: String,
        price: Double,
        productName: String
    ): Boolean

    suspend fun getAllProducts(userUuid: String?): List<Product>?

    suspend fun getUserProducts(userUuid: String?): List<Product>?
}