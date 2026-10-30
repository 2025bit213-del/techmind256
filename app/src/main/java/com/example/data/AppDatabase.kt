package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.*
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        CaseEntity::class,
        PaymentTransactionEntity::class,
        PromiseToPayEntity::class,
        FieldVisitEntity::class,
        LegalDocumentEntity::class,
        AuditLogEntity::class,
        SystemConfigEntity::class,
        TimelineActionEntity::class,
        SmsNotificationEntity::class,
        PaymentPlanEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun caseDao(): CaseDao
    abstract fun paymentDao(): PaymentDao
    abstract fun promiseDao(): PromiseDao
    abstract fun fieldVisitDao(): FieldVisitDao
    abstract fun legalDocDao(): LegalDocDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun systemConfigDao(): SystemConfigDao
    abstract fun timelineDao(): TimelineDao
    abstract fun smsNotificationDao(): SmsNotificationDao
    abstract fun paymentPlanDao(): PaymentPlanDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vml_recovery_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.let { database ->
                                    SampleDataProvider.populateInitialData(database)
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
