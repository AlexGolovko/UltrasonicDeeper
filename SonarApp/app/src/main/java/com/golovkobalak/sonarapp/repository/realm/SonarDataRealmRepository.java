package com.golovkobalak.sonarapp.repository.realm;

import com.golovkobalak.sonarapp.model.GeoSquare;
import com.golovkobalak.sonarapp.model.SonarData;

import java.util.List;

import com.golovkobalak.sonarapp.repository.SonarDataRepository;
import io.realm.Realm;
import io.realm.RealmResults;

public class SonarDataRealmRepository implements SonarDataRepository {
    private Realm instance;

    public SonarDataRealmRepository() {
    }

    @Override
    public void saveList(List<SonarData> sonarDataList) {
        instance = Realm.getInstance(SonarDataRepoConfig.getConfig());
        instance.beginTransaction();
        instance.copyToRealm(sonarDataList);
        instance.commitTransaction();
    }

    @Override
    public List<SonarData> getAll() {
        instance = Realm.getInstance(SonarDataRepoConfig.getConfig());
        return instance.where(SonarData.class).findAll();
    }

    @Override
    public List<SonarData> findByGeoSquare(GeoSquare geoSquare) {
        final Realm instance = Realm.getInstance(SonarDataRepoConfig.getConfig());
        //works only for Ukraine
        return instance.where(SonarData.class)
                .greaterThan(SonarData.Field.LATITUDE, geoSquare.south)
                .lessThan(SonarData.Field.LATITUDE, geoSquare.north)
                .greaterThan(SonarData.Field.LONGITUDE, geoSquare.west)
                .lessThan(SonarData.Field.LONGITUDE, geoSquare.east)
                .findAll();
    }

    @Override
    public void save(SonarData sonarData) {
        try (Realm instance = Realm.getInstance(SonarDataRepoConfig.getConfig())) {
            instance.beginTransaction();
            instance.copyToRealm(sonarData);
            instance.commitTransaction();
        }

    }
}
