package com.kyrgyzcoder.myfitness.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kyrgyzcoder.myfitness.DATE_FORMAT
import com.kyrgyzcoder.myfitness.R
import com.kyrgyzcoder.myfitness.model.Runs
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class HistoryRecyclerViewAdapter :
    ListAdapter<Runs, HistoryRecyclerViewAdapter.MyViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Runs>() {
            override fun areItemsTheSame(oldItem: Runs, newItem: Runs): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Runs, newItem: Runs): Boolean {
                return oldItem.locEndLat == newItem.locEndLat &&
                        oldItem.locEndLon == newItem.locEndLon &&
                        oldItem.locStartLat == newItem.locStartLat &&
                        oldItem.locStartLon == newItem.locStartLon &&
                        oldItem.runDate == newItem.runDate
            }
        }
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val runDate: TextView = itemView.findViewById(R.id.textViewListTime)
        val startPoint: TextView = itemView.findViewById(R.id.textViewListStartingPoint)
        val endPoint: TextView = itemView.findViewById(R.id.textViewListEndPoint)
        val distance: TextView = itemView.findViewById(R.id.textViewListDistance)
        val timeSpent: TextView = itemView.findViewById(R.id.textViewListTimeSpent)
        val avrgSpeed: TextView = itemView.findViewById(R.id.textViewListAvrgSpeed)
    }

    fun getItemAt(position: Int): Runs {
        return getItem(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.list_runs, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val dateFormat: DateFormat = SimpleDateFormat(DATE_FORMAT, Locale.ROOT)
        val currentRun = getItemAt(position)

        holder.runDate.text = dateFormat.format(currentRun.runDate)
        var str = "[ ${currentRun.locStartLat} , ${currentRun.locStartLon} ]"
        holder.startPoint.text = str
        str = "[ ${currentRun.locEndLat} , ${currentRun.locEndLon} ]"
        holder.endPoint.text = str
        str = currentRun.runDistanceMeters.toString() + " meters"
        holder.distance.text = str
        str = "${currentRun.runTimeSecs / 60} minutes & ${currentRun.runTimeSecs % 60} seconds"
        holder.timeSpent.text = str
        val speed = currentRun.runDistanceMeters / currentRun.runTimeSecs
        str = "$speed m/s"
        holder.avrgSpeed.text = str
    }
}