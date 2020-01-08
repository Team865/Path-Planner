package ca.warp7.planner2.state

import javafx.scene.image.Image

class Path {
    var useFeaturesNotInWPILib = false

    var background = Image(Path::class.java.getResourceAsStream("/2020.png"))

    val controlPoints: MutableList<ControlPoint> = ArrayList()

    var maxAngular = 0.0
    var maxAngularAcc = 0.0

    var totalTime = 0.0
    var totalSumOfCurvature = 0.0
    var totalDist = 0.0

    var robotWidth = 0.0
    var robotLength = 0.0

    var maxVelocity = 0.0
    var maxAcceleration = 0.0
}