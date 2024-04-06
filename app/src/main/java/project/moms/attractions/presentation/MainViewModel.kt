package project.moms.attractions.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import project.moms.attractions.data.GalleryDao
import project.moms.attractions.data.Photo

class MainViewModel(
    private val galleryDao: GalleryDao
) : ViewModel() {

    val allPhotos = this.galleryDao.getAllPhotos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    fun onSave(newPhoto: ByteArray, date: String) {
        viewModelScope.launch {
            galleryDao.insert(Photo(0, date, newPhoto))
        }
    }
}