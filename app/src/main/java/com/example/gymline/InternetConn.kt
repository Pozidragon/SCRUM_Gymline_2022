package com.example.gymline

class InternetConn {
    companion object {
        fun internetIsConnected(): Boolean {
            return try {
                val command = "ping -c 1 google.com"
                Runtime.getRuntime().exec(command).waitFor() == 0
            } catch (e: Exception) {
                false
            }
        }
    }
}