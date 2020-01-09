package ca.warp7.planner2

import ca.warp7.planner2.state.Constants
import ca.warp7.planner2.state.Path
import ca.warp7.planner2.state.PixelReference
import edu.wpi.first.wpilibj.geometry.Pose2d
import edu.wpi.first.wpilibj.geometry.Rotation2d
import edu.wpi.first.wpilibj.geometry.Translation2d
import edu.wpi.first.wpilibj.trajectory.Trajectory
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.scene.text.FontSmoothingType
import kotlin.math.abs

fun drawArrowForPose(ref: PixelReference, gc: GraphicsContext, point: Pose2d) {
    gc.lineWidth = 2.0

    val posOnCanvas = ref.transform(point.translation)

    val directionVector = point.rotation.translation()
    val arrowOffset = ref.transform(point.translation + directionVector.times(Constants.kArrowLength))

    // This is the offset from the base of the tip to the actual tip
    val r1 = directionVector.times(Constants.kArrowTipLength * Constants.k60DegreesRatio * 2)
    val r2 = r1.rotateBy(Rotation2d(0.0, 1.0)).times(Constants.k60DegreesRatio)
    val r3 = r1.rotateBy(Rotation2d(0.0, -1.0)).times(Constants.k60DegreesRatio)

    val ovalSize = ref.scale(Translation2d(Constants.kControlPointCircleSize, Constants.kControlPointCircleSize))
    val ovalWidth = abs(ovalSize.x)
    val ovalHeight = abs(ovalSize.y)
    gc.strokeOval(posOnCanvas.x - ovalWidth / 2.0,
            posOnCanvas.y - ovalHeight / 2.0, ovalWidth, ovalHeight)

    val start = posOnCanvas + ref.scale(directionVector.times(Constants.kControlPointCircleSize / 2.0))
    gc.lineTo(start, arrowOffset)

    val a1 = arrowOffset + ref.scale(r1)
    val a2 = arrowOffset + ref.scale(r2)
    val a3 = arrowOffset + ref.scale(r3)

    gc.beginPath()
    gc.vertex(a1)
    gc.vertex(a2)
    gc.vertex(a3)
    gc.vertex(a1)
    gc.closePath()
    gc.stroke()
}

fun drawSplines(
        ref: PixelReference,
        trajectory: Trajectory,
        odd: Boolean,
        gc: GraphicsContext,
        robotWidth: Double,
        robotLength: Double
) {

    val maxCurvature = trajectory.states.map { abs(it.curvatureRadPerMeter) }.max()!!

    gc.lineWidth = 1.5

    val s0 = trajectory.states.first()
    val t0 = s0.poseMeters.translation
    var normal = s0.poseMeters.rotation.normal().translation() * robotWidth
    var left = ref.transform(t0 - normal)
    var right = ref.transform(t0 + normal)

    if (s0.curvatureRadPerMeter.isFinite()) {
        if (odd) {
            gc.stroke = Color.rgb(0, 128, 255)
        } else {
            gc.stroke = Color.rgb(0, 255, 0)
        }
        val a0 = ref.transform(t0) - ref.scale(Translation2d(robotLength,
                robotWidth).rotateBy(s0.poseMeters.rotation))
        val b0 = ref.transform(t0) + ref.scale(Translation2d(-robotLength,
                robotWidth).rotateBy(s0.poseMeters.rotation))
        gc.lineTo(a0, b0)
        gc.lineTo(left, a0)
        gc.lineTo(right, b0)
    }

    for (i in 1 until trajectory.states.size) {
        val s = trajectory.states[i]
        val t = s.poseMeters.translation
        normal = s.poseMeters.rotation.normal().translation() * robotWidth
        val newLeft = ref.transform(t - normal)
        val newRight = ref.transform(t + normal)

        if (s.curvatureRadPerMeter.isFinite()) {
            val kx = abs(s.curvatureRadPerMeter) / maxCurvature
            if (odd) {
                val r = linearInterpolate(0.0, 192.0, kx) + 63
                val b = 255 - linearInterpolate(0.0, 192.0, kx)
                gc.stroke = Color.rgb(r.toInt(), 128, b.toInt())
            } else {
                val r = linearInterpolate(0.0, 192.0, kx) + 63
                val g = 255 - linearInterpolate(0.0, 192.0, kx)
                gc.stroke = Color.rgb(r.toInt(), g.toInt(), 0)
            }
        } else {
            gc.stroke = Color.MAGENTA
        }

        gc.lineTo(left, newLeft)
        gc.lineTo(right, newRight)
        left = newLeft
        right = newRight
    }

    val s1 = trajectory.states.last()

    if (s1.curvatureRadPerMeter.isFinite()) {
        val t1 = s1.poseMeters.translation
        if (odd) {
            gc.stroke = Color.rgb(0, 128, 255)
        } else {
            gc.stroke = Color.rgb(0, 255, 0)
        }
        val a1 = ref.transform(t1) - ref.scale(Translation2d(-robotLength,
                robotWidth).rotateBy(s1.poseMeters.rotation))
        val b1 = ref.transform(t1) + ref.scale(Translation2d(robotLength,
                robotWidth).rotateBy(s1.poseMeters.rotation))
        gc.lineTo(a1, b1)
        gc.lineTo(left, a1)
        gc.lineTo(right, b1)
    }
}

