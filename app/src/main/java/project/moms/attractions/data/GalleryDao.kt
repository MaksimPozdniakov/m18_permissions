package project.moms.attractions.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface GalleryDao {
    @Insert
    suspend fun insert(photo: Photo): Long // Асинхронное добавление фото, возвращает ID нового элемента

    @Query("SELECT * FROM photos")
    suspend fun getAllPhotos(): List<Photo> // Асинхронное получение всех фото
}