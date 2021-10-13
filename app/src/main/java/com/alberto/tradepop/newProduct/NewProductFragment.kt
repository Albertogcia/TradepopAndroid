package com.alberto.tradepop.newProduct

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.alberto.tradepop.Extensions.DialogUtils
import com.alberto.tradepop.databinding.FragmentNewProductBinding
import com.alberto.tradepop.profile.ProfileViewModel
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.default
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.x.di
import org.kodein.di.direct
import org.kodein.di.instance
import com.alberto.tradepop.Extensions.FileUtils
import com.alberto.tradepop.Extensions.hideKeyboard
import com.alberto.tradepop.HomeActivity
import com.alberto.tradepop.R
import id.zelory.compressor.constraint.resolution
import kotlinx.coroutines.withContext


class NewProductFragment : Fragment(), DIAware {

    override val di: DI by di()
    private val viewModel: NewProductViewModel by lazy {
        ViewModelProvider(
            this,
            direct.instance()
        ).get(NewProductViewModel::class.java)
    }

    private lateinit var binding: FragmentNewProductBinding

    private var loadingDialog: AlertDialog? = null
    private var resultLauncher: ActivityResultLauncher<Intent>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.binding = FragmentNewProductBinding.inflate(inflater)
        lifecycleScope.launchWhenStarted {
            launch {
                viewModel.userState.collect { state ->
                    if (state.user != null) {
                        binding.noUserFragment.isVisible = false
                        binding.mainScroll.visibility = View.VISIBLE
                    } else {
                        binding.mainScroll.visibility = View.GONE
                        binding.noUserFragment.isVisible = true
                    }
                }
            }

            launch {
                viewModel.state.collect { state ->
                    if (state.showMessage) {
                        state.messageData?.let {
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
                    if (state.reloadData) {
                        loadingDialog?.dismiss()
                        clearView()
                        viewModel.resetViewModel()
                        val home = activity as? HomeActivity
                        home?.let {
                            it.newProductCreated()
                        }
                    }
                }
            }
        }
        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                it.data?.data?.let { uri ->
                    handleImage(uri)
                }
            }
        }
        binding.coverImage.setOnClickListener { openGallerySelector() }
        binding.selectCategoryCard.setOnClickListener { openCategorySelector() }
        binding.uploadButton.setOnClickListener { checkFields() }
        viewModel.checkUserStatus()
        return binding.root
    }

    private fun clearView(){
        binding.titleEditText.setText("")
        binding.descriptionEditText.setText("")
        binding.priceEditText.setText("")
        binding.selectedCategoryTextView.setText(R.string.new_product_description_label)
        binding.coverImage.setImageResource(R.color.secondary_color)
        binding.cameraImage.isVisible = true
    }

    private fun openCategorySelector() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(resources.getString(R.string.new_product_select_category_label))
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_category_selector, null)
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
        resultLauncher?.launch(intent)
    }

    private fun handleImage(uri: Uri){
        val file = FileUtils().getFile(requireContext(), uri)
        CoroutineScope(Dispatchers.IO).launch {
            val compressedImageFile = Compressor.compress(requireContext(), file) {
                resolution(1280, 1280)
            }
            withContext(Dispatchers.Main) {
                this@NewProductFragment.binding.coverImage.setImageURI(
                    compressedImageFile.toUri()
                )
                this@NewProductFragment.binding.cameraImage.isVisible = false
                this@NewProductFragment.viewModel.coverImageFile = compressedImageFile
            }
        }
    }

    private fun checkFields() {
        this.hideKeyboard()
        loadingDialog = DialogUtils().showLoadingDialog(requireContext())
        viewModel.checkFields(binding.titleEditText.text.toString(),binding.descriptionEditText.text.toString(),binding.priceEditText.text.toString())
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkUserStatus()
    }
}