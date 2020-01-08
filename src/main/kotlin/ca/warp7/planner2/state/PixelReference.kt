package ca.warp7.planner2.state

import edu.wpi.first.wpilibj.geometry.Translation2d

/**
 * Defines functions to scale translations
 * to pixel sizes on the screen
 *
 * In JavaFX, x is to the right, y is down, origin at top-left
 *
 * In Path Planner coordinates, x is up, y is left,
 * origin is at the bottom-centre of the image
 *
 * The border is just extra. No calculations involved
 */
class PixelReference {

    private var pixelPerMetreWidth = 0.0
    private var pixelPerMetreHeight = 0.0

    private var originX = 0.0
    private var originY = 0.0

    fun set(
            width: Double,
            height: Double,
            offsetX: Double,
            offsetY: Double,
            fieldWidth: Double,
            fieldHeight: Double
    ) {
        pixelPerMetreWidth = width / fieldWidth
        pixelPerMetreHeight = height / fieldHeight

        originX = offsetX + width / 2.0
        originY = offsetY + height
    }

    fun scale(point: Translation2d) = Translation2d(
            -pixelPerMetreWidth * point.y,
            -pixelPerMetreHeight * point.x
    )

    fun inverseScale(point: Translation2d) = Translation2d(
            -point.y / pixelPerMetreHeight,
            -point.x / pixelPerMetreWidth
    )

    fun transform(point: Translation2d) = Translation2d(
            originX - pixelPerMetreWidth * point.y,
            originY - pixelPerMetreHeight * point.x
    )

    fun inverseTransform(point: Translation2d) = Translation2d(
            (originY - point.y) / pixelPerMetreHeight,
            (originX - point.x) / pixelPerMetreWidth
    )
}