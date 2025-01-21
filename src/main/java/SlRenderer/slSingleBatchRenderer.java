package SlRenderer;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static csc133.spot.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

public class slSingleBatchRenderer {

    private static final int OGL_MATRIX_SIZE = 16;
    private static long window;

    // don't call glCreateProgram() here - we have no gl-context here
    private static int shader_program;
    private static Matrix4f viewProjMatrix = new Matrix4f();
    private static FloatBuffer myFloatBuffer = BufferUtils.createFloatBuffer(OGL_MATRIX_SIZE);
    private static int vpMatLocation = 0, renderColorLocation = 0;
    private static Vector3f VEC_RC =
            new Vector3f(0.0f, 0.7f, 0.6f); // "vector render color" for square
    private static Vector3f VEC_RC_DYN =
            new Vector3f(0.016f, 0.4f, 0.8f); // "vector render color" for square - dynamic (color changing)

    private static int maxRows = 20, maxCols = 18, length = 30, padding = 10;

    private static int gridWidth = (maxCols * length) + ((maxCols - 1) * padding),
            gridHeight = (maxRows * length) + ((maxRows - 1) * padding);
    private static int offset = ((winWidth / 2) - (gridWidth / 2)),
            offsetY = ((winHeight / 2) - (gridHeight / 2));

    // used for color changing
    private static boolean redUp = false, greenUp = false, blueUp = true, loopedR = false,
            loopedG = false, loopedB = true, firstLoop = true, firstDraw = true;

    public static void render() {
        window = SlRenderer.slWindow.get(winWidth, winHeight, winPosX, winPosY);
        try {
            renderLoop();
            SlRenderer.slWindow.destroyOglWindow();
        } finally {
            glfwTerminate();
            glfwSetErrorCallback(null).free();
        }
    } // void render()

    private static void renderLoop() {
        glfwPollEvents();
        initOpenGL();
        renderObjects();
        /* Process window messages in the main thread */
        while (!glfwWindowShouldClose(window)) {
            glfwWaitEvents();
        }
    } // void renderLoop()

    private static void initOpenGL() {
        GL.createCapabilities();
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glViewport(0, 0, winWidth, winHeight);
        float clearRed = 0.1f, clearGreen = 0.2f, clearBlue = 0.25f, clearAlpha = 1.0f;
        glClearColor(clearRed, clearGreen, clearBlue, clearAlpha);
        shader_program = glCreateProgram();
        int vs = glCreateShader(GL_VERTEX_SHADER);
        Matrix4f viewProjMatrix = new Matrix4f();
        String uniformVarName = "viewProjMatrix";
        glShaderSource(vs,
                "uniform mat4 viewProjMatrix;" +
                        "void main(void) {" +
                        " gl_Position = viewProjMatrix * gl_Vertex;" +
                        "}");
        glCompileShader(vs);
        glAttachShader(shader_program, vs);
        int fs = glCreateShader(GL_FRAGMENT_SHADER);
        String colorUniformVarName = "renderColorLocation";
        glShaderSource(fs,
                "uniform vec3 renderColorLocation;" + // Define a uniform variable for color
                        "void main(void) {" +
                        " gl_FragColor = vec4(renderColorLocation, 1.0);" + // Use the uniform color
                        "}");
        glCompileShader(fs);
        glAttachShader(shader_program, fs);
        glLinkProgram(shader_program);
        glUseProgram(shader_program);
        vpMatLocation = glGetUniformLocation(shader_program, uniformVarName);
        renderColorLocation = glGetUniformLocation(shader_program, colorUniformVarName);
        return;
    } // void initOpenGL()

    private static int[] getIndexArrayForSquares(int maxRows, int maxCols, int ips, int vps) {
        int[] indices = new int[maxRows * maxCols * ips];
        int vIndex = 0, myI = 0;

        while (myI < indices.length) {
            indices[myI++] = vIndex;
            indices[myI++] = vIndex+1;
            indices[myI++] = vIndex+2;

            indices[myI++] = vIndex;
            indices[myI++] = vIndex+2;
            indices[myI++] = vIndex+3;
            vIndex += vps;
        }
        return indices;
    }

