package com.mobilecomputing.library

import android.util.Log
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.mobilecomputing.library.ui.MainViewModel
import java.util.Calendar
import java.util.Timer
import java.util.TimerTask

class FirebaseManager {

    private val db = FirebaseFirestore.getInstance()

    fun addSeat(
        building: String,
        floor: Int,
        room: String,
        seatNumber: Int,
        durationMinutes: Int,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit,
        //userId: String
    ){val currentUser = FirebaseAuth.getInstance().currentUser
        val userEmail = currentUser?.email
        val reservationData = hashMapOf(
            "building" to building,
            "floor" to floor,
            "room" to room,
            "seatNumber" to seatNumber,
            "durationMinutes" to durationMinutes,
            "startTime" to FieldValue.serverTimestamp(),
            "userEmail" to userEmail
        )

        db.collection("library")
            .add(reservationData)
            .addOnSuccessListener { documentReference ->
                onSuccess.invoke(documentReference.id)
                startTimerForSeat(documentReference.id, (durationMinutes* 60 * 1000).toLong())
            }
            .addOnFailureListener { e ->
                onFailure.invoke(e)
            }

    }

    private fun startTimerForSeat(reservationId: String, durationMillis: Long) {
        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                deleteReservationFromFirestore(reservationId)
            }
        }, durationMillis)
    }

    fun deleteReservationFromFirestore(reservationId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("library")
            .document(reservationId)
            .delete()
            .addOnSuccessListener {
                Log.d("Reservation", "Reservation deleted successfully")
            }
            .addOnFailureListener { e ->
                Log.d("Reservation", "Error deleting reservation", e)
            }

    }
}