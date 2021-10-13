package com.alberto.tradepop.network.dataManager

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
}