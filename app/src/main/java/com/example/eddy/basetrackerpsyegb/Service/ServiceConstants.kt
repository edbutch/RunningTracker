package com.example.eddy.basetrackerpsyegb.Service



object ACTION{
    const val START_TRACKING = "com.example.eddy.basetrackerpsyegb.action.START_TRACKING"
    const val STOP_TRACKING = "com.example.eddy.basetrackerpsyegb.action.STOP_TRACKING"
    const val PAUSE_TRACKING = "com.example.eddy.basetrackerpsyegb.action.PAUSE_TRACKING"
    const val RESUME_TRACKING = "com.example.eddy.basetrackerpsyegb.action.RESUME_TRACKING"
    const val TOGGLE_TRACKING = "com.example.eddy.basetrackerpsyegb.action.TOGGLE_TRACKING"

    const val STOP_SERVICE = "com.example.eddy.basetrackerpsyegb.action.STOP_SERVICE"


}
object NOTIFICATION_ID{
    const val FOREGROUND_SERVICE = 101
    private val CHANNEL_ID = "com.example.eddy.basetrackerpsyegb"
}