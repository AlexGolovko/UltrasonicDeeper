package com.golovkobalak.sonarapp.repository;

import com.golovkobalak.sonarapp.model.GeoSquare;
import com.golovkobalak.sonarapp.model.SonarData;

import java.util.List;

public interface SonarDataRepository {
    void saveList(List<SonarData> sonarDataList);

    List<SonarData> getAll();

    List<SonarData> findByGeoSquare(GeoSquare geoSquare);

    void save(SonarData sonarData);
}
