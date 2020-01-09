package ca.warp7.planner2.state

import ca.warp7.frc2020.lib.trajectory.QuinticHermiteSpline
import edu.wpi.first.wpilibj.trajectory.Trajectory
import edu.wpi.first.wpilibj.trajectory.TrajectoryParameterizer
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

    var robotWidth = 0.4
    var robotLength = 0.6

    var maxVelocity = 3.0
    var maxAcceleration = 3.0

    var optimizing = false

    val trajectoryList = mutableListOf<Trajectory>()

    fun regenerateAll() {
        val x = QuinticHermiteSpline.parameterize(controlPoints.map { it.pose }
                .zipWithNext { a, b -> QuinticHermiteSpline.fromPose(a, b) })
        trajectoryList.clear()
        trajectoryList.add(TrajectoryParameterizer.timeParameterizeTrajectory(x, listOf(),
                0.0, 0.0, 3.0,
                3.0, false))
        totalTime = trajectoryList.sumByDouble { it.totalTimeSeconds }
    }
}