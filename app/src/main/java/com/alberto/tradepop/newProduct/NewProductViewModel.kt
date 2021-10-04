package com.alberto.tradepop.newProduct

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NewProductViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is new product Fragment"
    }
    val text: LiveData<String> = _text
}