    private static float[] getVertexArray(int maxRows, int maxCols, int winWidth, int winHeight,
                                          int myOffset, int myPadding, int myLength) {

        // vert per square, vertices per vertex
        int vps = 4, fpv = 2;
        float[] vertices = new float[maxRows * maxCols * vps * fpv];

        int xmin = myOffset, xmax = xmin + myLength;
        int yOffset = 0;
        if (winHeight >= monitorH) yOffset = ((winHeight - monitorH) + myOffset * 2);
        int ymax = winHeight - offsetY - yOffset, ymin = ymax - myLength;

        int index = 0;
        for (int i = 0; i < maxRows; i++) {
            for (int j = 0; j < maxCols; j++) {
                vertices[index++] = xmin;   // bottom left X
                vertices[index++] = ymin;   // bottom left Y

                vertices[index++] = xmax;   // bottom right X
                vertices[index++] = ymin;   // bottom right Y

                vertices[index++] = xmax;   // top right X
                vertices[index++] = ymax;   // top right Y

                vertices[index++] = xmin;   // top left X
                vertices[index++] = ymax;   // top left Y

                xmin = xmax + myPadding;
                xmax = xmin + myLength;
            }
            xmin = myOffset;
            xmax = xmin + myLength;
            ymax = ymin - myPadding;
            ymin = ymax - myLength;
        }
        return vertices;
    }

    // used to update the colors of the grid squares dynamically.
    // probably very crude and messy solution but it works (most of the time...)
    private static void updateColors() {
        // color change values
        float colorChange = 0.0024f;

        // green and blue only
//            if (colorChangeZ >= 0.99999f) {
//                blueUp = false;
//                greenUp = true;
//                loopedG = true;
//            } else if (colorChangeZ <= 0.35f){
//                blueUp = true;
//                loopedB = false;
//                firstLoop = false;
//            }
//            if (blueUp && !greenUp) colorChangeZ += colorChange;
//            else if (!blueUp && loopedB || (!firstLoop && !blueUp)) colorChangeZ -= colorChange;
//
//            if (colorChangeY >= 0.99999f) {
//                greenUp = false;
//                blueUp = true;
//                loopedB = true;
//            } else if (colorChangeY <= 0.35f){
//                greenUp = true;
//                loopedG = false;
//            }
//            if (greenUp && !blueUp) colorChangeY += colorChange;
//            else if (!greenUp && loopedG || (!firstLoop && !greenUp)) colorChangeY -= colorChange;


        // rgb
        if (VEC_RC_DYN.z > 1.0f) {
            blueUp = false;
            greenUp = true;
            loopedG = true;
        } else if (VEC_RC_DYN.z < 0.0f){
            blueUp = true;
            loopedB = false;
            firstLoop = false;
        }
        if (blueUp && !redUp) VEC_RC_DYN.z += colorChange;
        else if (!blueUp && loopedB || (!firstLoop && !blueUp)) VEC_RC_DYN.z -= colorChange;

        if (VEC_RC_DYN.y > 1.0f) {
            greenUp = false;
            redUp = true;
            loopedR = true;
        } else if (VEC_RC_DYN.y < 0.0f){
            greenUp = true;
            loopedG = false;
        }
        if (greenUp && !blueUp) VEC_RC_DYN.y += colorChange;
        else if (!greenUp && loopedG || (!firstLoop && !greenUp)) VEC_RC_DYN.y -= colorChange;

        if (VEC_RC_DYN.x > 1.0f) {
            redUp = false;
            blueUp = true;
            loopedB = true;
        } else if (VEC_RC_DYN.x < 0.0f){
            redUp = true;
            loopedR = false;
        }
        if (redUp && !greenUp) VEC_RC_DYN.x += colorChange;
        else if (!redUp && loopedR || (!firstLoop && !redUp)) VEC_RC_DYN.x -= colorChange;

//            System.out.println(blueUp + " " + colorChangeZ);
//            System.out.println(greenUp + " " + colorChangeY);
//            System.out.println(redUp + " " + colorChangeX);
    }

