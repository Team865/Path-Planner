package ca.warp7.planner2

import ca.warp7.planner2.state.Constants
import ca.warp7.planner2.state.PixelReference
import edu.wpi.first.wpilibj.geometry.Pose2d
import edu.wpi.first.wpilibj.geometry.Rotation2d
import edu.wpi.first.wpilibj.geometry.Translation2d
import javafx.scene.canvas.GraphicsContext
import kotlin.math.abs

fun drawArrowForPose(ref: PixelReference, gc: GraphicsContext, point: Pose2d) {
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