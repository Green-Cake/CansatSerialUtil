package tech.tohkatsu.cansat.graph

import com.sun.javafx.scene.PerspectiveCameraHelper
import org.lwjgl.Version
import org.lwjgl.glfw.Callbacks
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import tech.tohkatsu.cansat.CansatSerialUtil
import tech.tohkatsu.cansat.util.Coordinate
import kotlin.system.exitProcess

@ExperimentalUnsignedTypes
class AppGraph {

    companion object {

        const val INITIAL_WIDTH = 600
        const val INITIAL_HEIGHT = 600

        const val FIELD_SCALE = 100.0f

    }

    private var window = -1L // -1 is temporary value

    var rotateYaw = -12f
    var rotatePitch = 10f

    var offsetX = -0.8f
    var offsetY = -0.8f

    /**
     * Notify that this application must be closed.
     * After this method called, the application's main loop stops.
     */
    fun close() = GLFW.glfwSetWindowShouldClose(window, true)

    /**
     * make [AppGraph] start to run.
     */
    fun run() {

        println("LWJGL version: ${Version.getVersion()}")

        init()
        loop()

        Callbacks.glfwFreeCallbacks(window)
        GLFW.glfwDestroyWindow(window)

        GLFW.glfwTerminate()
        GLFW.glfwSetErrorCallback(null)?.free()

    }

    private fun init() {

        GLFWErrorCallback.createPrint(System.err).set()

        if(!GLFW.glfwInit()) {
            exitProcess(1)
        }

        GLFW.glfwDefaultWindowHints()
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE)
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE)

        window = GLFW.glfwCreateWindow(INITIAL_WIDTH, INITIAL_HEIGHT, "Tohkatsu Fantasy", MemoryUtil.NULL, MemoryUtil.NULL)
        if(window == MemoryUtil.NULL) {
            exitProcess(1)
        }

        GLFW.glfwSetKeyCallback(window) { w, key, _, action, _ ->

            if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_RELEASE)
                GLFW.glfwSetWindowShouldClose(w, true)

        }

        MemoryStack.stackPush().use {

            val pWidth = it.mallocInt(1)
            val pHeight = it.mallocInt(1)

            GLFW.glfwGetWindowSize(window, pWidth, pHeight)
            val vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor())!!

            GLFW.glfwSetWindowPos(
                window,
                vidmode.width() - pWidth[0],
                vidmode.height() - pHeight[0]
            )

        }

        GLFW.glfwMakeContextCurrent(window)

        GLFW.glfwSwapInterval(1)
        GLFW.glfwShowWindow(window)

    }

    private fun loop() {

        GL.createCapabilities()

        GL11.glClearColor(0f, 0f, 0f, 0f)

        while(!GLFW.glfwWindowShouldClose(window)) {

            render()

            GLFW.glfwSwapBuffers(window)

            GLFW.glfwPollEvents()

        }

    }

    @Suppress("UNREACHABLE_CODE")
    private fun render() {

        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)

        GL11.glMatrixMode(GL11.GL_PROJECTION)
        GL11.glLoadIdentity()

        GL11.glMatrixMode(GL11.GL_MODELVIEW)
        Graphics.onMatrix {

            GL11.glRotatef(rotateYaw, 0f, 1f, 0f)
            GL11.glRotatef(rotatePitch, 1f, 0f, 0f)
            GL11.glTranslatef(offsetX, offsetY, 0f)

            GL11.glColor3f(1f, 0f, 0f)
            Graphics.begin(GL11.GL_LINE_STRIP) {//x
                GL11.glVertex3f(0f, 0f, 0f)
                GL11.glVertex3f(1f, 0f, 0f)
            }

            GL11.glColor3f(0f, 1f, 0f)
            Graphics.begin(GL11.GL_LINE_STRIP) {//y
                GL11.glVertex3f(0f, 0f, 0f)
                GL11.glVertex3f(0f, 1f, 0f)
            }

            GL11.glColor3f(0f, 0f, 1f)
            Graphics.begin(GL11.GL_LINE_STRIP) {//z
                GL11.glVertex3f(0f, 0f, 0f)
                GL11.glVertex3f(0f, 0f, -1f)
            }

        }

        return

        //UNIMPLEMENTED...
        //*

        val data = ArrayList(CansatSerialUtil.instance.parsedDataList).toList()

        //unimplemented analyzing the accelerator and gyro

        val points: List<Coordinate> = TODO("unimplemented") //coordinates for specified sec

        val pMin = minOf(points.minOf { it.x }, points.minOf { it.y }, points.minOf { it.z })
        val pMax = minOf(points.maxOf { it.x }, points.maxOf { it.y }, points.maxOf { it.z })

        val ratio = (pMax - pMin) / FIELD_SCALE

        //draw the graph
        GL11.glColor3f(1f, 1f, 1f)//white
        Graphics.begin(GL11.GL_LINE_STRIP) {

            for(p in points) {

                GL11.glColor3f(1f, 1f, 1f)
                GL11.glVertex3f((p.x-pMin)*ratio, (p.y-pMin)*ratio, (p.z-pMin)*ratio)

            }

        }

        //*/

    }

}