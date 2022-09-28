package com.golovkobalak.sonarapp.model;

import java.nio.charset.StandardCharsets;

import io.javalin.http.ContentType;

public class Config {
    private String hostedPath;
    private byte[] fileBody;
    private ContentType contentType;

    public Config(String hostedPath, byte[] fileBody, ContentType contentType) {
        this.hostedPath = hostedPath;
        this.fileBody = fileBody;
        this.contentType = contentType;
    }

    public Config(String hostedPath, String fileBody, ContentType contentType) {
        this.hostedPath = hostedPath;
        this.fileBody = fileBody.getBytes(StandardCharsets.UTF_8);
        this.contentType = contentType;
    }


    public String getHostedPath() {
        return hostedPath;
    }

    public byte[] getFileBody() {
        return fileBody;
    }

    public ContentType getContentType() {
        return contentType;
    }
}