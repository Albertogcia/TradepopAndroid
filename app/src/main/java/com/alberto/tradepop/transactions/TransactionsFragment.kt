package com.alberto.tradepop.transactions

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.alberto.tradepop.Extensions.DialogUtils
import com.alberto.tradepop.Extensions.TransactionsItemDecoration
import com.alberto.tradepop.R
import com.alberto.tradepop.databinding.FragmentTransactionsBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.x.di
import org.kodein.di.direct
import org.kodein.di.instance


class TransactionsFragment : Fragment(), DIAware {

    override val di: DI by di()
    private val viewModel: TransactionsViewModel by lazy {
        ViewModelProvider(
            this,
            direct.instance()
        ).get(TransactionsViewModel::class.java)
    }

    private lateinit var binding: FragmentTransactionsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.binding = FragmentTransactionsBinding.inflate(inflater)
        val spacing = resources.getDimensionPixelSize(R.dimen.recycler_transaction_spacing)
        binding.recyclerView.addItemDecoration(TransactionsItemDecoration(spacing))
        val adapter = TransactionRecyclerAdapter()
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
                viewModel.transactionsState.collect { state ->
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
                    adapter.transactionsList = state.transactions
                }
            }
        }
        binding.swipeRefreshLayout.setOnRefreshListener { viewModel.getTransactions() }
        viewModel.checkUserStatus()
        return binding.root
    }

    override fun onResume() {
        viewModel.checkUserStatus()
        super.onResume()
    }

    companion object {
        fun newInstance(): TransactionsFragment = TransactionsFragment()
    }
}