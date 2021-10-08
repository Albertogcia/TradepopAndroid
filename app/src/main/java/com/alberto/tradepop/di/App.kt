package com.alberto.tradepop.di

import android.app.Application
import org.kodein.di.DI
import org.kodein.di.DIAware

class App: Application(), DIAware {

    override val di: DI by DI.lazy {
        import(AppDIModule(application = this@App).create())
        import(ViewModelDIModule.create())
        import(UserDataManagerDIModule.create())
    }

}