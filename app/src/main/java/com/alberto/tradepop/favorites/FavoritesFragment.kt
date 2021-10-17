package com.alberto.tradepop.favorites

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alberto.tradepop.Extensions.DialogUtils
import com.alberto.tradepop.Extensions.SpacesItemDecoration
import com.alberto.tradepop.R
import com.alberto.tradepop.databinding.FragmentFavoritesBinding
import com.alberto.tradepop.network.models.Product
import com.alberto.tradepop.productDetails.ProductDetailsActivity
import com.alberto.tradepop.products.ProductRecyclerAdapter
import com.alberto.tradepop.profile.ProfileFragment
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.x.di
import org.kodein.di.direct
import org.kodein.di.instance

class FavoritesFragment : Fragment(), DIAware {

    override val di: DI by di()
    private val viewModel: FavoritesViewModel by lazy {
        ViewModelProvider(
            this,
            direct.instance()
        ).get(FavoritesViewModel::class.java)
    }

    private lateinit var binding: FragmentFavoritesBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.binding = FragmentFavoritesBinding.inflate(inflater)
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
                        binding.noUserFragment.isVisible = false
                        binding.swipeRefreshLayout.isVisible = true
                    } else {
                        binding.swipeRefreshLayout.isVisible = false
                        binding.noUserFragment.isVisible = true
                    }
                }
            }
            launch {
                viewModel.favoritesState.collect { state ->
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
        viewModel.checkUserStatus()
        return binding.root
    }

    private fun productSelected(product: Product) {
        val intent = Intent(requireContext(), ProductDetailsActivity::class.java)
        intent.putExtra("product", product)
        resultLauncher.launch(intent)
    }

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                viewModel.getProducts()
            }
        }

    override fun onResume() {
        viewModel.checkUserStatus()
        super.onResume()
    }

    companion object {
        fun newInstance(): FavoritesFragment = FavoritesFragment()
    }
}