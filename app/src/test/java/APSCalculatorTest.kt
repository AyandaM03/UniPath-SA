package com.example.unipathsa

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class APSCalculatorTest {

    @Test
    fun apsCalculation_isCorrect() {
        val marks = listOf(80, 70, 60, 50, 40, 30)

        val result = APSCalculator.calculateAPS(marks)

        assertEquals(27, result)
    }

    @Test
    fun apsCalculation_emptyList_returnsZero() {
        val result = APSCalculator.calculateAPS(emptyList())

        assertEquals(0, result)
    }

    @Test
    fun markValidation_acceptsValidMark() {
        assertTrue(APSCalculator.isValidMark(75))
    }

    @Test
    fun markValidation_rejectsMarkAbove100() {
        assertFalse(APSCalculator.isValidMark(101))
    }

    @Test
    fun markValidation_rejectsNegativeMark() {
        assertFalse(APSCalculator.isValidMark(-1))
    }
}