package com.golovkobalak.sonarapp.repository.realm;

import io.realm.DynamicRealm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;

public class SonarDataRepoConfig {
    private static final RealmConfiguration config = new RealmConfiguration.Builder()
            .schemaVersion(1)
            .migration(new SonarDataRepoMigration())
            .build();

    public static RealmConfiguration getConfig() {
        return config;
    }

    private static class SonarDataRepoMigration implements RealmMigration {
        @Override
        public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {

        }
    }
}
