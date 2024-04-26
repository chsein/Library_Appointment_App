package com.mobilecomputing.library.ui

import android.R
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobilecomputing.library.RoomList
import com.mobilecomputing.library.databinding.FragmentMainBinding

class MainFragment: Fragment() {

    private val viewModel: MainViewModel by activityViewModels()
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var postRowAdapter: PostRowAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun setupSpinner() {
        val buildingNames = RoomList.rooms.map { it.building }.distinct()
        val allBuildingsList = listOf("All Buildings") + buildingNames
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, allBuildingsList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.building.adapter = adapter

    }

    private fun setupRecyclerView() {
        postRowAdapter = PostRowAdapter(viewModel) {
            val action =
                MainFragmentDirections.actionMainFragmentToGridFragment(it)
            findNavController().navigate(action)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = postRowAdapter
        viewModel.observeFilteredRooms().observe(viewLifecycleOwner){
            postRowAdapter.submitList(it)
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.showActionBar()
        setupSpinner()
        setupRecyclerView()

        binding.building.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedBuilding = parent?.getItemAtPosition(position).toString()
                viewModel.filterRoomsByBuilding(selectedBuilding)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
}