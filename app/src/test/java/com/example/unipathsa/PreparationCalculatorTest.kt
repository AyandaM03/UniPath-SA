package com.example.unipathsa

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PreparationCalculatorTest {

    private val calculator = PreparationCalculator()

    @Test
    fun testEmptyState_returnsZeroPercent() {
        val state = PreparationTaskState()
        val result = calculator.calculate(state)

        assertEquals(0, result.percentage)
        assertEquals(0, result.completedCount)
        assertEquals(7, result.pendingTasks.size)
        assertTrue(result.completedTasks.isEmpty())
    }

    @Test
    fun testAllTasksCompleted_returnsOneHundredPercent() {
        val state = PreparationTaskState(
            isProfileSaved = true,
            isSchoolAdded = true,
            hasSubjects = true,
            hasAps = true,
            hasSavedItems = true,
            hasApplications = true,
            hasDocuments = true
        )
        val result = calculator.calculate(state)

        assertEquals(100, result.percentage)
        assertEquals(7, result.completedCount)
        assertEquals(7, result.completedTasks.size)
        assertTrue(result.pendingTasks.isEmpty())
    }

    @Test
    fun testPartialTasks_returnsCorrectShare() {
        val state = PreparationTaskState(
            isProfileSaved = true,
            hasAps = true,
            hasApplications = true
        )
        val result = calculator.calculate(state)

        // 3 of 7 tasks = 300 / 7 = 42%
        assertEquals(42, result.percentage)
        assertEquals(3, result.completedCount)
        assertEquals(4, result.pendingTasks.size)
    }
}