package com.example.unipathsa

/**
 * State container for user preparation tasks.
 */
data class PreparationTaskState(
    val isProfileSaved: Boolean = false,
    val isSchoolAdded: Boolean = false,
    val hasSubjects: Boolean = false,
    val hasAps: Boolean = false,
    val hasSavedItems: Boolean = false,
    val hasApplications: Boolean = false,
    val hasDocuments: Boolean = false
)

/**
 * Result container for user preparation score and task lists.
 */
data class PreparationResult(
    val percentage: Int,
    val completedCount: Int,
    val totalTasks: Int = 7,
    val completedTasks: List<String>,
    val pendingTasks: List<String>
)

/**
 * Plain Kotlin class to calculate user preparation percentage based on completed tasks.
 * Unit-testable without Android or Firebase framework dependencies.
 */
class PreparationCalculator {

    /**
     * Calculates preparation percentage (0% to 100%) and categorizes completed/pending tasks.
     * Each of the 7 tasks is worth an equal share (100% / 7 = ~14.28%).
     */
    fun calculate(state: PreparationTaskState): PreparationResult {
        val completedList = mutableListOf<String>()
        val pendingList = mutableListOf<String>()

        if (state.isProfileSaved) completedList.add("Profile Details") else pendingList.add("Save Profile")
        if (state.isSchoolAdded) completedList.add("School") else pendingList.add("Add School")
        if (state.hasSubjects) completedList.add("Subject Marks") else pendingList.add("Add Marks")
        if (state.hasAps) completedList.add("APS Calculated") else pendingList.add("Calculate APS")
        if (state.hasSavedItems) completedList.add("Saved Items") else pendingList.add("Save Courses")
        if (state.hasApplications) completedList.add("Applications") else pendingList.add("Add Applications")
        if (state.hasDocuments) completedList.add("Documents") else pendingList.add("Upload Documents")

        val completedCount = completedList.size
        val totalTasks = 7
        val percentage = (completedCount * 100) / totalTasks

        return PreparationResult(
            percentage = percentage,
            completedCount = completedCount,
            totalTasks = totalTasks,
            completedTasks = completedList,
            pendingTasks = pendingList
        )
    }
}