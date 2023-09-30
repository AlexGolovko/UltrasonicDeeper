package com.gmail.golovkobalak.sonar.deeper

import androidx.lifecycle.ViewModel
import com.gmail.golovkobalak.sonar.model.SonarDataEntity
import com.gmail.golovkobalak.sonar.model.SonarDataException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val INITIAL_SIZE = 256

class DeeperViewModel : ViewModel() {
    private var depthListMaxSize: Int = INITIAL_SIZE
    val list: MutableList<SonarDataEntity> = mutableListOf()
    var maxDepth = 1f

    private val _sonarDataEntity = MutableStateFlow(SonarDataEntity())
    val sonarDataEntity = _sonarDataEntity.asStateFlow()
    fun updateDepth(sonarDataEntity: SonarDataEntity) {
        if (list.size > depthListMaxSize) {
            for (index in list.size - 1 downTo depthListMaxSize) {
                list.removeAt(index)
            }
        }
        list.add(0, sonarDataEntity)
        maxDepth = list.maxBy { it.depth }.depth
        _sonarDataEntity.value = sonarDataEntity
    }

    private val _sonarDataException = MutableStateFlow(SonarDataException())
    val sonarDataException = _sonarDataException.asStateFlow()

    fun updateSonarDataError(sonarDataException: SonarDataException) {
        _sonarDataException.value = sonarDataException
    }

    fun updateMaxSize(maxSize: Float) {
        if (depthListMaxSize == INITIAL_SIZE) depthListMaxSize = maxSize.toInt()
    }

}
