package com.example.unipathsa

import org.junit.Assert.assertEquals
import org.junit.Test

class PreparationProgressTest {

    @Test
    fun newUser_startsAtZero() {
        val result = PreparationProgress.calculate(0, 7)

        assertEquals(0, result)
    }

    @Test
    fun completingTasks_increasesProgress() {
        val result = PreparationProgress.calculate(3, 7)

        assertEquals(42, result)
    }

    @Test
    fun allTasksCompleted_returns100() {
        val result = PreparationProgress.calculate(7, 7)

        assertEquals(100, result)
    }

    @Test
    fun progress_doesNotExceed100() {
        val result = PreparationProgress.calculate(10, 7)

        assertEquals(100, result)
    }
}