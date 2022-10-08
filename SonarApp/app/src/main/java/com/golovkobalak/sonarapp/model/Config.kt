package com.golovkobalak.sonarapp.model

import io.javalin.http.ContentType
import java.nio.charset.StandardCharsets

class Config {
    val hostedPath: String
    val fileBody: ByteArray
    val contentType: ContentType

    constructor(hostedPath: String, fileBody: ByteArray, contentType: ContentType) {
        this.hostedPath = hostedPath
        this.fileBody = fileBody
        this.contentType = contentType
    }

    constructor(hostedPath: String, fileBody: String, contentType: ContentType) {
        this.hostedPath = hostedPath
        this.fileBody = fileBody.toByteArray(StandardCharsets.UTF_8)
        this.contentType = contentType
    }
}