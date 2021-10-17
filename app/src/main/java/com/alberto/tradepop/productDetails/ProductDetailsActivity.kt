package com.alberto.tradepop.productDetails

import android.app.Activity
import android.content.Intent
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import coil.load
import com.alberto.tradepop.Extensions.*
import com.alberto.tradepop.databinding.ActivityProductDetailsBinding
import com.alberto.tradepop.network.models.Product
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.resolution
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.di
import org.kodein.di.direct
import org.kodein.di.instance

import com.alberto.tradepop.R


class ProductDetailsActivity : AppCompatActivity(), DIAware {

    override val di: DI by di()
    private val viewModel: ProductDetailsViewModel by lazy {
        ViewModelProvider(
            this,
            direct.instance()
        ).get(ProductDetailsViewModel::class.java)
    }

    private lateinit var binding: ActivityProductDetailsBinding
    private var loadingDialog: AlertDialog? = null
    private lateinit var favoriteMenuItem: MenuItem
    private lateinit var product: Product

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_details)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        this.binding = ActivityProductDetailsBinding.inflate(layoutInflater).also {
            setContentView(it.root)
        }
        (intent.getSerializableExtra("product") as? Product)?.let {
            this.product = it
        } ?: run {
            finish()
        }
        viewModel.setProduct(this.product)
        lifecycleScope.launchWhenStarted {
            launch {
                viewModel.userState.collect { state ->
                    if (state.user != null) {
                        if (state.user.uuid == product.owner) {
                            binding.deleteButton.isVisible = true
                            binding.editButton.isVisible = true
                            binding.coverImage.setOnClickListener { openGallerySelector() }
                            binding.selectCategoryCard.setOnClickListener { openCategorySelector() }
                        } else {
                            binding.buyButton.isVisible = true
                            binding.titleEditText.isEnabled = false
                            binding.descriptionEditText.isEnabled = false
                            binding.priceEditText.isEnabled = false
                        }
                    }
                }
            }
            launch {
                viewModel.productDetailsState.collect { state ->
                    if (state.showMessage) {
                        state.messageData?.let {
                            loadingDialog?.dismiss()
                            DialogUtils().showSimpleMessage(
                                resources.getString(it.messageTitle),
                                resources.getString(it.messageDescription),
                                resources.getString(R.string.generic_ok),
                                this@ProductDetailsActivity
                            )
                            viewModel.messageDisplayed()
                        }
                    }
                    if (state.errorFavorite){
                        Toast.makeText(this@ProductDetailsActivity, R.string.product_details_error_message, Toast.LENGTH_LONG).show()
                    }
                    if (state.finish) {
                        loadingDialog?.dismiss()
                        setResult(RESULT_OK)
                        finish()
                    }
                }
            }
        }
        binding.buyButton.setOnClickListener { buyProduct() }
        binding.editButton.setOnClickListener { editProduct() }
        binding.deleteButton.setOnClickListener { showDeleteAlert() }
        binding.titleEditText.setText(product.title)
        binding.descriptionEditText.setText(product.description)
        product.categoryId?.let {
            when (it) {
                1 -> {
                    binding.selectedCategoryTextView.text = resources.getString(R.string.category_1)
                }
                2 -> {
                    binding.selectedCategoryTextView.text = resources.getString(R.string.category_2)
                }
                3 -> {
                    binding.selectedCategoryTextView.text = resources.getString(R.string.category_3)
                }
                4 -> {
                    binding.selectedCategoryTextView.text = resources.getString(R.string.category_4)
                }
                5 -> {
                    binding.selectedCategoryTextView.text = resources.getString(R.string.category_5)
                }
            }
        }
        binding.priceEditText.setText(product.price.toString())
        binding.coverImage.load(product.coverImageUrl) {
            error(R.drawable.no_image_placeholder)
        }
        viewModel.checkUserState()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.product_details_action_bar_menu, menu)
        menu?.findItem(R.id.action_favorite)?.let {
            favoriteMenuItem = it
            if (viewModel.isProductFavorite(product.uuid)) {
                it.setIcon(R.drawable.icon_favorite_fill)
            } else {
                it.setIcon(R.drawable.icon_favorite)
            }
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_favorite -> {
            if (!viewModel.isProductFavorite(product.uuid)) {
                item.setIcon(R.drawable.icon_favorite_fill)
            } else {
                item.setIcon(R.drawable.icon_favorite)
            }
            viewModel.changeFavorite()
            true
        }

        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    private fun buyProduct() {
        loadingDialog = this.showLoadingDialog(this)
        viewModel.buyProduct()
    }

    private fun editProduct() {
        this.hideKeyboard()
        loadingDialog = this.showLoadingDialog(this)
        viewModel.checkFields(
            binding.titleEditText.text.toString(),
            binding.descriptionEditText.text.toString(),
            binding.priceEditText.text.toString()
        )
    }

    private fun showDeleteAlert() {
        val builder = AlertDialog.Builder(this@ProductDetailsActivity)
        builder.setTitle(resources.getString(R.string.generic_delete))
        builder.setMessage(resources.getString(R.string.product_details_delete_product_message))
        builder.setPositiveButton(resources.getString(R.string.generic_delete)) { _, _ ->
            deleteProduct()
        }
        builder.setNegativeButton(resources.getString(R.string.generic_cancel), null)
        builder.show()
    }

    private fun deleteProduct() {
        loadingDialog = this.showLoadingDialog(this)
        viewModel.deleteProduct()
    }

    private fun openCategorySelector() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(resources.getString(R.string.new_product_select_category_label))
        val dialogView =
            LayoutInflater.from(this).inflate(R.layout.dialog_category_selector, null)
        builder.setView(dialogView)
        val dialog = builder.show()
        dialogView.findViewById<CardView>(R.id.category1).setOnClickListener {
            categorySelected(
                1,
                resources.getString(R.string.category_1)
            ); dialog.dismiss()
        }
        dialogView.findViewById<CardView>(R.id.category2).setOnClickListener {
            categorySelected(
                2,
                resources.getString(R.string.category_2)
            ); dialog.dismiss()
        }
        dialogView.findViewById<CardView>(R.id.category3).setOnClickListener {
            categorySelected(
                3,
                resources.getString(R.string.category_3)
            ); dialog.dismiss()
        }
        dialogView.findViewById<CardView>(R.id.category4).setOnClickListener {
            categorySelected(
                4,
                resources.getString(R.string.category_4)
            ); dialog.dismiss()
        }
        dialogView.findViewById<CardView>(R.id.category5).setOnClickListener {
            categorySelected(
                5,
                resources.getString(R.string.category_5)
            ); dialog.dismiss()
        }
    }

    private fun categorySelected(categoryId: Int, categoryName: String) {
        binding.selectedCategoryTextView.text = categoryName
        viewModel.selectedCategoryId = categoryId
    }

    private fun openGallerySelector() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        resultLauncher.launch(intent)
    }

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                it.data?.data?.let { uri ->
                    handleImage(uri)
                }
            }
        }

    private fun handleImage(uri: Uri) {
        val file = FileUtils().getFile(this, uri)
        CoroutineScope(Dispatchers.IO).launch {
            val compressedImageFile = Compressor.compress(this@ProductDetailsActivity, file) {
                resolution(1280, 1280)
            }
            withContext(Dispatchers.Main) {
                this@ProductDetailsActivity.binding.coverImage.setImageURI(
                    compressedImageFile.toUri()
                )
                this@ProductDetailsActivity.viewModel.coverImageFile = compressedImageFile
            }
        }
    }

}