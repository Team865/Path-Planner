package ca.warp7.planner2

import ca.warp7.planner2.fx.combo
import ca.warp7.planner2.fx.menuItem
import ca.warp7.planner2.state.Constants
import ca.warp7.planner2.state.PixelReference
import ca.warp7.planner2.state.getDefaultPath
import edu.wpi.first.wpilibj.geometry.Pose2d
import edu.wpi.first.wpilibj.geometry.Rotation2d
import edu.wpi.first.wpilibj.geometry.Translation2d
import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.collections.FXCollections
import javafx.collections.MapChangeListener
import javafx.collections.ObservableMap
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.Label
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCombination
import javafx.scene.input.MouseEvent
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.stage.Stage
import kotlin.math.min

class Planner2 {

    val stage = Stage()

    val menuBar = MenuBar()

    val canvas: Canvas = Canvas()
    val canvasContainer = Pane(canvas)

    var simulating = false

    val pathStatus: ObservableMap<String, String> = FXCollections
            .observableMap<String, String>(LinkedHashMap())

    private val pathStatusLabel = Label().apply {
        style = "-fx-text-fill: white"
    }

    val pointStatus: ObservableMap<String, String> = FXCollections
            .observableMap<String, String>(LinkedHashMap())

    private val pointStatusLabel = Label()

    val view = BorderPane().apply {
        top = menuBar
        center = canvasContainer
        bottom = VBox().apply {
            children.addAll(
                    HBox().apply {
                        style = "-fx-background-color: white"
                        padding = Insets(4.0, 16.0, 4.0, 16.0)
                        children.add(pointStatusLabel)
                    },
                    HBox().apply {
                        style = "-fx-background-color: #1e2e4a"
                        padding = Insets(4.0, 16.0, 4.0, 16.0)
                        children.add(pathStatusLabel)
                    }
            )
        }
    }

    val referenceImage = Image(Planner2::class.java.getResourceAsStream("/reference.png"))

    init {
        menuBar.isUseSystemMenuBar = true
        pathStatus.addListener(MapChangeListener {
            pathStatusLabel.text = pathStatus.entries
                    .joinToString("   ") { it.key + ": " + it.value }
        })
        pointStatus.addListener(MapChangeListener {
            pointStatusLabel.text = pointStatus.entries
                    .joinToString("   ") { it.key + ": " + it.value }
        })
        canvas.isFocusTraversable = true
        canvas.addEventFilter(MouseEvent.MOUSE_CLICKED) { canvas.requestFocus() }
        stage.scene = Scene(view)
        stage.title = "FRC Drive Path Planner"
        stage.width = 1000.0
        stage.height = 600.0
        stage.icons.add(Image(Planner2::class.java.getResourceAsStream("/icon.png")))
    }

    val dialogs = Dialogs(stage)
    val gc: GraphicsContext = canvas.graphicsContext2D

    var controlDown = false

    val path = getDefaultPath()
    val ref = PixelReference()

    private val fileMenu = Menu("File", null,
            menuItem("New/Open Trajectory", combo(KeyCode.N, control = true)) {
                PathWizard(stage).show()
            },
            menuItem("Save as", combo(KeyCode.S, control = true)) {

            },
            menuItem("Configure Path", combo(KeyCode.COMMA, control = true)) {
                //                config.showSettings(stage)
//                regenerate()
            },
            MenuItem("Generate WPILib Java function"),
            MenuItem("Generate WPILib C++ function"),
            MenuItem("Generate PathFinder-style CSV")
    )

    private val editMenu = Menu(
            "Edit",
            null,
            MenuItem("Insert Spline Control Point"),
            MenuItem("Insert Reverse Direction"),
            MenuItem("Insert Quick Turn"),
            MenuItem("Delete Point(s)"),
            MenuItem("Reverse Point(s)"),
            MenuItem("Snap Point(s) to 0.01m"),
            MenuItem("Edit Point"),
            MenuItem("Add Change to Point(s)"),
            menuItem("Mirror path about x axis", combo(KeyCode.M)) {

            },
            menuItem("Select All", combo(KeyCode.A, control = true)) {
                //                for (cp in state.controlPoints) cp.isSelected = true
//                redrawScreen()
            }
    )

    private fun transformItem(name: String, combo: KeyCombination, x: Double,
                              y: Double, theta: Double, fieldRelative: Boolean): MenuItem {
        return menuItem(name, combo) {
            transformSelected(x, y, theta, fieldRelative)
        }
    }

