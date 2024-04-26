package com.mobilecomputing.library.ui

import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mobilecomputing.library.FirebaseManager
import com.mobilecomputing.library.Room
import com.mobilecomputing.library.RoomList
import com.mobilecomputing.library.databinding.ActionBarBinding
import com.mobilecomputing.library.databinding.FragmentUsageBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Timer
import java.util.TimerTask

class MainViewModel : ViewModel(){

    private var actionBarBinding: ActionBarBinding? = null

    private val _filteredRoomsLiveData = MutableLiveData<List<Room>>()
    val filteredRoomsLiveData: LiveData<List<Room>>
        get() = _filteredRoomsLiveData

    private val reservedSeatNumbersLiveData = MutableLiveData<List<Int>>()
    //val reservedSeatNumbersLiveData: LiveData<List<Room>>


    private var allRooms = RoomList.rooms

    private var displayName = MutableLiveData("Uninitialized")
    private var email = MutableLiveData("Uninitialized")
    private var uid = MutableLiveData("Uninitialized")

    init {
        // Initialize with all rooms
        _filteredRoomsLiveData.value = allRooms
        displayName.postValue("No user")
        email.postValue("No email, no active user")
        uid.postValue("No uid, no active user")
    }

    fun filterRoomsByBuilding(building: String) {
        val filteredRooms = if (building == "All Buildings") {
            allRooms
        } else {
            allRooms.filter { it.building == building }
        }
        _filteredRoomsLiveData.value = filteredRooms
    }

    fun observeFilteredRooms(): LiveData<List<Room>>{
        return filteredRoomsLiveData
    }

    fun initActionBarBinding(it: ActionBarBinding) {
        this.actionBarBinding = it
    }
    fun hideActionBar() {
        actionBarBinding?.actionUsage?.visibility = View.GONE
    }

    fun showActionBar() {
        actionBarBinding?.actionUsage?.visibility = View.VISIBLE
        actionBarBinding?.actionTitle?.text = "University of Texas at Austin"
    }



    private fun userLogout() {
        displayName.postValue("No user")
        email.postValue("No email, no active user")
        uid.postValue("No uid, no active user")
        stopTimer()
    }

    fun updateUser() {
        // XXX Write me. Update user data in view model
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            displayName.postValue(currentUser.displayName ?: "No display name")
            email.postValue(currentUser.email ?: "No email")
            uid.postValue(currentUser.uid)
        } else {
            userLogout()
        }
    }

    fun observeEmail() : LiveData<String> {
        return email
    }

    fun signOut() {
        seatusage.value = false
        userLogout()
        FirebaseAuth.getInstance().signOut()
    }

    private var usingRoom = MutableLiveData<Room>()
    private var usingSeat = MutableLiveData<Int>()
    private var seatusage = MutableLiveData<Boolean>()
    private var remainingTime = MutableLiveData<Long>()

    private var timer = Timer()
    private var timerTask: TimerTask? = null

    fun stopTimer(){
        timerTask?.cancel()
        timer?.cancel()
    }

    fun setSeat(room: Room, seatNum: Int){
        usingRoom.value = room
        usingSeat.value = seatNum
    }

    fun observeUsingRoom():LiveData<Room>{
        return usingRoom
    }

    fun observeUsingSeat():LiveData<Int>{
        return usingSeat
    }

    fun startUse(){
        seatusage.value = true
    }

    fun endUse(){
        seatusage.value = false
        timer.cancel()
    }

    fun getseatusage(): Boolean{
        return seatusage.value ?: false
    }

    fun setRemainingTime(time:Long){
        remainingTime.postValue(time)
    }

    fun observeRemainingTime():LiveData<Long>{
        return remainingTime
    }

    fun startTimer(durationMillis: Long) {
        stopTimer()
        timer = Timer()
        val startTimeMillis = System.currentTimeMillis()
        val timerTask = object : TimerTask() {
            override fun run() {
                // Code to be executed when the timer task runs
                val remainingTime = calculateRemainingTime(startTimeMillis, durationMillis)
                setRemainingTime(remainingTime)
                if (remainingTime <= 0) {
                    timer.cancel()
                }
            }
        }
        timer.scheduleAtFixedRate(timerTask, 0, 1000)
    }

    private fun calculateRemainingTime(startTimeMillis:Long, durationMillis: Long): Long {
        val currentTimeMillis = System.currentTimeMillis()
        return durationMillis - (currentTimeMillis - startTimeMillis)
    }

    fun formatMillisToMinutesSeconds(millis: Long): String {
        val totalSeconds = millis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    fun deleteSeatWhenReturn(userEmail: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("library")
            .whereEqualTo("userEmail", userEmail)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val firebasemanager = FirebaseManager()
                    firebasemanager.deleteReservationFromFirestore(document.id)
                }
            }
            .addOnFailureListener { e ->
                // Handle failure to retrieve seat information
                Log.e("MainViewModel", "Error retrieving reserved seat", e)
            }
    }

    fun retrieveReservedSeatForUser(userEmail: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("library")
            .whereEqualTo("userEmail", userEmail)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    // Retrieve seat information from the document
                    val building = document.getString("building")
                    val room = document.getString("room")
                    val seatNumber = document.getLong("seatNumber")?.toInt()

                    // Update ViewModel with the reserved seat information
                    setReservedSeat(building, room, seatNumber)
                }
            }
            .addOnFailureListener { e ->
                // Handle failure to retrieve seat information
                Log.e("MainViewModel", "Error retrieving reserved seat", e)
            }
    }
    fun setReservedSeat(building: String?, room: String?, seatNumber: Int?) {
        // Update ViewModel with reserved seat information
        if (building != null && room != null && seatNumber != null) {
            // Set the reserved seat information in the ViewModel
            usingRoom.value = Room(building, 0,room!!,0,false,false, 0)
            usingSeat.value = seatNumber!!
            seatusage.value = true // Assuming seat is in use
        }
    }


    fun getReservedSeatsForRoom(roomName: String) {
        viewModelScope.launch {
            try {
                val reservedSeats = mutableListOf<Int>()
                val querySnapshot = FirebaseFirestore.getInstance()
                    .collection("library")
                    .whereEqualTo("room", roomName)
                    .get()
                    .await()

                for (document in querySnapshot.documents) {
                    val seatNumber = document.getLong("seatNumber")?.toInt()
                    seatNumber?.let { reservedSeats.add(it) }
                }

                // Update LiveData with reserved seat numbers
                reservedSeatNumbersLiveData.postValue(reservedSeats)
                Log.d("TAG", reservedSeats.toString())
            } catch (e: Exception) {
                Log.e("TAG", "Error retrieving reserved seats for room $roomName", e)
            }
        }
    }

    fun observeSeatNumbers():LiveData<List<Int>>{
        return reservedSeatNumbersLiveData
    }


}