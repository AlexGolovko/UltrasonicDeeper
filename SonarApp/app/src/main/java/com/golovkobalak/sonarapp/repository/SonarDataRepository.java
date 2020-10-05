package com.golovkobalak.sonarapp.repository;

import com.golovkobalak.sonarapp.model.SonarData;

import java.util.List;

import io.realm.Realm;

public class SonarDataRepository {
    private Realm instance;

    public SonarDataRepository() {
    }

    public void saveList(List<SonarData> sonarDataList) {
        instance = Realm.getInstance(SonarDataRepoConfig.getConfig());
        instance.beginTransaction();
        instance.copyToRealm(sonarDataList);
        instance.commitTransaction();
    }

    public List<SonarData> getAll() {
        instance = Realm.getInstance(SonarDataRepoConfig.getConfig());
        return instance.where(SonarData.class).findAll();
    }
}
