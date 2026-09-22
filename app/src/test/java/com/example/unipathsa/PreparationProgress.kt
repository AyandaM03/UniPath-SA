package com.example.unipathsa

object PreparationProgress {

    fun calculate(completedTasks: Int, totalTasks: Int): Int {
        if (totalTasks <= 0) return 0

        val percentage = (completedTasks.toDouble() / totalTasks * 100).toInt()

        return percentage.coerceIn(0, 100)
    }
}