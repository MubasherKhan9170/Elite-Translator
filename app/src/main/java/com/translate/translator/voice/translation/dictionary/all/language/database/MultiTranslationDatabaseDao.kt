package com.translate.translator.voice.translation.dictionary.all.language.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface MultiTranslationDatabaseDao {
    @Insert
    suspend fun insert(item: MultiTranslationItem)

    /**
     * When updating a row with a value already set in a column,
     * replaces the old value with the new one.
     *
     * @param night new value to write
     */
    @Update
    suspend fun update(item: MultiTranslationItem)

    /**
     * Selects and returns the row that matches the supplied start time, which is our key.
     *
     * @param key startTimeMilli to match
     */
    @Query("SELECT * from multi_translation_history_table WHERE id = :key")
    suspend fun get(key: Long): MultiTranslationItem?

    /**
     * Deletes the selected Item values from the table.
     *
     * This does not delete the table, only its contents.
     */
    @Query("DELETE FROM multi_translation_history_table WHERE id = :key")
    suspend fun delete(key: Long)



    /**
     * Deletes all values from the table.
     *
     * This does not delete the table, only its contents.
     */
    @Query("DELETE FROM multi_translation_history_table")
    suspend fun clear()

    /**
     * Selects and returns all rows in the table,
     *
     * sorted by start time in descending order.
     */
    @Query("SELECT * FROM multi_translation_history_table ORDER BY id DESC")
    fun getAllHistory(): LiveData<List<MultiTranslationItem>>

    /**
     * Selects and returns all rows in the table,
     *
     * sorted by start time in descending order.
     */
    @Query("SELECT * FROM multi_translation_history_table WHERE favourite_status = :status")
    fun getAllFavourite(status: Boolean): LiveData<List<MultiTranslationItem>>

    /**
     * Selects and returns the latest night.
     */
    @Query("SELECT * FROM multi_translation_history_table ORDER BY id DESC LIMIT 1")
    suspend fun getTonight(): MultiTranslationItem?
}