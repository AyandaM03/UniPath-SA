package com.example.unipathsa

object APSCalculator {

    fun calculateAPS(marks: List<Int>): Int {
        if (marks.isEmpty()) return 0

        return marks.sumOf { mark ->
            when {
                mark >= 80 -> 7
                mark >= 70 -> 6
                mark >= 60 -> 5
                mark >= 50 -> 4
                mark >= 40 -> 3
                mark >= 30 -> 2
                else -> 1
            }
        }
    }

    fun isValidMark(mark: Int): Boolean {
        return mark in 0..100
    }
}