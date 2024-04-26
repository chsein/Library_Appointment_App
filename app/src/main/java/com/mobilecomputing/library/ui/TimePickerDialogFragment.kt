package com.mobilecomputing.library.ui

import android.app.AlertDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.firestore
import com.mobilecomputing.library.FirebaseManager
import com.mobilecomputing.library.R
import com.mobilecomputing.library.Room
import com.mobilecomputing.library.databinding.FragmentGridBinding
import com.mobilecomputing.library.databinding.TimeDialogBinding
import java.util.*

class TimePickerDialogFragment : DialogFragment(){

    companion object {
        const val ARG_FLOOR = "arg_floor"
        const val ARG_BUILDING = "arg_building"
        const val ARG_ROOM = "arg_room"
        const val ARG_SEAT_NUMBER = "arg_seat_number"
    }
    private val viewModel: MainViewModel by activityViewModels()
    private var _binding: TimeDialogBinding? = null
    private val binding get() = _binding!!
    private lateinit var seatButton: Button
    private var startTimeMillis: Long = 0

    private var selectedHours = 0
    private var selectedMinutes = 0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = arguments ?: throw IllegalArgumentException("Arguments not provided")
        val firebaseManager = FirebaseManager()


        val argsseat = args.getSerializable(ARG_ROOM) as? Room
        val seatNumber = args.getInt(ARG_SEAT_NUMBER, -1)

        val building = argsseat?.building
        val floor = argsseat?.floor
        val name = argsseat?.name

        val dialogView = requireActivity().layoutInflater.inflate(R.layout.time_dialog, null)
        val hoursPicker = dialogView.findViewById<NumberPicker>(R.id.hoursPicker)
        val minutesPicker = dialogView.findViewById<NumberPicker>(R.id.minutesPicker)

        hoursPicker.minValue = 0
        hoursPicker.maxValue = 3 // Maximum of 4 hours
        hoursPicker.value = selectedHours

        minutesPicker.minValue = 0
        minutesPicker.maxValue = 59 // Maximum of 59 minutes
        minutesPicker.value = selectedMinutes

        val builder = AlertDialog.Builder(requireContext())

        builder.setView(dialogView)
            .setTitle("Choose Reservation Duration")
            .setPositiveButton("OK") { _, _ ->
                selectedHours = hoursPicker.value
                selectedMinutes = minutesPicker.value
                val totalDurationMinutes = selectedHours * 60 + selectedMinutes
                val totalDurationMillis = totalDurationMinutes * 60 * 1000  // Convert to milliseconds

                viewModel.startTimer(totalDurationMillis.toLong())
                (requireActivity() as? TimePickerDialogListener)?.onDurationSelected(totalDurationMinutes)

                val formattedTime = String.format("%02d:%02d", selectedHours, selectedMinutes)
                val toastMessage = "$building $name seat $seatNumber selected for $formattedTime"

                Toast.makeText(requireContext(), toastMessage, Toast.LENGTH_LONG).show()

                viewModel.setSeat(argsseat!!, seatNumber)
                viewModel.startUse()

                firebaseManager.addSeat(
                    building!!,
                    floor!!,
                    name!!,
                    seatNumber,
                    totalDurationMinutes,
                    { reservationId ->
                        Log.d("Reservation", "Reservation added with ID: $reservationId")
                    },
                    { exception ->
                        Log.e("Reservation", "Error adding reservation", exception)

                    }
                )

            }
            .setNegativeButton("Cancel"){ _, _ ->
            }

        return builder.create()
    }



    interface TimePickerDialogListener {
        fun onDurationSelected(durationMinutes: Int)
    }
}