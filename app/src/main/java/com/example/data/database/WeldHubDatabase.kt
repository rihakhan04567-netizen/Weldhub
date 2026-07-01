package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        DesignEntity::class,
        PortfolioEntity::class,
        BookingEntity::class,
        MessageEntity::class,
        FavoriteEntity::class,
        ReviewEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class WeldHubDatabase : RoomDatabase() {
    abstract fun weldHubDao(): WeldHubDao

    companion object {
        @Volatile
        private var INSTANCE: WeldHubDatabase? = null

        fun getDatabase(context: Context): WeldHubDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WeldHubDatabase::class.java,
                    "weldhub_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