    private val pointMenu = Menu(
            "Control Point",
            null,
            transformItem("Rotate 1 degree counter-clockwise", combo(KeyCode.Q), 0.0, 0.0, 1.0, false),
            transformItem("Rotate 1 degree clockwise", combo(KeyCode.W), 0.0, 0.0, -1.0, false),
            transformItem("Move up 0.01 metres", combo(KeyCode.UP), 0.01, 0.0, 1.0, true),
            transformItem("Move down 0.01 metres", combo(KeyCode.DOWN), -0.01, 0.0, 1.0, true),
            transformItem("Move left 0.01 metres", combo(KeyCode.LEFT), 0.0, 0.01, 1.0, true),
            transformItem("Move right 0.01 metres", combo(KeyCode.RIGHT), 0.0, -0.01, 1.0, true),
            transformItem("Move forward 0.01 metres", combo(KeyCode.UP, shift = true), 0.01, 0.0, 1.0, false),
            transformItem("Move reverse 0.01 metres", combo(KeyCode.DOWN, shift = true), -0.01, 0.0, 1.0, false),
            transformItem("Move left-normal 0.01 metres", combo(KeyCode.LEFT, shift = true), 0.0, 0.01, 1.0, false),
            transformItem("Move right-normal 0.01 metres", combo(KeyCode.RIGHT, shift = true), 0.0, -0.01, 1.0, false)
    )

    private val constraintMenu = Menu("Constraints")

    private val viewMenu = Menu("View", null,
            MenuItem("Resize Canvas to Window"),
            menuItem("Start/Pause Simulation", combo(KeyCode.SPACE)) { onSpacePressed() },
            menuItem("Stop Simulation", combo(KeyCode.DIGIT0)) { stopSimulation() },
            menuItem("Toggle graphs", combo(KeyCode.G)) { }
    )

    init {
        for (handler in handlers) {
            constraintMenu.items.add(MenuItem(handler.getName()).apply {
                this.setOnAction { handler.editConstraint(stage) }
            })
        }
        menuBar.menus.addAll(
                fileMenu,
                editMenu,
                pointMenu,
                constraintMenu,
                viewMenu,
                dialogs.helpMenu
        )
        canvas.setOnMouseClicked { onMouseClick(it.x, it.y) }
        stage.scene.setOnKeyPressed {
            if (it.code == KeyCode.CONTROL) {
                controlDown = true
            }
        }
        stage.scene.setOnKeyReleased {
            if (it.code == KeyCode.CONTROL) {
                controlDown = false
            }
        }
    }

    fun onMouseClick(x: Double, y: Double) {

    }

    fun onSpacePressed() {

    }

    fun stopSimulation() {

    }

    fun transformSelected(x: Double, y: Double, theta: Double, fieldRelative: Boolean) {
        val delta = Translation2d(x, y)
        val rotation = Rotation2d.fromDegrees(theta)
        var offset = delta
        if (!fieldRelative) {
            for (controlPoint in path.controlPoints) {
                if (controlPoint.isSelected) {
                    offset = delta.rotateBy(controlPoint.pose.rotation)
                }
            }
        }
        for (controlPoint in path.controlPoints) {
            if (controlPoint.isSelected) {
                val oldPose = controlPoint.pose
                val newPose = Pose2d(snap(oldPose.translation + offset), oldPose.rotation + rotation)
                controlPoint.pose = newPose
            }
        }
        regenerate()
    }

    fun show() {
        Platform.runLater {
            regenerate()
            stage.show()
            val resizeListener = ChangeListener<Number> { _, _, _ ->
                redrawScreen()
            }

            canvas.widthProperty().bind(canvasContainer.widthProperty())
            canvas.heightProperty().bind(canvasContainer.heightProperty())
            canvas.widthProperty().addListener(resizeListener)
            canvas.heightProperty().addListener(resizeListener)
        }
    }

    fun regenerate() {
        pointStatus["Test"] = "Hello World"
        redrawScreen()
    }

    fun redrawScreen() {
        val bg = path.background
        val imageWidthToHeight = bg.width / bg.height

        var w = canvas.width - 32
        var h = canvas.height - 32

        w = min(w, h * imageWidthToHeight)
        h = min(w * imageWidthToHeight, w / imageWidthToHeight)
        w = h * imageWidthToHeight

        val offsetX = (canvas.width - w) / 2.0
        val offsetY = (canvas.height - h) / 2.0

        ref.set(w, h, offsetX, offsetY, Constants.kFieldSize * 2, Constants.kFieldSize)

        gc.fill = Color.WHITE
        gc.fillRect(0.0, 0.0, canvas.width, canvas.height)
        gc.drawImage(bg, offsetX, offsetY, w, h)
        drawAllControlPoints()
    }

    private fun drawAllControlPoints() {
        if (simulating) return
        for (controlPoint in path.controlPoints) {
            gc.stroke = when {
                controlPoint.isSelected -> Color.rgb(0, 255, 255)
                else -> Color.rgb(255, 255, 0)
            }
            drawArrowForPose(ref, gc, controlPoint.pose)
        }
    }
}