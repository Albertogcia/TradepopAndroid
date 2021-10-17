package com.alberto.tradepop.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.alberto.tradepop.favorites.FavoritesViewModel
import com.alberto.tradepop.loginRegister.LoginRegisterViewModel
import com.alberto.tradepop.newProduct.NewProductViewModel
import com.alberto.tradepop.productDetails.ProductDetailsViewModel
import com.alberto.tradepop.products.ProductsViewModel
import com.alberto.tradepop.profile.ProfileViewModel
import com.alberto.tradepop.transactions.TransactionsViewModel
import org.kodein.di.*
import org.kodein.type.erased

object ViewModelDIModule : DIBaseModule("ViewModelDIModule") {

    override val builder: DI.Builder.() -> Unit = {
        bind<ViewModelProvider.Factory>() with singleton {
            DIViewModelFactory(di)
        }

        bind<LoginRegisterViewModel>() with provider { LoginRegisterViewModel(instance()) }
        bind<ProfileViewModel>() with singleton { ProfileViewModel(instance(), instance()) }
        bind<NewProductViewModel>() with provider { NewProductViewModel(instance(), instance()) }
        bind<ProductsViewModel>() with singleton { ProductsViewModel(instance(), instance()) }
        bind<ProductDetailsViewModel>() with provider { ProductDetailsViewModel(instance(), instance()) }
        bind<FavoritesViewModel>() with singleton { FavoritesViewModel(instance(), instance()) }
        bind<TransactionsViewModel>() with singleton { TransactionsViewModel(instance(), instance()) }
    }

    class DIViewModelFactory(private val di: DI) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return di.direct.Instance(erased(modelClass))
        }
    }

}