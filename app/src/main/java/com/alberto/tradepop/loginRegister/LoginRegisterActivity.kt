package com.alberto.tradepop.loginRegister

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.alberto.tradepop.Extensions.showEditTextDialog
import com.alberto.tradepop.Extensions.showLoadingDialog
import com.alberto.tradepop.Extensions.showSimpleMessage
import com.alberto.tradepop.R
import com.alberto.tradepop.databinding.ActivityLoginRegisterBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.kodein.di.android.di
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.direct
import org.kodein.di.instance

class LoginRegisterActivity : AppCompatActivity(), DIAware {

    override val di: DI by di()
    private val viewModel: LoginRegisterViewModel by lazy {
        ViewModelProvider(
            this,
            direct.instance()
        ).get(LoginRegisterViewModel::class.java)
    }
    private lateinit var binding: ActivityLoginRegisterBinding

    private var loadingDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_register)

        this.binding = ActivityLoginRegisterBinding.inflate(layoutInflater).also {
            setContentView(it.root)
        }

        binding.loginButton.setOnClickListener { onLoginButtonTapped() }
        binding.registerButton.setOnClickListener { onRegisterButtonTapped() }

        binding.loginSelectorLayout.setOnClickListener { logInSelectorTapped() }
        binding.registerSelectorLayout.setOnClickListener { signInSelectorTapped() }

        binding.loginShowPasswordButton.setOnClickListener { showLoginPasswordTapped() }
        binding.registerShowPasswordButton.setOnClickListener { showRegisterPasswordTapped() }

        binding.forgotPasswordTextView.setOnClickListener { this.showEditTextDialog(this, this::sendResetEmail) }

        binding.closeButton.setOnClickListener { this.finish() }

        lifecycleScope.launchWhenStarted {
            launch {
                viewModel.state.collect { state ->
                    if(state.showMessage){
                        state.messageData?.let {
                            this@LoginRegisterActivity.showMessage(resources.getString(it.messageTitle), resources.getString(it.messageDescription))
                        }
                    }
                    if(state.dismissActivity){
                        this@LoginRegisterActivity.finish()
                    }
                }
            }
        }
    }

    private fun sendResetEmail(email: String){
        loadingDialog = this.showLoadingDialog(this)
        viewModel.sendResetPasswordEmail(email)
    }

    private fun logInSelectorTapped(){
        binding.registerLayout.visibility = View.GONE
        binding.registerSelectorTextView.alpha = 0.4f
        binding.registerSelectorUnderline.visibility = View.INVISIBLE
        //
        binding.loginLayout.visibility = View.VISIBLE
        binding.loginSelectorTextView.alpha = 1f
        binding.loginSelectorUnderline.visibility = View.VISIBLE
    }

    private fun signInSelectorTapped(){
        binding.loginLayout.visibility = View.GONE
        binding.loginSelectorTextView.alpha = 0.4f
        binding.loginSelectorUnderline.visibility = View.INVISIBLE
        //
        binding.registerLayout.visibility = View.VISIBLE
        binding.registerSelectorTextView.alpha = 1f
        binding.registerSelectorUnderline.visibility = View.VISIBLE
    }

    private fun onLoginButtonTapped() {
        loadingDialog = this.showLoadingDialog(this)
        viewModel.loginButtonTapped(binding.loginEmailEditText.text.toString(), binding.loginPasswordEditText.text.toString())
    }

    private fun onRegisterButtonTapped(){
        loadingDialog = this.showLoadingDialog(this)
        viewModel.registerButtonTapped(binding.registerUsernameEditText.text.toString(), binding.registerEmailEditText.text.toString(), binding.registerPasswordEditText.text.toString())
    }

    private fun showLoginPasswordTapped(){
        if(binding.loginPasswordEditText.transformationMethod == PasswordTransformationMethod.getInstance()){
            binding.loginShowPasswordButton.setImageResource(R.drawable.icon_open_eye)
            binding.loginPasswordEditText.transformationMethod = HideReturnsTransformationMethod.getInstance()
        }
        else{
            binding.loginShowPasswordButton.setImageResource(R.drawable.icon_close_eye)
            binding.loginPasswordEditText.transformationMethod = PasswordTransformationMethod.getInstance()
        }
    }

    private fun showRegisterPasswordTapped(){
        if(binding.registerPasswordEditText.transformationMethod == PasswordTransformationMethod.getInstance()){
            binding.registerShowPasswordButton.setImageResource(R.drawable.icon_open_eye)
            binding.registerPasswordEditText.transformationMethod = HideReturnsTransformationMethod.getInstance()
        }
        else{
            binding.registerShowPasswordButton.setImageResource(R.drawable.icon_close_eye)
            binding.registerPasswordEditText.transformationMethod = PasswordTransformationMethod.getInstance()
        }
    }

    private fun showMessage(title: String, description: String){
        this.loadingDialog?.dismiss()
        this@LoginRegisterActivity.showSimpleMessage(title, description, this@LoginRegisterActivity)
        viewModel.messageDisplayed()
    }
}