fun drawRobot(
        ref: PixelReference,
        gc: GraphicsContext,
        robotWidth: Double,
        robotLength: Double,
        point: Pose2d
) {
    val pos = ref.transform(point.translation)
    val heading = point.rotation
    val a = ref.scale(Translation2d(robotLength, robotWidth).rotateBy(heading))
    val b = ref.scale(Translation2d(robotLength, -robotWidth).rotateBy(heading))
    val p1 = pos + a
    val p2 = pos + b
    val p3 = pos - a
    val p4 = pos - b
//    gc.stroke = Color.rgb(60, 92, 148)
    gc.lineWidth = 2.0
    gc.stroke = Color.WHITE
    gc.fill = Color.rgb(90, 138, 222)
    gc.beginPath()
    gc.vertex(p1)
    gc.vertex(p2)
    gc.vertex(p3)
    gc.vertex(p4)
    gc.vertex(p1)
    gc.closePath()
    gc.stroke()
    gc.fill()
}


fun drawGraph(gc: GraphicsContext, path: Path) {

    val pad = 16.0

    val width = gc.canvas.width - pad * 2.0
    val height = gc.canvas.height - pad * 2.0

    val timeHeight = 16.0
    val textHeight = 20.0
    val spaceHeight = 16.0
    val graphHeight = (height - textHeight * 3 - timeHeight - 2 * spaceHeight) / 4.0

    val timeTop = pad + textHeight
    val timeBottom = timeTop + timeHeight

    val velTop = timeBottom + textHeight + spaceHeight
    val velMid = velTop + graphHeight
    val velBottom = velMid + graphHeight

    val accTop = velBottom + textHeight + spaceHeight
    val accMid = accTop + graphHeight
    val accBottom = accMid + graphHeight

    gc.fill = Color.BLACK
    gc.fillRect(0.0, 0.0, gc.canvas.width, gc.canvas.height)

    gc.fontSmoothingType = FontSmoothingType.LCD
    gc.lineWidth = 1.0
    gc.fill = Color.WHITE
    gc.fillText("Time Steps (Total: ${path.totalTime.f}s)", pad, timeTop - 10.0)
    gc.fillText("Velocity vs Time (±${path.maxVelocity}m/s)", pad, velTop - 10.0)
    gc.fillText("Acceleration vs Time (±${path.maxAcceleration}m/s^2)", pad, accTop - 10.0)

    gc.stroke = Color.ORANGE
    var trackedTime = 0.0
    for (trajectory in path.trajectoryList) {
        for (trajectoryState in trajectory.states) {
            val progress = (trajectoryState.timeSeconds + trackedTime) / path.totalTime
            val x = (pad + progress * width)
            gc.strokeLine(x, timeTop, x, timeBottom)
        }
        trackedTime += trajectory.totalTimeSeconds
    }
    gc.stroke = Color.GRAY
    gc.lineWidth = 1.0

    val velPxPerM = graphHeight / path.maxVelocity
    gc.strokeLine(pad, velTop, pad + width, velTop)
    gc.strokeLine(pad, velBottom, pad + width, velBottom)
    var velStep = -path.maxVelocity.toInt()
    if (velStep.toDouble() == -path.maxVelocity) velStep++
    while (velStep < path.maxVelocity) {
        val h = velTop + (path.maxVelocity - velStep) * velPxPerM
        gc.strokeLine(pad, h, pad + width, h)
        velStep++
    }

    val accPxPerM = graphHeight / path.maxAcceleration
    gc.strokeLine(pad, accTop, pad + width, accTop)
    gc.strokeLine(pad, accBottom, pad + width, accBottom)
    var accStep = -path.maxAcceleration.toInt()
    if (accStep.toDouble() == -path.maxAcceleration) accStep++
    while (accStep < path.maxAcceleration) {
        val h = accTop + (path.maxAcceleration - accStep) * accPxPerM
        gc.strokeLine(pad, h, pad + width, h)
        accStep++
    }

    gc.lineWidth = 2.0
    gc.stroke = Color.rgb(128, 128, 255)
    trackedTime = 0.0
    gc.beginPath()
    for (trajectory in path.trajectoryList) {
        for (trajectoryState in trajectory.states) {
            val progress = (trajectoryState.timeSeconds + trackedTime) / path.totalTime
            val v = trajectoryState.velocityMetersPerSecond

            gc.lineTo(pad + progress * width, velMid - v / path.maxVelocity * graphHeight)
        }
        trackedTime += trajectory.totalTimeSeconds
    }
    gc.stroke()

    gc.lineWidth = 2.0
    gc.stroke = Color.rgb(0, 128, 192)
    trackedTime = 0.0
    gc.beginPath()
    for (trajectory in path.trajectoryList) {
        for (trajectoryState in trajectory.states) {
            val progress = (trajectoryState.timeSeconds + trackedTime) / path.totalTime
            val dv = trajectoryState.accelerationMetersPerSecondSq

            gc.lineTo(pad + progress * width, accMid - dv / path.maxAcceleration * graphHeight)
        }
        trackedTime += trajectory.totalTimeSeconds
    }
    gc.stroke()

}