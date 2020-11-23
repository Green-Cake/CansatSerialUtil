package tech.tohkatsu.cansat

import com.google.gson.Gson
import java.nio.file.Files
import java.nio.file.StandardOpenOption

data class Config(
    val colorPromptBackground: String, val colorPromptText: String,
    val colorPromptWarningText: String, val font_size: Double
) {

    @ExperimentalUnsignedTypes
    companion object {



    }

}