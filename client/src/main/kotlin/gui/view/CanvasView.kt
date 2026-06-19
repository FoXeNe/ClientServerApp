package gui.view

import gui.AppState
import gui.LocaleManager
import javafx.animation.AnimationTimer
import javafx.application.Platform
import javafx.collections.ListChangeListener
import javafx.geometry.Insets
import javafx.geometry.VPos
import javafx.scene.Cursor
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.Label
import javafx.scene.effect.DropShadow
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.paint.CycleMethod
import javafx.scene.paint.LinearGradient
import javafx.scene.paint.RadialGradient
import javafx.scene.paint.Stop
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.scene.text.TextAlignment
import model.Product
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

class CanvasView(
    private val onEditRequest: (Product) -> Unit,
) : StackPane() {
    private val canvas = Canvas()
    private val hint = Label(LocaleManager.getString("msg.collection_empty"))

    private data class Anim(
        var product: Product,
        val color: Color,
        var scale: Double = 0.0,
        var phase: Phase = Phase.APPEARING,
        var age: Double = 0.0, // frames since born, for glow pulse
    ) { enum class Phase { APPEARING, STABLE, DISAPPEARING } }

    private val animations = mutableMapOf<Long, Anim>()
    private val colorPalette = listOf(
        Color.web("#E53935"), Color.web("#1E88E5"), Color.web("#43A047"),
        Color.web("#FB8C00"), Color.web("#8E24AA"), Color.web("#00ACC1"),
        Color.web("#F4511E"), Color.web("#546E7A"),
    )
    private val ownerColors = mutableMapOf<String, Color>()
    private var colorIndex = 0
    private var hoveredId: Long? = null
    private var mouseX = 0.0; private var mouseY = 0.0
    private var frameCount = 0L

    private val timer = object : AnimationTimer() {
        override fun handle(now: Long) { draw() }
    }

    init {
        canvas.widthProperty().bind(widthProperty())
        canvas.heightProperty().bind(heightProperty())
        canvas.widthProperty().addListener { _, _, _ -> draw() }
        canvas.heightProperty().addListener { _, _, _ -> draw() }

        hint.style = "-fx-font-size: 14px; -fx-text-fill: #606080;"

        children.addAll(canvas, hint)
        StackPane.setMargin(hint, Insets(10.0))

        canvas.setOnMouseClicked { e ->
            val hit = hitTest(e.x, e.y)
            if (hit != null) showProductInfo(hit)
        }

        canvas.setOnMouseMoved { e ->
            mouseX = e.x; mouseY = e.y
            val hit = hitTest(e.x, e.y)
            hoveredId = hit?.id
            canvas.cursor = if (hit != null) Cursor.HAND else Cursor.DEFAULT
        }

        canvas.setOnMouseExited { hoveredId = null }

        AppState.products.addListener(ListChangeListener { updateAnimations() })
        timer.start()
    }

    private fun ownerColor(owner: String?): Color {
        val key = owner ?: ""
        return ownerColors.getOrPut(key) { colorPalette[colorIndex++ % colorPalette.size] }
    }

    fun updateAnimations() {
        val currentIds = AppState.products.map { it.id }.toSet()
        for (product in AppState.products) {
            val existing = animations[product.id]
            if (existing == null) {
                animations[product.id] = Anim(product, ownerColor(product.owner), 0.0, Anim.Phase.APPEARING)
            } else if (existing.phase == Anim.Phase.DISAPPEARING) {
                existing.phase = Anim.Phase.APPEARING; existing.product = product
            } else {
                existing.product = product
            }
        }
        for (id in animations.keys.toList()) {
            if (id !in currentIds) animations[id]?.phase = Anim.Phase.DISAPPEARING
        }
        Platform.runLater { hint.isVisible = AppState.products.isEmpty() }
    }

    private fun draw() {
        val gc = canvas.graphicsContext2D
        val w = canvas.width; val h = canvas.height
        if (w <= 0 || h <= 0) return
        frameCount++

        drawBackground(gc, w, h)

        val stable = animations.values.filter { it.phase != Anim.Phase.DISAPPEARING || it.scale > 0 }
        if (stable.isEmpty()) return

        val products = stable.map { it.product }
        val xs = products.map { it.coordinates.x.toDouble() }
        val ys = products.map { it.coordinates.y.toDouble() }
        val minX = xs.minOrNull() ?: 0.0; val maxX = xs.maxOrNull() ?: 0.0
        val minY = ys.minOrNull() ?: 0.0; val maxY = ys.maxOrNull() ?: 0.0

        val padL = 64.0; val padR = 24.0; val padT = 24.0; val padB = 54.0
        val rangeX = max(maxX - minX, 1.0); val rangeY = max(maxY - minY, 1.0)
        fun mapX(x: Double) = padL + (x - minX) / rangeX * (w - padL - padR)
        fun mapY(y: Double) = padT + (1.0 - (y - minY) / rangeY) * (h - padT - padB)

        drawGrid(gc, w, h, minX, maxX, minY, maxY, padL, padR, padT, padB)
        drawAxes(gc, w, h, minX, maxX, minY, maxY, padL, padR, padT, padB)

        // advance animations
        for (anim in stable) {
            anim.age++
            when (anim.phase) {
                Anim.Phase.APPEARING -> {
                    anim.scale = min(1.0, anim.scale + 0.05)
                    if (anim.scale >= 1.0) anim.phase = Anim.Phase.STABLE
                }
                Anim.Phase.DISAPPEARING -> anim.scale = max(0.0, anim.scale - 0.05)
                Anim.Phase.STABLE -> anim.scale = 1.0
            }
        }

        // draw non-hovered first, hovered last (on top)
        val hovered = stable.filter { it.product.id == hoveredId }
        val rest = stable.filter { it.product.id != hoveredId }

        for (anim in rest) {
            val cx = mapX(anim.product.coordinates.x.toDouble())
            val cy = mapY(anim.product.coordinates.y.toDouble())
            val r = bubbleRadius(anim.product) * anim.scale
            if (r < 0.5) continue
            drawBubble(gc, cx, cy, r, anim.color, anim.product, hovered = false, anim.age)
        }
        for (anim in hovered) {
            val cx = mapX(anim.product.coordinates.x.toDouble())
            val cy = mapY(anim.product.coordinates.y.toDouble())
            val r = bubbleRadius(anim.product) * anim.scale
            if (r < 0.5) continue
            drawBubble(gc, cx, cy, r, anim.color, anim.product, hovered = true, anim.age)
            drawTooltip(gc, cx, cy - r - 8, anim.product, w, h)
        }

        drawLegend(gc, w)
        animations.entries.removeAll { it.value.phase == Anim.Phase.DISAPPEARING && it.value.scale <= 0 }
    }

    private fun bubbleRadius(p: Product) =
        (12.0 + ln(p.price.toDouble() + 1) * 4.5).coerceIn(14.0, 52.0)

    private fun drawBackground(gc: GraphicsContext, w: Double, h: Double) {
        gc.fill = LinearGradient(0.0, 0.0, 0.0, h, false, CycleMethod.NO_CYCLE,
            Stop(0.0, Color.web("#EEF1FB")),
            Stop(1.0, Color.web("#E4E9F6")),
        )
        gc.fillRect(0.0, 0.0, w, h)
    }

    private fun drawGrid(
        gc: GraphicsContext, w: Double, h: Double,
        minX: Double, maxX: Double, minY: Double, maxY: Double,
        padL: Double, padR: Double, padT: Double, padB: Double,
    ) {
        gc.save()
        gc.stroke = Color.web("#BFC8E0", 0.8)
        gc.lineWidth = 0.6
        gc.setLineDashes(5.0, 5.0)
        val n = 6
        for (i in 0..n) {
            val t = i.toDouble() / n
            gc.strokeLine(padL + t * (w - padL - padR), padT, padL + t * (w - padL - padR), h - padB)
            gc.strokeLine(padL, padT + t * (h - padT - padB), w - padR, padT + t * (h - padT - padB))
        }
        gc.setLineDashes()
        gc.restore()
    }

    private fun drawAxes(
        gc: GraphicsContext, w: Double, h: Double,
        minX: Double, maxX: Double, minY: Double, maxY: Double,
        padL: Double, padR: Double, padT: Double, padB: Double,
    ) {
        val axisY = h - padB; val axisX = padL
        gc.save()
        gc.stroke = Color.web("#7080A0")
        gc.lineWidth = 1.5
        gc.strokeLine(axisX, padT, axisX, axisY)
        gc.strokeLine(axisX, axisY, w - padR, axisY)
        // arrows
        gc.fill = Color.web("#7080A0")
        gc.fillPolygon(doubleArrayOf(w - padR, w - padR - 6, w - padR - 6), doubleArrayOf(axisY, axisY - 4, axisY + 4), 3)
        gc.fillPolygon(doubleArrayOf(axisX, axisX - 4, axisX + 4), doubleArrayOf(padT, padT + 7, padT + 7), 3)

        gc.font = Font.font("System", 10.0)
        gc.fill = Color.web("#5060A0")
        val n = 6
        gc.textAlign = TextAlignment.CENTER; gc.textBaseline = VPos.TOP
        for (i in 0..n) {
            val t = i.toDouble() / n
            val px = axisX + t * (w - padL - padR)
            gc.fillText(fmtVal(minX + t * (maxX - minX)), px, axisY + 5)
            // tick
            gc.stroke = Color.web("#8090B0"); gc.lineWidth = 1.0
            gc.strokeLine(px, axisY - 3, px, axisY + 3)
        }
        gc.textAlign = TextAlignment.RIGHT; gc.textBaseline = VPos.CENTER
        for (i in 0..n) {
            val t = i.toDouble() / n
            val py = padT + (1.0 - t) * (h - padT - padB)
            gc.fill = Color.web("#5060A0")
            gc.fillText(fmtVal(minY + t * (maxY - minY)), axisX - 5, py)
            gc.stroke = Color.web("#8090B0"); gc.lineWidth = 1.0
            gc.strokeLine(axisX - 3, py, axisX + 3, py)
        }
        // axis labels
        gc.font = Font.font("System", FontWeight.BOLD, 11.0)
        gc.fill = Color.web("#404880")
        gc.textAlign = TextAlignment.CENTER; gc.textBaseline = VPos.BOTTOM
        gc.fillText("X", w - padR + 14, axisY + 4)
        gc.textAlign = TextAlignment.LEFT; gc.textBaseline = VPos.TOP
        gc.fillText("Y", axisX - 4, padT - 14)
        gc.restore()
    }

    private fun fmtVal(v: Double) =
        if (v == v.toLong().toDouble()) v.toLong().toString() else "%.1f".format(v)

    private fun drawBubble(
        gc: GraphicsContext, cx: Double, cy: Double, r: Double,
        color: Color, product: Product, hovered: Boolean, age: Double,
    ) {
        val boost = if (hovered) 1.12 else 1.0
        val er = r * boost

        // new-object pulse glow (fades out over 60 frames)
        val glowAlpha = if (age < 60) (1.0 - age / 60.0) * 0.55 else 0.0
        if (glowAlpha > 0.01) {
            val pulse = 1.0 + 0.18 * sin(age * 0.18) * (1.0 - age / 60.0)
            gc.fill = color.deriveColor(0.0, 1.0, 1.2, glowAlpha * 0.5)
            val gr = er * pulse * 1.55
            gc.fillOval(cx - gr, cy - gr, gr * 2, gr * 2)
        }

        // hover ring
        if (hovered) {
            val ring = er + 5 + 2 * sin(frameCount * 0.08)
            gc.stroke = color.deriveColor(0.0, 0.8, 1.3, 0.7)
            gc.lineWidth = 2.0
            gc.setLineDashes(5.0, 4.0)
            gc.strokeOval(cx - ring, cy - ring, ring * 2, ring * 2)
            gc.setLineDashes()
        }

        // soft shadow
        gc.save()
        for (s in 3 downTo 1) {
            gc.fill = Color.color(0.1, 0.15, 0.3, 0.06)
            gc.fillOval(cx - er + s * 1.5, cy - er + s * 2.0, er * 2, er * 2)
        }
        gc.restore()

        // radial gradient body
        gc.fill = RadialGradient(
            0.0, 0.0, cx - er * 0.2, cy - er * 0.3, er * 1.1,
            false, CycleMethod.NO_CYCLE,
            Stop(0.0, color.deriveColor(0.0, 0.7, 1.5, 0.95)),
            Stop(0.6, color.deriveColor(0.0, 1.0, 1.0, 0.95)),
            Stop(1.0, color.deriveColor(0.0, 1.3, 0.7, 0.95)),
        )
        gc.fillOval(cx - er, cy - er, er * 2, er * 2)

        // border
        gc.stroke = color.deriveColor(0.0, 1.1, 0.55, if (hovered) 1.0 else 0.75)
        gc.lineWidth = if (hovered) 2.2 else 1.5
        gc.strokeOval(cx - er, cy - er, er * 2, er * 2)

        // specular glare (top-left highlight)
        if (er > 8) {
            gc.fill = RadialGradient(
                0.0, 0.0, cx - er * 0.35, cy - er * 0.4, er * 0.6,
                false, CycleMethod.NO_CYCLE,
                Stop(0.0, Color.color(1.0, 1.0, 1.0, 0.5)),
                Stop(1.0, Color.color(1.0, 1.0, 1.0, 0.0)),
            )
            gc.fillOval(cx - er * 0.9, cy - er * 0.9, er * 1.2, er * 0.9)
        }

        // label
        if (er > 13) {
            val fs = (er * 0.38).coerceIn(9.0, 14.0)
            gc.fill = Color.color(1.0, 1.0, 1.0, 0.95)
            gc.font = Font.font("System", FontWeight.BOLD, fs)
            gc.textAlign = TextAlignment.CENTER
            gc.textBaseline = VPos.CENTER
            val maxChars = if (er > 30) 8 else if (er > 20) 6 else 4
            gc.fillText(product.name.take(maxChars), cx, cy)
        }
    }

    private fun drawTooltip(gc: GraphicsContext, cx: Double, topY: Double, product: Product, w: Double, h: Double) {
        val lines = listOf(
            "${product.name}  #${product.id}",
            "X=${product.coordinates.x}  Y=${product.coordinates.y}",
            LocaleManager.getString("col.price") + ": ${product.price}",
            LocaleManager.getString("col.owner") + ": ${product.owner ?: "-"}",
        )
        val padH = 8.0; val padV = 5.0; val lineH = 16.0
        val tw = 180.0; val th = lines.size * lineH + padV * 2
        var tx = cx - tw / 2
        var ty = topY - th - 6

        tx = tx.coerceIn(4.0, w - tw - 4)
        ty = ty.coerceIn(4.0, h - th - 4)

        gc.save()
        gc.fill = Color.color(0.1, 0.12, 0.22, 0.88)
        roundRect(gc, tx, ty, tw, th, 8.0)
        gc.fill = Color.web("#B0BAE0", 0.6)
        gc.lineWidth = 1.0
        roundRectStroke(gc, tx, ty, tw, th, 8.0)

        gc.fill = Color.WHITE
        gc.font = Font.font("System", FontWeight.BOLD, 11.0)
        gc.textAlign = TextAlignment.LEFT
        gc.textBaseline = VPos.TOP
        gc.fillText(lines[0], tx + padH, ty + padV)
        gc.font = Font.font("System", 10.0)
        for (i in 1 until lines.size) {
            gc.fill = Color.web("#C8D4F0")
            gc.fillText(lines[i], tx + padH, ty + padV + i * lineH)
        }
        gc.restore()
    }

    private fun roundRect(gc: GraphicsContext, x: Double, y: Double, w: Double, h: Double, r: Double) {
        gc.beginPath()
        gc.moveTo(x + r, y)
        gc.lineTo(x + w - r, y); gc.arcTo(x + w, y, x + w, y + r, r)
        gc.lineTo(x + w, y + h - r); gc.arcTo(x + w, y + h, x + w - r, y + h, r)
        gc.lineTo(x + r, y + h); gc.arcTo(x, y + h, x, y + h - r, r)
        gc.lineTo(x, y + r); gc.arcTo(x, y, x + r, y, r)
        gc.closePath(); gc.fill()
    }

    private fun roundRectStroke(gc: GraphicsContext, x: Double, y: Double, w: Double, h: Double, r: Double) {
        gc.beginPath()
        gc.moveTo(x + r, y)
        gc.lineTo(x + w - r, y); gc.arcTo(x + w, y, x + w, y + r, r)
        gc.lineTo(x + w, y + h - r); gc.arcTo(x + w, y + h, x + w - r, y + h, r)
        gc.lineTo(x + r, y + h); gc.arcTo(x, y + h, x, y + h - r, r)
        gc.lineTo(x, y + r); gc.arcTo(x, y, x + r, y, r)
        gc.closePath(); gc.stroke()
    }

    private fun drawLegend(gc: GraphicsContext, w: Double) {
        if (ownerColors.isEmpty()) return
        val entryH = 20.0; val padX = 14.0; val padY = 14.0
        val legendW = 140.0; val legendH = ownerColors.size * entryH + 12.0
        val lx = w - legendW - padX; val ly = padY

        gc.save()
        gc.fill = Color.color(0.95, 0.96, 1.0, 0.82)
        roundRect(gc, lx, ly, legendW, legendH, 10.0)
        gc.stroke = Color.web("#A8B4D0", 0.7); gc.lineWidth = 1.0
        roundRectStroke(gc, lx, ly, legendW, legendH, 10.0)

        gc.font = Font.font("System", FontWeight.BOLD, 10.0)
        gc.fill = Color.web("#404870")
        gc.textAlign = TextAlignment.LEFT; gc.textBaseline = VPos.CENTER
        ownerColors.entries.forEachIndexed { i, (owner, color) ->
            val ey = ly + 6 + i * entryH + entryH / 2
            gc.fill = color
            gc.fillOval(lx + 9, ey - 6, 12.0, 12.0)
            gc.stroke = color.darker(); gc.lineWidth = 1.0
            gc.strokeOval(lx + 9, ey - 6, 12.0, 12.0)
            gc.fill = Color.web("#303060")
            gc.fillText((if (owner.isBlank()) "-" else owner).take(13), lx + 26, ey)
        }
        gc.restore()
    }

    private fun hitTest(mouseX: Double, mouseY: Double): Product? {
        val w = canvas.width; val h = canvas.height
        val stable = animations.values.filter { it.phase != Anim.Phase.DISAPPEARING || it.scale > 0 }
        if (stable.isEmpty()) return null

        val products = stable.map { it.product }
        val xs = products.map { it.coordinates.x.toDouble() }
        val ys = products.map { it.coordinates.y.toDouble() }
        val minX = xs.minOrNull() ?: 0.0; val maxX = xs.maxOrNull() ?: 0.0
        val minY = ys.minOrNull() ?: 0.0; val maxY = ys.maxOrNull() ?: 0.0
        val rangeX = max(maxX - minX, 1.0); val rangeY = max(maxY - minY, 1.0)
        val padL = 64.0; val padR = 24.0; val padT = 24.0; val padB = 54.0
        fun mapX(x: Double) = padL + (x - minX) / rangeX * (w - padL - padR)
        fun mapY(y: Double) = padT + (1.0 - (y - minY) / rangeY) * (h - padT - padB)

        return stable.lastOrNull { anim ->
            val cx = mapX(anim.product.coordinates.x.toDouble())
            val cy = mapY(anim.product.coordinates.y.toDouble())
            val r = bubbleRadius(anim.product) * anim.scale * 1.12
            val dx = mouseX - cx; val dy = mouseY - cy
            dx * dx + dy * dy <= r * r
        }?.product
    }

    private fun showProductInfo(product: Product) {
        val fmt = LocaleManager.getNumberFormat()
        val dateFmt = LocaleManager.getDateFormat()
        val info = buildString {
            appendLine("ID: ${product.id}")
            appendLine("${LocaleManager.getString("col.name")}: ${product.name}")
            appendLine("X: ${fmt.format(product.coordinates.x)}, Y: ${fmt.format(product.coordinates.y)}")
            appendLine("${LocaleManager.getString("col.price")}: ${fmt.format(product.price)}")
            appendLine("${LocaleManager.getString("col.unit")}: ${product.unitOfMeasure?.name ?: "-"}")
            appendLine("${LocaleManager.getString("col.org_name")}: ${product.manufacturer.name}")
            appendLine("${LocaleManager.getString("col.org_fullname")}: ${product.manufacturer.fullName}")
            appendLine("${LocaleManager.getString("col.org_employees")}: ${fmt.format(product.manufacturer.employeesCount)}")
            appendLine("${LocaleManager.getString("col.date")}: ${dateFmt.format(java.util.Date.from(product.creationDate.toInstant()))}")
            appendLine("${LocaleManager.getString("col.owner")}: ${product.owner ?: "-"}")
        }
        val alert = Alert(Alert.AlertType.INFORMATION)
        alert.title = LocaleManager.getString("info.title")
        alert.headerText = product.name
        alert.contentText = info.trim()

        if (product.owner == AppState.currentUser) {
            val editBtn = ButtonType(LocaleManager.getString("cmd.update"))
            alert.buttonTypes.setAll(editBtn, ButtonType.CLOSE)
            val result = alert.showAndWait()
            if (result.isPresent && result.get() == editBtn) onEditRequest(product)
        } else {
            alert.showAndWait()
        }
    }

    fun stop() { timer.stop() }
}
