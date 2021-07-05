package ridaz.ksk.sowit_test_android.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ridaz.ksk.sowit_test_android.db.PolygonDao
import ridaz.ksk.sowit_test_android.db.PolygonDb
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class PolygonDbModule {

    companion object{

        @Provides
        @Singleton
         fun providePolygonDao(polygonDb: PolygonDb) : PolygonDao = polygonDb.polygonDao()

        @Provides
        @Singleton
         fun buildDatabase(application: Application) =
            Room.databaseBuilder(application, PolygonDb::class.java, "polygon_database")
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build()
    }
}