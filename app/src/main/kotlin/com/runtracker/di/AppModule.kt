package com.runtracker.di

import android.content.Context
import androidx.room.Room
import com.runtracker.data.db.RunTrackerDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): RunTrackerDatabase =
        Room.databaseBuilder(ctx, RunTrackerDatabase::class.java, "runtracker.db").build()

    @Provides fun provideUserDao(db: RunTrackerDatabase)  = db.userDao()
    @Provides fun provideRunDao(db: RunTrackerDatabase)   = db.runDao()
    @Provides fun provideRouteDao(db: RunTrackerDatabase) = db.routeDao()
}
