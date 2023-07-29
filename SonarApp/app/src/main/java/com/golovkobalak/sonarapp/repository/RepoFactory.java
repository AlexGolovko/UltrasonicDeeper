package com.golovkobalak.sonarapp.repository;

import com.golovkobalak.sonarapp.repository.realm.SonarDataRealmRepository;

public class RepoFactory {

    public SonarDataRepository sonarDataRepository() {
        return new SonarDataRealmRepository();
    }
}
