package com.golovkobalak.sonarapp.controller

import android.util.Log
import com.golovkobalak.sonarapp.SonarContext
import io.javalin.Javalin
import java.io.File

class TilesController {
    private val TAG = TilesController::class.java.name

    private var app: Javalin? = null
    fun start() {
        app = Javalin.create().start(7000)

        // Add the "/{z}/{x}/{y}.png" route and associate it with the handler
        app!!.get("/{z}/{x}/{y}.png") { ctx ->
            // Extract the z, x, and y values from the path parameters
            val z = ctx.pathParam("z")
            val x = ctx.pathParam("x")
            val y = ctx.pathParam("y")

            // Generate the path to the image file
            val imagePath = SonarContext.filesDirAbsPath + "/Tiles/$z/$x/$y.png"
            Log.d(TAG, "imagePath: $imagePath")
            // Check if the file exists
            val imageFile = File(imagePath)
            if (imageFile.exists() && imageFile.isFile) {
                // Set the Content-Type header to "image/png"
                ctx.contentType("image/png")

                // Return the image file
                ctx.result(imageFile.readBytes())
            } else {
                // If the file doesn't exist, return a 404 Not Found response
                ctx.status(404).result("Tile not found")
            }
        }
        Log.d(TAG, "started")

    }
    fun destroy() {
        app!!.stop();
    }

}