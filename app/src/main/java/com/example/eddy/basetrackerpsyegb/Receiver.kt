package com.example.eddy.basetrackerpsyegb


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Message
import android.os.RemoteException
import android.util.Log

/*Created By Edward Butcher
* Simple broadcast layout in Kotlin
* Learning kotlin!!!! */
class Receiver : BroadcastReceiver() {
    val TAG = "Rceiver"


    override fun onReceive(context: Context, intent: Intent) {
        val bundle = intent.extras
        val command = intent.getStringExtra("COMMAND")
        Log.v("onReieve", "Intent : " + intent.action + "Command : " + command)

        try {
            if (command == null) {
                return
            }

            val msg: Message
            if (command.equals("LOG_DATA")) {

            }
//            when (command) {
//                "CPU_TEST" -> {
//                    msg = Message.obtain(null, ServiceConstants.MessageCodes.MODIFY_CPU_SERVICE, 0, 0)
//                    msg.data = bundle
//                    sendMessage(msg)
//                }
//
//            }


        } catch (e: Exception) {
            Log.v("onReceive", "Exception onReceive: $e")
        }

    }

    private fun sendMessage(msg: Message) {
//        if (ApplicationMain.isBoundToService) {
//            if (ApplicationMain.mServiceMessenger != null) {
//                try {
//                    ApplicationMain.mServiceMessenger.send(msg)
//                } catch (e: RemoteException) {
//                    e.printStackTrace()
//                }
//
//            } else {
//                Log.v("SendMessageERR", "Send Message Error!Messager is NULL!")
//            }
//        }

    }
}