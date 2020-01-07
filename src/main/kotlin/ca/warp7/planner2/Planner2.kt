package ca.warp7.planner2

import ca.warp7.planner2.fx.combo
import ca.warp7.planner2.fx.menuItem
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
import javafx.scene.input.MouseEvent
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Stage

class Planner2  {

    val stage = Stage()

    val menuBar = MenuBar()

    val canvas = Canvas()

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
        left = canvas
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
        stage.title = "FRC Drive Trajectory Planner"
        stage.width = 1000.0
        stage.height = 600.0
        stage.icons.add(Image(Planner2::class.java.getResourceAsStream("/icon.png")))
    }

    val dialogs = Dialogs(stage)
    val gc: GraphicsContext = canvas.graphicsContext2D

    var controlDown = false

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
            menuItem("Generate Commons-based Command", null) {
//                val s = toCommonsCommand(state)
//                dialogs.showTextBox("Command", s)
            },
            MenuItem("Generate Java Command"),
            MenuItem("Generate WPILib function"),
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

    private val pointMenu = Menu(
            "Control Point",
            null,
            menuItem("Rotate 1 degree counter-clockwise", combo(KeyCode.Q)) {
                transformSelected(0.0, 0.0, 1.0, false)
            },
            menuItem("Rotate 1 degree clockwise", combo(KeyCode.W)) {
                transformSelected(0.0, 0.0, -1.0, false)
            },
            menuItem("Move up 0.01 metres", combo(KeyCode.UP)) {
                transformSelected(0.01, 0.0, 0.0, true)
            },
            menuItem("Move down 0.01 metres", combo(KeyCode.DOWN)) {
                transformSelected(-0.01, 0.0, 0.0, true)
            },
            menuItem("Move left 0.01 metres", combo(KeyCode.LEFT)) {
                transformSelected(0.0, 0.01, 0.0, true)
            },
            menuItem("Move right 0.01 metres", combo(KeyCode.RIGHT)) {
                transformSelected(0.0, -0.01, 0.0, true)
            },
            menuItem("Move forward 0.01 metres", combo(KeyCode.UP, shift = true)) {
                transformSelected(0.01, 0.0, 0.0, false)
            },
            menuItem("Move backward 0.01 metres", combo(KeyCode.DOWN, shift = true)) {
                transformSelected(-0.01, 0.0, 0.0, false)
            },
            menuItem("Move left-normal 0.01 metres", combo(KeyCode.LEFT, shift = true)) {
                transformSelected(0.0, 0.01, 0.0, false)
            },
            menuItem("Move right-normal 0.01 metres", combo(KeyCode.RIGHT, shift = true)) {
                transformSelected(0.0, -0.01, 0.0, false)
            }
    )

    private val viewMenu = Menu("View", null,
            MenuItem("Resize Canvas to Window"),
            menuItem("Start/Pause Simulation", combo(KeyCode.SPACE)) { onSpacePressed() },
            menuItem("Stop Simulation", combo(KeyCode.DIGIT0)) { stopSimulation() }
    )

    init {
        menuBar.menus.addAll(
                fileMenu,
                editMenu,
                pointMenu,
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
//        val delta = Translation2D(x, y)
//        val rotation = Rotation2D.fromDegrees(theta)
//        for (controlPoint in state.controlPoints) {
//            if (controlPoint.isSelected) {
//                val oldPose = controlPoint.pose
//                val offset = if (fieldRelative) delta else delta.rotate(oldPose.rotation)
//                val newPose = Pose2D(snap(oldPose.translation + offset), oldPose.rotation + rotation)
//                controlPoint.pose = newPose
//                val mutableWaypoints = controlPoint.segment.waypoints.toMutableList()
//                mutableWaypoints[controlPoint.indexInSegment] = newPose
//                controlPoint.segment.waypoints = mutableWaypoints
//            }
//        }
//        regenerate()
    }
}