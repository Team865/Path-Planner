package ca.warp7.pathplotter.ui

import ca.warp7.pathplotter.util.f2
import edu.wpi.first.networktables.ConnectionNotification
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.*
import javafx.scene.paint.Color
import org.kordamp.ikonli.Ikon
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign.*
import kotlin.math.abs

class InfoBar {

    private fun label(icon: Ikon): Label {
        val label = Label()
        label.graphic = FontIcon.of(icon, 18, Color.valueOf("#5a8ade"))
        label.style = "-fx-font-size:16"
        return label
    }

    private val time = label(MDI_TIMER)
    private val dist = label(MDI_RULER)
    private val curve = label(MDI_VECTOR_CURVE)
    private val vel = label(MDI_SPEEDOMETER)

    private val red = Background(BackgroundFill(Color.RED, CornerRadii(6.0), null))
    private val green = Background(BackgroundFill(Color.GREEN, CornerRadii(6.0), null))

    private val robotStatus = Pane().apply {
        prefWidth = 12.0
        prefHeight = 12.0
        maxHeight = 12.0
        background = red
    }

    private val robotLabel = Label("Robot Disconnected").apply {
        this.style = "-fx-font-size: 16"
    }

    private val robotStatusCont = HBox(robotStatus, robotLabel).apply {
        this.spacing = 4.0
        alignment = Pos.CENTER
    }

    val container = HBox()

    init {
        setTime(0.0, 3.5)
        setDist(0.0)
        setVel(0.0, 0.0, 0.0, 0.0)
        setCurve(0.1, 0.1, 3.5)
        container.children.addAll(time, dist, vel, curve, robotStatusCont)
        container.spacing = 12.0
        container.alignment = Pos.CENTER
        container.padding = Insets(4.0, 12.0, 4.0, 12.0)
        container.style = "-fx-background-color: #eee"
    }

    fun setTime(current: Double, total: Double) {
        time.text = "${current.f2}/${total.f2}s"
    }

    fun setDist(total: Double) {
        dist.text = "${total.f2}m"
    }

    fun setCurve(k: Double, dk: Double, sdk2: Double) {
        curve.text = "κ=${f(k)}  κ'=${f(dk)}  Σ(κ')²=${sdk2.f2}"
    }

    fun setVel(v: Double, w: Double, dv: Double, dw: Double) {
        vel.text = "v=${f(v)}  ω=${f(w)}  v'=${f(dv)}  ω'=${f(dw)}"
    }

    private fun f(d: Double): String {
        val res = abs(d).f2
        if (res.length > 4) return res.substring(0, 4)
        return res
    }

    fun setConnection(conn: ConnectionNotification) {
        if (conn.connected) {
            robotStatus.background = green
            robotLabel.text = "${conn.conn.remote_ip}@${conn.conn.remote_port}"
        } else {
            robotStatus.background = red
            robotLabel.text = "Robot Disconnected"
        }
    }
}