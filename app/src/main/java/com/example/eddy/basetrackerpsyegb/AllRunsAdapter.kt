package com.example.eddy.basetrackerpsyegb

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.eddy.basetrackerpsyegb.DB.RunMetrics
import com.example.eddy.basetrackerpsyegb.map.PolyDecodeDemoActivity
import org.jetbrains.anko.find
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.xml.datatype.DatatypeConstants.HOURS




class AllRunsAdapter(
    val runList: ArrayList<RunMetrics>,
    val context: Context
) : RecyclerView.Adapter<AllRunsAdapter.ViewHolder>(){
    val HEADER = 0
    init{
        runList.add(RunMetrics())

        runList.sortByDescending { it.id }
        runList.reverse()
    }




    override fun onBindViewHolder(holder: ViewHolder, pos: Int ) {
        if(pos != HEADER){
            holder?.listId.text = runList.get(pos).id.toString()
            holder?.runDistance.text = runList.get(pos).totalDistance.toString()
            holder?.startDate.text = getDate(runList.get(pos).startTime)
            var startTime = runList.get(pos).startTime
            var endTime = runList.get(pos).endTime
            holder?.runDuration.text = calculateDuration(startTime, endTime)
            holder?.rootView.setOnClickListener { startMapViewActivity(runList.get(pos).id) }
        }else{

//            val icon : Drawable? = context.getDrawable(android.R.drawable.arrow_down_float)
//            icon?.setBounds(0,0,((icon.intrinsicWidth * .5).toInt()) , ((icon.intrinsicHeight * .5).toInt()))

//            holder?.listId.setCompoundDrawablesWithIntrinsicBounds(icon,icon,icon,icon)
//            holder?.runDistance.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.arrow_down_float, 0, 0, 0)
//            holder?.startDate.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.arrow_down_float, 0, 0, 0)
//            holder?.runDuration.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.arrow_down_float, 0, 0, 0)


//            holder?.listId?.setOnClickListener{sortRunsByID()}
//            holder?.runDistance?.setOnClickListener{sortRunsByDistance()}
//            holder?.startDate?.setOnClickListener{sortRunsByDate()}
//            holder?.runDuration?.setOnClickListener{sortRunsByDuration()}
            holder?.rootView.setBackgroundColor(Color.GREEN)

        }


        Log.v("onBindView" , "date width :: ${holder.startDate.width}" )
    }


    private fun startMapViewActivity(id: Int){
        Log.e("startMapViewActivity", "hello ID $id")
        val intent = Intent(context, PolyDecodeDemoActivity::class.java)
        intent.putExtra(RunMetrics.ID, id)
        context.startActivity(intent)
    }
    private fun calculateDuration(startTime: Long, endTime: Long): String {

        var durMillis = (endTime - startTime)

        val hms = String.format(
            "%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(durMillis),
            TimeUnit.MILLISECONDS.toMinutes(durMillis) % TimeUnit.HOURS.toMinutes(1),
            TimeUnit.MILLISECONDS.toSeconds(durMillis) % TimeUnit.MINUTES.toSeconds(1)
        )

        return hms
    }


    private fun compareDur(startTime: Long, endTime: Long): Long{
        return endTime - startTime
    }
    private fun getDate(time: Long): String? {
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        val date = Date(time)
        return format.format(date)
    }


    override fun getItemCount(): Int {
        return runList.size
    }


    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): AllRunsAdapter.ViewHolder {
        val v = LayoutInflater.from(p0?.context).inflate(R.layout.runs_list_element, p0, false)
        return ViewHolder(v)
    }




    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val listId = itemView.findViewById<TextView>(R.id.runLIstID)
        val runDistance = itemView.findViewById<TextView>(R.id.runListLength)
        val runDuration = itemView.findViewById<TextView>(R.id.runListDuration)
        val startDate = itemView.findViewById<TextView>(R.id.runListStartDate)
        val rootView = itemView.findViewById<CardView>(R.id.rootCard)

    }


    fun sortRuns(){
        this.runList.sortByDescending { it.startTime }
        notifyDataSetChanged()
    }

    fun sortRunsByID(){
        this.runList.sortByDescending { it.id }
        notifyDataSetChanged()
    }

    fun sortRunsByDate(){
        this.runList.sortByDescending { it.startTime }
        notifyDataSetChanged()
    }

    fun sortRunsByDistance(){
        this.runList.sortByDescending { it.totalDistance }
        notifyDataSetChanged()
    }


    fun sortRunsByDuration(){
        this.runList.sortByDescending { calculateDuration(it.startTime,it.endTime)}
        notifyDataSetChanged()
    }




}