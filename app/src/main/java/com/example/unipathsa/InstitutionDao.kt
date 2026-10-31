package com.example.unipathsa

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class InstitutionDao {

    @Query("SELECT * FROM institutions ORDER BY name")
    abstract fun getAll(): Flow<List<InstitutionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(items: List<InstitutionEntity>)

    @Query("DELETE FROM institutions")
    abstract suspend fun clear()

    // Replaces everything, so institutions deleted in Firestore disappear too
    @Transaction
    open suspend fun replaceAll(items: List<InstitutionEntity>) {
        clear()
        insertAll(items)
    }
}