    private static void renderObjects() {

        int ips = 6, vps = 4; // indices per square, vertices per square
        int vpt = 3, tps = 2; // vertices per triangle, triangles per square
        int totalDrawnVerts = maxRows * maxCols * vpt * tps;
        float[] vertices = getVertexArray(maxRows, maxCols, winWidth, winHeight, offset, padding, length);
        int[] indices = getIndexArrayForSquares(maxRows, maxCols, ips, vps);


        // TODO: ignore this for now .. i was testing stuff.
//        slGoLBoardLive my_board = new slGoLBoardLive(maxRows, maxCols);
//        my_board.updateNextCellArray();
//        my_board.splitGrid();
//        System.out.println(my_board.getLiveCellArray()[19][17]);
//        my_board.printGoLBoard();



        int vbo = glGenBuffers();
        int ibo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, (FloatBuffer) BufferUtils.
                createFloatBuffer(vertices.length).
                put(vertices).flip(), GL_STATIC_DRAW);
        glEnableClientState(GL_VERTEX_ARRAY);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, (IntBuffer) BufferUtils.
                createIntBuffer(indices.length).
                put(indices).flip(), GL_STATIC_DRAW);
        int vertexSize = 2, vertexStride = 0;
        long vertexPointer = 0L;
        long indexPointer = 0L;
        glVertexPointer(vertexSize, GL_FLOAT, vertexStride, vertexPointer);
        SlRenderer.slCamera my_cam = new SlRenderer.slCamera();
        my_cam.setProjectionOrtho();
        viewProjMatrix = my_cam.getProjectionMatrix();
        boolean unifMatrix4fvTranspose = false;
        glUniformMatrix4fv(vpMatLocation, unifMatrix4fvTranspose,
                viewProjMatrix.get(myFloatBuffer));
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        int indexOffset = 0;


        int loopCount = 0;


        // start render
        while (!glfwWindowShouldClose(window)) {
            glfwPollEvents();
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            // colorchange is for rainbow, squareColor is for solid color
//            glUniform3f(renderColorLocation, (float) VEC_RC_DYN.x, (float) VEC_RC_DYN.y, (float) VEC_RC_DYN.z);
            glUniform3f(renderColorLocation, (float) VEC_RC.x, (float) VEC_RC.y, (float) VEC_RC.z);
            glDrawElements(GL_TRIANGLES, totalDrawnVerts, GL_UNSIGNED_INT, indexPointer);
            glfwSwapBuffers(window);



            // TODO: stuff underneath this comment is for changing colors and for future GOL stuff i was testing

//            updateColors();

            // TODO: doesnt update cells when they spawn/die - when cells spawn they stay forever.
//            if (loopCount < 16) {
//                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
//            }

            if (loopCount < 100) loopCount++;


            /*

            // render all squares on grid
//            for (int k = 0; k < maxRows; k++) {
//                for (int i = 0; i < maxCols; i++) {
//                    if (my_board.getLiveCellArray()[k][i]) {
//                        squareColorX = colorChangeX; // White color
//                        squareColorY = colorChangeY;
//                        squareColorZ = colorChangeZ;
//                    } else {
//                        squareColorX = 0.0f; // Black color
//                        squareColorY = 0.0f;
//                        squareColorZ = 0.0f;
//                    }
//                    glUniform3f(renderColorLocation, squareColorX, squareColorY, squareColorZ);
//                    // Calculate the offset for the indices of the current square
//                    indexOffset = (k * maxCols + i) * 6;
//
//                    // Draw the square using the calculated offset
//                    glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, (long) indexOffset * Integer.BYTES);
//
//                }
//
//            }


            // render only squares within updated range
            for (int k = 0; k < my_board.getUpdatedCells().size(); k++) {
                int[] squareIndex = my_board.getUpdatedCells().get(k);
                int i = squareIndex[0];
                int j = squareIndex[1];

                if (my_board.getLiveCellArray()[i][j]) {
                    squareColorX = colorChangeX; // White color
                    squareColorY = colorChangeY;
                    squareColorZ = colorChangeZ;
                } else {
                    squareColorX = 0.0f; // Black color
                    squareColorY = 0.0f;
                    squareColorZ = 0.0f;
                }

                glUniform3f(renderColorLocation, squareColorX, squareColorY, squareColorZ);
                // Calculate the offset for the indices of the current square
                indexOffset = (i * maxCols + j) * 6;

                // Draw the square using the calculated offset
                glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, (long) indexOffset * Integer.BYTES);
            }

//            try {
//                Thread.sleep(50);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }

            */


        }
        glDeleteBuffers(vbo);
        glDeleteBuffers(ibo);
    } // renderObjects
}