package com.example.eddy.basetrackerpsyegb.service

class ServiceEvent(
    var control: Control,
    val id: Int = 0,
    var totalTime: Long = 0L
) {
    enum class Control {
        START, STOP, PAUSE, RESUME, STOP_FROM_INTENT
    }
}