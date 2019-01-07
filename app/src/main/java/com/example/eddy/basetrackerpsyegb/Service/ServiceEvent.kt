package com.example.eddy.basetrackerpsyegb.Service

class ServiceEvent(
    var control: Control,
    val id: Int = 0,
    var totalTime: String = "",
    var time: Long = 0
) {
    enum class Control {
        START, STOP, PAUSE, RESUME
    }
}