package com.mobilecomputing.library

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.mobilecomputing.library.databinding.ActivityMainBinding
import com.mobilecomputing.library.databinding.ActionBarBinding
import com.mobilecomputing.library.ui.MainFragmentDirections
import com.mobilecomputing.library.ui.MainViewModel
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract

class MainActivity : AppCompatActivity() {

    private var actionBarBinding: ActionBarBinding? = null
    private val viewModel: MainViewModel by viewModels()
    private lateinit var navController : NavController
    private lateinit var appBarConfiguration: AppBarConfiguration


    private fun initActionBar(actionBar: ActionBar) {
        // Disable the default and enable the custom
        actionBar.setDisplayShowTitleEnabled(false)
        actionBar.setDisplayShowCustomEnabled(true)
        actionBarBinding = ActionBarBinding.inflate(layoutInflater)
        // Apply the custom view
        actionBar.customView = actionBarBinding?.root
        viewModel.initActionBarBinding(actionBarBinding!!)
    }

    private val signInLauncher =
        registerForActivityResult(FirebaseAuthUIActivityResultContract()) {
            viewModel.updateUser()
        }

    private fun NavController.safeNavigate(direction: NavDirections) {
        currentDestination?.
        getAction(direction.actionId)?.
        run {
            navigate(direction)
        }
    }


    private fun actionBarLaunchUsage() {
        // XXX Write me actionBarBinding, safeNavigate

        actionBarBinding?.actionUsage?.setOnClickListener{
            navController.safeNavigate(MainFragmentDirections.actionMainFragmentToUsageFragment())
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)
        setSupportActionBar(activityMainBinding.toolbar)
        supportActionBar?.let{
            initActionBar(it)
        }

        actionBarLaunchUsage()

        navController = findNavController(R.id.main_frame)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        AuthInit(viewModel, signInLauncher)
        viewModel.updateUser()

    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.main_frame)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}