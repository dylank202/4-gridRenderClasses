
package SlRenderer;

import static csc133.spot.*;
import org.lwjgl.glfw.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.*;

public class slWindow {
    private static long myOglWin = NULL;

    private static GLFWKeyCallback keyCallback;
    private static GLFWFramebufferSizeCallback fbCallback;
    private static String winTitle = "CSC 133";

    private static void slWindow() {}

    public static long get(int myWidth, int myHeight, int myPosX, int myPosY) {
        if (myOglWin == NULL || winWidth != myWidth || winHeight != myHeight || winPosX != myPosX || winPosY != myPosY) {
            winWidth = myWidth;
            winHeight = myHeight;
            winPosX = myPosX;
            winPosY = myPosY;
            GLFWErrorCallback errorCallback;
            if (myOglWin == NULL) {
                glfwSetErrorCallback(errorCallback =
                        GLFWErrorCallback.createPrint(System.err));
                if (!glfwInit()) {
                    throw new IllegalStateException("Unable to initialize GLFW");
                }
                glfwDefaultWindowHints();
                glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
                glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
                glfwWindowHint(GLFW_SAMPLES, 8);
            }
            myOglWin = glfwCreateWindow(winWidth, winHeight, winTitle, NULL, NULL);
            glfwSetKeyCallback(myOglWin, keyCallback = new GLFWKeyCallback() {
                @Override
                public void invoke(long window, int key, int scancode, int action, int
                        mods) {
                    if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                        glfwSetWindowShouldClose(window, true);
                }
            });
            if (myOglWin == NULL) {
                throw new RuntimeException("Failed to create the GLFW window");
            }

            glfwSetFramebufferSizeCallback(myOglWin, fbCallback = new
                    GLFWFramebufferSizeCallback() {
                        @Override
                        public void invoke(long window, int w, int h) {
                            if (w > 0 && h > 0) {
                                winWidth = w;
                                winHeight = h;
                            } }
                    });
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            glfwSetWindowPos(myOglWin, winPosX, winPosY);
            glfwMakeContextCurrent(myOglWin);
            int VSYNC_INTERVAL = 1;
            glfwSwapInterval(VSYNC_INTERVAL);
            glfwShowWindow(myOglWin);
        }
        return myOglWin;
    }

    public static void destroyOglWindow() {
        glfwDestroyWindow(myOglWin);
        keyCallback.free();
        fbCallback.free();
    }

    public static long get() {
        return get(winWidth, winHeight, winPosX, winPosY);
    }
}
