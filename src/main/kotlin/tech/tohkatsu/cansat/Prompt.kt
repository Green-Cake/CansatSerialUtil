package tech.tohkatsu.cansat

import javafx.geometry.Insets
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.CornerRadii
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.Text
import java.util.*

@ExperimentalUnsignedTypes
class Prompt(private val app: CansatSerialUtil) {

    var latestFontSize = 15.0
    var latestNormalFill = Color.WHITE!!
    var latestWarningFill = Color.RED!!

    private val flow get() = app.controllerMain.flow_prompt

    private fun getPrefixInfo(calendar: Calendar = Calendar.getInstance()) =
            String.format("[%tH:%tM:%tS:%tL] ",
                calendar, calendar, calendar, calendar
            )

    fun updateFontSize(size: Double) {

        if(size == latestFontSize)
            return

        latestFontSize = size
        flow.children.filterIsInstance<Text>().forEach {
            it.font = Font.font(it.font.family, latestFontSize)
        }

    }

    fun updateColor(background: Color, normal: Color, warning: Color) {

        if(flow.background?.fills?.firstOrNull()?.fill != background) {
            flow.background = Background(BackgroundFill(background, CornerRadii.EMPTY, Insets.EMPTY))
            kotlin.io.println("background color updated.")
        }

        if(latestNormalFill != normal) {
            latestNormalFill = normal
            flow.children.filter { it is Text && CansatSerialUtil.STYLE_NORMAL in it.styleClass }.forEach {
                (it as Text).fill = latestNormalFill
            }
        }

        if(latestWarningFill != warning) {
            latestWarningFill = warning
            flow.children.filter { it is Text && CansatSerialUtil.STYLE_WARNING in it.styleClass }.forEach {
                (it as Text).fill = latestWarningFill
            }
        }

    }

    fun print(text: String, style: String) {

        flow.children += Text(getPrefixInfo()+text).apply {
            fill = getFillFromStyle(style)
            font = Font.font(latestFontSize)
            styleClass += style
        }

    }

    fun getFillFromStyle(style: String) = when(style) {
        CansatSerialUtil.STYLE_NORMAL -> latestNormalFill
        CansatSerialUtil.STYLE_WARNING -> latestWarningFill
        else -> Color.GREEN
    }

    fun warning(text: String) = print(text, CansatSerialUtil.STYLE_WARNING)

    fun print(text: String) = print(text, CansatSerialUtil.STYLE_NORMAL)

    fun warningln(text: String) = warning(text+"\n")

    fun println(text: String) = print(text+"\n")

}