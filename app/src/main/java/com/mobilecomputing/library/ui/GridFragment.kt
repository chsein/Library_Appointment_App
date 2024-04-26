package com.mobilecomputing.library.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.mobilecomputing.library.R
import com.mobilecomputing.library.databinding.FragmentGridBinding
import java.lang.Integer.min

class GridFragment :Fragment(){

    private val viewModel: MainViewModel by activityViewModels()
    private var _binding: FragmentGridBinding? = null
    private val binding get() = _binding!!
    private val args: GridFragmentArgs by navArgs()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGridBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.hideActionBar()
        val room = args.room
        binding.building.text = room.building
        binding.room.text = room.name
        if(room.image != 0) {
            binding.IV.setImageResource(room.image)
        }else{
            binding.IV.visibility = View.GONE
        }

        val seats = room.seats
        val columnCount = 4
        val gridLayout = binding.gridLayout
        gridLayout.removeAllViews()

        val params = GridLayout.LayoutParams().apply {
            width = GridLayout.LayoutParams.WRAP_CONTENT
            height = GridLayout.LayoutParams.WRAP_CONTENT
            setMargins(16, 16, 16, 16) // Set margins between buttons
        }

        // Calculate number of rows needed based on total seats and column count
        val numColumns = min(seats, columnCount)
        val numRows = (seats + numColumns - 1) / numColumns

        viewModel.getReservedSeatsForRoom(room.name)
        viewModel.observeSeatNumbers().observe(viewLifecycleOwner){reservedSeats->

            for (seat in 1..seats.coerceAtMost(numColumns * numRows)) {
                val button = Button(requireContext())
                button.text = seat.toString()
                button.layoutParams = params


                val row = (seat - 1) / numColumns
                val column = (seat - 1) % numColumns
                // Set GridLayout layout parameters for the button
                val buttonParams = GridLayout.LayoutParams(params).apply {
                    rowSpec = GridLayout.spec(row, 1f) // Set row weight to 1 for even distribution
                    columnSpec = GridLayout.spec(column, 1f) // Set column weight to 1 for even distribution
                }
                val isReserved = reservedSeats.contains(seat)
                button.isEnabled = !isReserved
                if(!isReserved){
                    button.setOnClickListener {
                        // Handle seat selection logic here
                        if(!viewModel.getseatusage()){
                            val selectedSeatNumber = button.text.toString().toInt()
                            val argsseat = Bundle().apply {
                                putSerializable(TimePickerDialogFragment.ARG_ROOM, room)
                                putInt(TimePickerDialogFragment.ARG_SEAT_NUMBER, selectedSeatNumber)
                            }

                            val timePickerFragment = TimePickerDialogFragment()
                            timePickerFragment.arguments = argsseat
                            timePickerFragment.show(parentFragmentManager, "timePicker")
                        }
                        else{
                            Toast.makeText(requireContext(), "Return your Seat", Toast.LENGTH_LONG).show()

                        }
                    }

                }

                gridLayout.addView(button, buttonParams)
            }
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clean up binding reference
    }

}