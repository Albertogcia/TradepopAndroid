package com.alberto.tradepop

import android.os.Bundle
import android.util.Log
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.alberto.tradepop.databinding.ActivityHomeBinding
import com.alberto.tradepop.favorites.FavoritesFragment
import com.alberto.tradepop.newProduct.NewProductFragment
import com.alberto.tradepop.products.ProductsFragment
import com.alberto.tradepop.profile.ProfileFragment

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        binding.navView.setOnItemSelectedListener { menuItem ->
            when(menuItem.itemId){
                R.id.navigation_products -> {
                    val productsFragment = ProductsFragment.newInstance()
                    setFragment(productsFragment)
                }
                R.id.navigation_add_product -> {
                    val newProductFragment = NewProductFragment.newInstance()
                    setFragment(newProductFragment)
                }
                R.id.navigation_profile -> {
                    val profileFragment = ProfileFragment.newInstance()
                    setFragment(profileFragment)
                }
                R.id.navigation_favorites -> {
                    val favoritesFragment = FavoritesFragment.newInstance()
                    setFragment(favoritesFragment)
                }
            }
            true
        }

        navView.selectedItemId = R.id.navigation_products
    }

    fun newProductCreated(){
        binding.navView.selectedItemId = R.id.navigation_products
    }

    private fun setFragment(fragment: Fragment){
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.nav_host_fragment_activity_home, fragment)
            .addToBackStack(null)
            .commit()
    }
}