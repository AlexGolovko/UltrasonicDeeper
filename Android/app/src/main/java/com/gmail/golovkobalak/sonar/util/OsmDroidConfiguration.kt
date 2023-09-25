package com.gmail.golovkobalak.sonar.util

import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.TileSourcePolicy
import org.osmdroid.tileprovider.tilesource.XYTileSource


object OsmDroidConfiguration {
    fun baseTileSource(): OnlineTileSourceBase {
        //Custom tile source without flag no bulk
        val tileSource: OnlineTileSourceBase = XYTileSource(
            "Mapnik",
            0, 22, 512, ".png", arrayOf(
                "https://a.tile.openstreetmap.org/",
                "https://b.tile.openstreetmap.org/",
                "https://c.tile.openstreetmap.org/"
            ), "© OpenStreetMap contributors",
            TileSourcePolicy(
                2,
                TileSourcePolicy.FLAG_NO_PREVENTIVE
                        or TileSourcePolicy.FLAG_USER_AGENT_MEANINGFUL
                        or TileSourcePolicy.FLAG_USER_AGENT_NORMALIZED
            )
        )
        return tileSource
    }
}