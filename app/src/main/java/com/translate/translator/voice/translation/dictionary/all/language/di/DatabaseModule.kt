package com.translate.translator.voice.translation.dictionary.all.language.di

import android.content.Context
import androidx.room.Room
import com.translate.translator.voice.translation.dictionary.all.language.database.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {

    @Provides
    fun provideHistoryDao(database: EliteTranslationDatabase): TranslationDatabaseDao {
        return database.translationDatabaseDao
    }

    @Provides
    fun provideRecentDao(database: EliteTranslationDatabase): RecentDatabaseDao {
        return database.recentDatabaseDao
    }

    @Provides
    fun provideMultiDao(database: EliteTranslationDatabase): MultiLanguageDatabaseDao {
        return database.multiLanguageDatabaseDao
    }

    @Provides
    fun provideMultiHistoryDao(database: EliteTranslationDatabase): MultiTranslationDatabaseDao {
        return database.multiTranslationDatabaseDao
    }



    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context): EliteTranslationDatabase {
        return Room.databaseBuilder(
            appContext,
            EliteTranslationDatabase::class.java,
            "beta1.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
}