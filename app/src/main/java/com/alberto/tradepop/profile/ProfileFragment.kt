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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alberto.tradepop.Extensions.DialogUtils
import com.alberto.tradepop.Extensions.SpacesItemDecoration
import com.alberto.tradepop.R
import com.alberto.tradepop.databinding.ActivityLoginRegisterBinding
import com.alberto.tradepop.databinding.FragmentProfileBinding
import com.alberto.tradepop.loginRegister.LoginRegisterViewModel
import com.alberto.tradepop.network.models.Product
import com.alberto.tradepop.newProduct.NewProductFragment
import com.alberto.tradepop.products.ProductRecyclerAdapter
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
        GridLayoutManager(context, 2, RecyclerView.VERTICAL, false).apply {
            binding.recyclerView.layoutManager = this
        }
        val spacing = resources.getDimensionPixelSize(R.dimen.recycler_grid_spacing)
        binding.recyclerView.addItemDecoration(SpacesItemDecoration(spacing))
        val adapter = ProductRecyclerAdapter(::productSelected)
        binding.recyclerView.adapter = adapter
        lifecycleScope.launchWhenStarted {
            launch {
                viewModel.userState.collect { state ->
                    if (state.user != null) {
                        binding.userNameTextView.text = state.user.username ?: ""
                        binding.noUserFragment.isVisible = false
                        binding.mainLayout.isVisible = true
                    } else {
                        binding.mainLayout.isVisible = false
                        binding.noUserFragment.isVisible = true
                    }
                }
            }
            launch {
                viewModel.profileState.collect { state ->
                    if (state.showMessage) {
                        state.messageData?.let {
                            binding.loadingBar.isVisible = false
                            binding.swipeRefreshLayout.isRefreshing = false
                            DialogUtils().showSimpleMessage(
                                resources.getString(it.messageTitle),
                                resources.getString(it.messageDescription),
                                resources.getString(R.string.generic_ok),
                                requireContext()
                            )
                            viewModel.messageDisplayed()
                        }
                    }
                    binding.swipeRefreshLayout.isRefreshing = false
                    binding.loadingBar.isVisible = false
                    adapter.productsList = state.products
                }
            }
        }
        binding.swipeRefreshLayout.setOnRefreshListener { viewModel.getProducts() }
        binding.logOutButton.setOnClickListener { viewModel.logOut() }
        viewModel.checkUserStatus()
        return this.binding.root
    }

    private fun productSelected(product: Product) {

    }

    override fun onResume() {
        viewModel.checkUserStatus()
        super.onResume()
    }

    companion object {
        fun newInstance(): ProfileFragment = ProfileFragment()
    }
}