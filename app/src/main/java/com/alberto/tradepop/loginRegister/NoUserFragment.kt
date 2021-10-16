package com.alberto.tradepop.loginRegister

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alberto.tradepop.databinding.FragmentNoUserBinding

class NoUserFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = FragmentNoUserBinding.inflate(inflater, container, false).apply {
        this.toLoginButton.setOnClickListener {
            activity?.let{
                val intent = Intent (it, LoginRegisterActivity::class.java)
                it.startActivity(intent)
            }
        }
    }.root

}