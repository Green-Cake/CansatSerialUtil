package tech.tohkatsu.cansat

data class Config(
    val colorPromptBackground: String, val colorPromptText: String,
    val colorPromptWarningText: String, val font_size: Double,
    val doesShowAllRawData: Boolean
) {

    @ExperimentalUnsignedTypes
    companion object {



    }

}