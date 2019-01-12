package com.example.eddy.basetrackerpsyegb.utils

import android.content.Context
import android.content.Intent
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.eddy.basetrackerpsyegb.DB.RunMetrics
import com.example.eddy.basetrackerpsyegb.DB.deleteRun
import com.example.eddy.basetrackerpsyegb.DB.getRuns
import com.example.eddy.basetrackerpsyegb.R
import com.example.eddy.basetrackerpsyegb.activities.RunOverviewActivity
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.text.SimpleDateFormat
import java.util.*


class AllRunsAdapter(
    var runList: MutableList<RunMetrics>,
    val context: Context
) : RecyclerView.Adapter<AllRunsAdapter.ViewHolder>() {

    init {
        initList()
    }

    private fun initList() {
        runList.sortedByDescending { it.id }
        runList.reverse()
        notifyDataSetChanged()
    }


    override fun onBindViewHolder(holder: ViewHolder, pos: Int) {

        holder?.listId.text = runList.get(pos).id.toString()
        holder?.runDistance.text = runList.get(pos).totalDistance.toString()
        holder?.startDate.text = getDate(runList.get(pos).startTime)
        var startTime = runList.get(pos).startTime
        var endTime = runList.get(pos).endTime
        holder?.runDuration.text = RunUtils.Companion.getDuration(runList[pos].totalTime)
//            holder?.rootView.setOnClickListener { delete(runList[pos].id) }


        Log.v("onBindView", "date width :: ${holder.startDate.width}")
    }


    private fun delete(id: Int) {
        doAsync {
            context.contentResolver.deleteRun(id.toLong())
            runList = context.contentResolver.getRuns().toMutableList()
            uiThread {
                initList()
                notifyDataSetChanged()
            }

        }
    }


    private fun startMapViewActivity(id: Int) {
        Log.e("startMapViewActivity", "hello ID $id")
        val intent = Intent(context, RunOverviewActivity::class.java)
        intent.putExtra(RunMetrics.ID, id)
        context.startActivity(intent)
    }

//


    private fun getDate(time: Long): String? {
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        val date = Date(time)
        return format.format(date)
    }

    override fun getItemCount(): Int {
        return runList.size
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val v = LayoutInflater.from(p0?.context).inflate(R.layout.runs_list_element, p0, false)
        return ViewHolder(v)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val listId = itemView.findViewById<TextView>(R.id.runLIstID)
        val runDistance = itemView.findViewById<TextView>(R.id.runListLength)
        val runDuration = itemView.findViewById<TextView>(R.id.runListDuration)
        val startDate = itemView.findViewById<TextView>(R.id.runListStartDate)
        val rootView = itemView.findViewById<CardView>(R.id.rootCard)

    }

}