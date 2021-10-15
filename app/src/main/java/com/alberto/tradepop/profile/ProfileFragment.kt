package com.alberto.tradepop.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.alberto.tradepop.databinding.ActivityLoginRegisterBinding
import com.alberto.tradepop.databinding.FragmentProfileBinding
import com.alberto.tradepop.loginRegister.LoginRegisterViewModel
import com.alberto.tradepop.newProduct.NewProductFragment
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.kodein.di.android.di
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.x.di
import org.kodein.di.direct
import org.kodein.di.instance

class ProfileFragment : Fragment(), DIAware {

    override val di: DI by di()
    private val viewModel: ProfileViewModel by lazy {
        ViewModelProvider(
            this,
            direct.instance()
        ).get(ProfileViewModel::class.java)
    }

    private lateinit var binding: FragmentProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.binding = FragmentProfileBinding.inflate(layoutInflater)
        lifecycleScope.launchWhenStarted {
            launch {
                viewModel.userState.collect { state ->
                    if(state.user != null){
                        binding.userNameTextView.text = state.user.username ?: ""
                        binding.noUserFragment.isVisible = false
                        binding.mainLayout.isVisible = true
                    }
                    else{
                        binding.mainLayout.isVisible = false
                        binding.noUserFragment.isVisible = true
                    }
                }
            }
        }
        viewModel.checkUserStatus()
        binding.logOutButton.setOnClickListener { viewModel.logOut() }
        return this.binding.root
    }

    override fun onResume() {
        viewModel.checkUserStatus()
        super.onResume()
    }

    companion object {
        fun newInstance(): ProfileFragment = ProfileFragment()
    }
}