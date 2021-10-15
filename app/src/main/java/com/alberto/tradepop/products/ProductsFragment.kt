package com.alberto.tradepop.products

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
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
import com.alberto.tradepop.databinding.FragmentNewProductBinding
import com.alberto.tradepop.databinding.FragmentProductsBinding
import com.alberto.tradepop.network.models.Product
import com.alberto.tradepop.newProduct.NewProductViewModel
import com.alberto.tradepop.profile.ProfileFragment
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.x.di
import org.kodein.di.direct
import org.kodein.di.instance

class ProductsFragment : Fragment(), DIAware {

    override val di: DI by di()
    private val viewModel: ProductsViewModel by lazy {
        ViewModelProvider(
            this,
            direct.instance()
        ).get(ProductsViewModel::class.java)
    }

    private lateinit var binding: FragmentProductsBinding

    private var loadingDialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.binding = FragmentProductsBinding.inflate(inflater)
        GridLayoutManager(context, 2, RecyclerView.VERTICAL, false).apply {
            binding.recyclerView.layoutManager = this
        }
        val spacing = resources.getDimensionPixelSize(R.dimen.recycler_grid_spacing)
        binding.recyclerView.addItemDecoration(SpacesItemDecoration(spacing))
        val adapter = ProductRecyclerAdapter(::productSelected)
        binding.recyclerView.adapter = adapter
        lifecycleScope.launchWhenStarted {
            launch {
                viewModel.productsState.collect { state ->
                    if(state.showMessage){
                        state.messageData?.let {
                            binding.loadingBar.isVisible = false
                            binding.swipeRefreshLayout.isRefreshing = false
                            loadingDialog?.dismiss()
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
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                p0?.let {
                    viewModel.filterProducts(it)
                }
                return false
            }

        })
        viewModel.textToSearch.let {
            if(it != ""){
                binding.searchView.setQuery(viewModel.textToSearch, false)
                binding.searchView.isIconified = false
                binding.searchView.clearFocus()
            }
        }
        viewModel.checkUserStatus()
        return binding.root
    }

    private fun productSelected(product: Product) {

    }

    companion object {
        fun newInstance(): ProductsFragment = ProductsFragment()
    }
}