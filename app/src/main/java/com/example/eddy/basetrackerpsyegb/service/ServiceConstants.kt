package com.example.eddy.basetrackerpsyegb.service


object RECEIVER{
    const val FLAG = 100
    const val RECEIVER_FILTER = "com.example.eddy.basetrackerpsyegb.RECEIVER.action.GET_UPDATES"

//    const val START_TRACKING = "com.example.eddy.basetrackerpsyegb.RECEIVER.action.START_TRACKING"
//    const val TRACKING = "com.example.eddy.basetrackerpsyegb.RECEIVER.action.TRACKING_UPDATE"
//    const val UPDATE_TRACKING = "com.example.eddy.basetrackerpsyegb.RECEIVER.action.UPDATE_TRACKING"
//    const val STOP_TRACKING = "com.example.eddy.basetrackerpsyegb.RECEIVER.action.STOPTRACKING"
}

object COMMAND{
    val COMMAND = "COMMAND"
    val START_TRACKING = 0
    val UPDATE_TRACKING = 1
    val STOP_TRACKING = 2
    val PAUSE_TRACKING =3
    val RESUME_TRACKING =4
}
object ACTION{
    const val START_TRACKING = "com.example.eddy.basetrackerpsyegb.action.START_TRACKING"
//    const val STOP_TRACKING = "com.example.eddy.basetrackerpsyegb.action.STOP_TRACKING"
    const val PAUSE_TRACKING = "com.example.eddy.basetrackerpsyegb.action.PAUSE_TRACKING"
    const val RESUME_TRACKING = "com.example.eddy.basetrackerpsyegb.action.RESUME_TRACKING"
    const val TOGGLE_TRACKING = "com.example.eddy.basetrackerpsyegb.action.TOGGLE_TRACKING"

    const val STOP_TRACKING = "com.example.eddy.basetrackerpsyegb.action.STOP_TRACKING"
    const val STOP_SERVICE = "com.example.eddy.basetrackerpsyegb.action.STOP_SERVICE"


}
object NOTIFICATION_ID{
    const val FOREGROUND_SERVICE = 101
    private val CHANNEL_ID = "com.example.eddy.basetrackerpsyegb"
}