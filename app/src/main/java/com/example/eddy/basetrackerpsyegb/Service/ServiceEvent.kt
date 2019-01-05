package com.example.eddy.basetrackerpsyegb.Service

class ServiceEvent(var control: Control, val id: Int = 0) {
    enum class Control {
        START, STOP, PAUSE, RESUME
    }
}