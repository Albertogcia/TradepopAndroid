package com.alberto.tradepop.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.alberto.tradepop.loginRegister.LoginRegisterViewModel
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.direct
import org.kodein.di.singleton
import org.kodein.type.erased

object ViewModelDIModule: DIBaseModule("ViewModelDIModule") {

    override val builder: DI.Builder.() -> Unit = {
        bind<ViewModelProvider.Factory>() with singleton {
            DIViewModelFactory(di)
        }

        bind<LoginRegisterViewModel>() with singleton { LoginRegisterViewModel() }
    }

    class DIViewModelFactory(private val di: DI): ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return di.direct.Instance(erased(modelClass))
        }
    }

}