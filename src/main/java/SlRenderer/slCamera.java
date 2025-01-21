package SlRenderer;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import static csc133.spot.*;

public class slCamera {

    private static Matrix4f projectionMatrix, viewMatrix;
    private static float f_left, f_right, f_bottom, f_top, f_near, f_far;
    public static Vector3f defaultLookFrom, defaultLookAt, defaultUpVector;
    private static Vector3f curLookFrom, curLookAt, curUpVector;


    public slCamera(Vector3f camera_position) {
        new slCamera();
    }

    public slCamera() {
        defaultLookFrom = new Vector3f(0f, 0f, 10f);
        defaultLookAt = new Vector3f(0f, 0f, -1.0f);
        defaultUpVector = new Vector3f(0f, 1.0f, 0f);
        curLookFrom = defaultLookFrom;
        curLookAt = defaultLookAt;
        curUpVector = defaultUpVector;

        f_left = 0.0f;
        f_right = (float) winWidth;
        f_bottom = 0.0f;
        f_top = (float) winHeight;
        f_near = 0.0f;
        f_far = 10.0f;

        setCamera();
    }


    private void setCamera() {
        projectionMatrix = new Matrix4f().identity();
        viewMatrix = new Matrix4f().identity();
    }

    public void setProjectionOrtho() {
        projectionMatrix.identity();
        projectionMatrix.setOrtho(f_left, f_right, f_bottom, f_top, f_near, f_far);
    }

    public void setProjectionOrtho(float left, float right, float bottom, float top, float near, float far) {
        f_left = left;
        f_right = right;
        f_bottom = bottom;
        f_top = top;
        f_near = near;
        f_far = far;

        setProjectionOrtho();
    }

    public Matrix4f getViewMatrix() {
        viewMatrix.identity();
        viewMatrix.lookAt(curLookFrom, curLookAt.add(defaultLookFrom), curUpVector);
        return viewMatrix;
    }

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }
}