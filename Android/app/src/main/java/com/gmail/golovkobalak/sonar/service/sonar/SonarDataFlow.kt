package com.gmail.golovkobalak.sonar.service.sonar

import com.gmail.golovkobalak.sonar.model.SonarDataEntity
import com.gmail.golovkobalak.sonar.model.SonarDataException
import kotlinx.coroutines.flow.MutableStateFlow

object SonarDataFlow {
    val SonarDataFlow = MutableStateFlow(SonarDataEntity())
    val SonarDataErrorFlow = MutableStateFlow(SonarDataException())
}