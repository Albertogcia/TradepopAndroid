package com.alberto.tradepop.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.alberto.tradepop.loginRegister.LoginRegisterViewModel
import com.alberto.tradepop.newProduct.NewProductViewModel
import com.alberto.tradepop.products.ProductsViewModel
import com.alberto.tradepop.profile.ProfileViewModel
import org.kodein.di.*
import org.kodein.type.erased

object ViewModelDIModule : DIBaseModule("ViewModelDIModule") {

    override val builder: DI.Builder.() -> Unit = {
        bind<ViewModelProvider.Factory>() with singleton {
            DIViewModelFactory(di)
        }

        bind<LoginRegisterViewModel>() with singleton { LoginRegisterViewModel(instance()) }
        bind<ProfileViewModel>() with singleton { ProfileViewModel(instance()) }
        bind<NewProductViewModel>() with singleton { NewProductViewModel(instance(), instance()) }
        bind<ProductsViewModel>() with singleton { ProductsViewModel(instance(), instance()) }
    }

    class DIViewModelFactory(private val di: DI) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return di.direct.Instance(erased(modelClass))
        }
    }

}