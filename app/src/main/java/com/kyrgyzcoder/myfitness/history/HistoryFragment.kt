package com.kyrgyzcoder.myfitness.history

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kyrgyzcoder.myfitness.R
import com.kyrgyzcoder.myfitness.adapters.HistoryRecyclerViewAdapter
import com.kyrgyzcoder.myfitness.run.RunViewModel

class HistoryFragment : Fragment() {

    companion object {
        fun newInstance() = HistoryFragment()
    }

    private lateinit var viewModel: RunViewModel
    private lateinit var recyclerViewHistory: RecyclerView
    private lateinit var adapterHist: HistoryRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        val view = inflater.inflate(R.layout.history_fragment, container, false)
        recyclerViewHistory = view.findViewById(R.id.history_recyclerView)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(RunViewModel::class.java)

        adapterHist = HistoryRecyclerViewAdapter()
        recyclerViewHistory.layoutManager = LinearLayoutManager(this.context)
        recyclerViewHistory.setHasFixedSize(true)
        recyclerViewHistory.adapter = adapterHist

        viewModel.getAllRuns().observe(viewLifecycleOwner, Observer {
            adapterHist.submitList(it)
        })

        onSwipeDelete()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.history_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete_all -> {
                deleteHistory()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Function that asks for confirmation and clears history if confirmed
     */
    private fun deleteHistory() {
        val dialogBuilder = AlertDialog.Builder(activity)

        dialogBuilder.setMessage(getString(R.string.alert_delete_message))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.alert_delele_delete)) { _, _ ->
                viewModel.deleteAllRuns()
                Toast.makeText(this.context, "History cleaned", Toast.LENGTH_SHORT).show()
            }
        dialogBuilder.setNegativeButton(getString(R.string.alert_delete_cancel)) { _, _ ->
            Toast.makeText(this.context, "Cancelled", Toast.LENGTH_SHORT).show()
        }
        val alert = dialogBuilder.create()
        alert.setTitle(getString(R.string.alert_delete_title))
        alert.show()
    }

    /**
     * Function that deletes item when swiped right or left
     */

    private fun onSwipeDelete() {
        ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT.or(ItemTouchHelper.RIGHT)) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                TODO("Not yet implemented")
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val dialogBuilder = AlertDialog.Builder(activity!!)
                dialogBuilder.setMessage(getString(R.string.alert_swipe_message))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.alert_delele_delete)) { _, _ ->
                        viewModel.deleteRun(adapterHist.getItemAt(viewHolder.adapterPosition))
                        Toast.makeText(activity!!.applicationContext, "Deleted", Toast.LENGTH_SHORT)
                            .show()
                    }
                dialogBuilder.setNegativeButton(getString(R.string.alert_delete_cancel)) { _, _ ->
                    Toast.makeText(activity!!.applicationContext, "Cancelled", Toast.LENGTH_SHORT)
                        .show()
                    adapterHist.notifyDataSetChanged()
                }
                val alert = dialogBuilder.create()
                alert.setTitle(getString(R.string.alert_delete_title))
                alert.show()
            }
        }).attachToRecyclerView(recyclerViewHistory)
    }

}
