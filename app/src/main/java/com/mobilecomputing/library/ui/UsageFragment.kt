package com.mobilecomputing.library.ui

import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.mobilecomputing.library.AuthInit
import com.mobilecomputing.library.databinding.FragmentUsageBinding
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.mobilecomputing.library.FirebaseManager
import com.mobilecomputing.library.R
import com.mobilecomputing.library.databinding.ActionBarBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale
import java.util.jar.Manifest

//, OnMapReadyCallback
class UsageFragment  : Fragment(), OnMapReadyCallback{

    private var _binding: FragmentUsageBinding? = null
    private var actionBarBinding: ActionBarBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    private val signInLauncher =
        registerForActivityResult(FirebaseAuthUIActivityResultContract()) {
            viewModel.updateUser()
        }

    private lateinit var map: GoogleMap
    private lateinit var geocoder: Geocoder
    var locationName = "University of Texas at Austin"
    private var mapInitialized = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUsageBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun resetTextFields() {
        binding.textBuilding.text = "No Building Selected"
        binding.textRoom.text = "No Room Selected"
        binding.textSeatNumber.text = "No Seat Selected"
        binding.textRemainingTime.text = "Time Left: 00:00"
        locationName = "University of Texas at Austin"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.hideActionBar()
        actionBarBinding?.actionTitle?.text = "Usage"

        // Retrieve the SupportMapFragment
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFrag) as SupportMapFragment
        mapFragment.getMapAsync(this)
        geocoder = Geocoder(requireContext(), Locale.getDefault())
        resetTextFields()
        var useremail:String = ""

        viewModel.observeEmail().observe(viewLifecycleOwner){
            useremail = it
            binding.textUser.text = "User: $it"
            viewModel.retrieveReservedSeatForUser(it)
        }
        binding.returnseat.setOnClickListener{
            viewModel.endUse()
            resetTextFields()
            viewModel.deleteSeatWhenReturn(useremail)
        }
        binding.login.setOnClickListener{
            viewModel.updateUser()
            AuthInit(viewModel, signInLauncher)
        }
        binding.logout.setOnClickListener{
            viewModel.signOut()
            AuthInit(viewModel, signInLauncher)
        }

        viewModel.observeUsingRoom().observe(viewLifecycleOwner){
            if(viewModel.getseatusage()){
                binding.textBuilding.text = it.building
                locationName = it.building
                binding.textRoom.text = it.name
            }
        }

        viewModel.observeUsingSeat().observe(viewLifecycleOwner){
            if(viewModel.getseatusage()){
                binding.textSeatNumber.text = it.toString()
            }
        }
        viewModel.observeRemainingTime().observe(viewLifecycleOwner){

            if(viewModel.getseatusage()){
                val remainingTimeText = viewModel.formatMillisToMinutesSeconds(it)
                binding.textRemainingTime.text = "Time Left: $remainingTimeText"
            }
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        val PCL = LatLng(30.283204, -97.73810)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(PCL, 16f))
        if(viewModel.getseatusage()){
            addBuildingMarker(binding.textBuilding.text.toString())
        }
    }

    private fun addBuildingMarker(buildingName: String) {
        try {
            val addresses = geocoder.getFromLocationName(buildingName, 1)

            if (addresses!!.isNotEmpty()) {
                val address = addresses[0]
                val latLng = LatLng(address.latitude, address.longitude)
                map.clear() // Clear existing markers
                map.addMarker(MarkerOptions().position(latLng).title(buildingName))
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
            } else {
                Toast.makeText(requireContext(), "Building not found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            Toast.makeText(requireContext(), "Geocoding error", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }


}