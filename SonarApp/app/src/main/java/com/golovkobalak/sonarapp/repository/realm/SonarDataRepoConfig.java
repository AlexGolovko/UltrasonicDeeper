package com.golovkobalak.sonarapp.repository.realm;

import io.realm.DynamicRealm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;

public class SonarDataRepoConfig {
    private static final RealmConfiguration config = new RealmConfiguration.Builder()
            .deleteRealmIfMigrationNeeded()
            .build();

    public static RealmConfiguration getConfig() {
        return config;
    }
}
