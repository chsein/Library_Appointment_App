package com.mobilecomputing.library.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mobilecomputing.library.Room
import com.mobilecomputing.library.databinding.RowPostBinding

class PostRowAdapter (private val viewModel: MainViewModel,
                      private val navigateToFragment: (Room)->Unit )
    : ListAdapter<Room, PostRowAdapter.VH>(Diff()) {

    inner class VH(val rowPostBinding: RowPostBinding)
        : RecyclerView.ViewHolder(rowPostBinding.root) {
        init {
            rowPostBinding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val room = getItem(position)
                    navigateToFragment(room)
                }
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val rowpostbinding = RowPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(rowpostbinding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val rowpostbinding = holder.rowPostBinding
        val item = getItem(position)

        rowpostbinding.floor.text ="Floor ${item.floor} "
        rowpostbinding.room.text = item.name
        if(item.noise) rowpostbinding.mute.visibility = View.VISIBLE
        else rowpostbinding.mute.visibility = View.GONE
        if(item.computers) rowpostbinding.computer.visibility = View.VISIBLE
        else rowpostbinding.computer.visibility = View.GONE

    }

    class Diff : DiffUtil.ItemCallback<Room>() {
        override fun areItemsTheSame(oldItem: Room, newItem: Room): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
        override fun areContentsTheSame(oldItem: Room, newItem: Room): Boolean {
            return oldItem.building == newItem.building
                    && oldItem.floor == newItem.floor
                    && oldItem.name == newItem.name
                    && oldItem.seats == newItem.seats
                    && oldItem.noise == newItem.noise
                    && oldItem.computers == newItem.computers
        }
    }


}