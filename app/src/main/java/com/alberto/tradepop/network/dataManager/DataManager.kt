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

    suspend fun getAllProducts(userUuid: String?): List<Product>?

    suspend fun getUserProducts(userUuid: String?): List<Product>?
}