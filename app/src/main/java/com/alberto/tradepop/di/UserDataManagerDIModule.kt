package com.alberto.tradepop.di

import com.alberto.tradepop.network.userDataManager.UserDataManager
import com.alberto.tradepop.network.userDataManager.UserDataManagerImp
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton

object UserDataManagerDIModule: DIBaseModule("NetworkDIModule") {

    override val builder: DI.Builder.() -> Unit = {
        bind<UserDataManager>() with singleton { UserDataManagerImp() }
    }

}