package tech.tohkatsu.cansat.graph

import org.lwjgl.opengl.GL11

object Graphics {

    inline fun begin(i: Int, block: ()->Unit) {

        GL11.glBegin(i)
        block()
        GL11.glEnd()
    }

}