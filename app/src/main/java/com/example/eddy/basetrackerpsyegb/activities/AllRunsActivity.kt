package com.example.eddy.basetrackerpsyegb.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.widget.LinearLayout
import com.example.eddy.basetrackerpsyegb.AllRunsAdapter
import com.example.eddy.basetrackerpsyegb.DB.getAllRuns
import com.example.eddy.basetrackerpsyegb.R
import kotlinx.android.synthetic.main.activity_all_runs.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class AllRunsActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_runs)

        allRunsRecylerView?.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)

        fillRecylerView()



    }

    private fun fillRecylerView() {
        doAsync {
            val runs = contentResolver.getAllRuns()

            uiThread{
                allRunsRecylerView.adapter =
                        AllRunsAdapter(runs, this@AllRunsActivity)

            }

        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, MainActivity::class.java))

    }
}
