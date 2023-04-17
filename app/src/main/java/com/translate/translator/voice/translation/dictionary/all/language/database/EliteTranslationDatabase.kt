package com.translate.translator.voice.translation.dictionary.all.language.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [TranslationItem::class, RecentItem::class, MultiLangItem::class, MultiTranslationItem::class], version = 9, exportSchema = false)
abstract class EliteTranslationDatabase: RoomDatabase() {

    abstract val translationDatabaseDao: TranslationDatabaseDao
    abstract val recentDatabaseDao:RecentDatabaseDao
    abstract val multiLanguageDatabaseDao: MultiLanguageDatabaseDao
    abstract val multiTranslationDatabaseDao: MultiTranslationDatabaseDao

    /*companion object {
        @Volatile
        private var INSTANCE: EliteTranslationDatabase? = null

        fun getInstance(context: Context): EliteTranslationDatabase {
            synchronized(this){
                var instance = INSTANCE

                if(instance == null){
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        EliteTranslationDatabase::class.java,
                        "elite_room_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }

    }*/
}
