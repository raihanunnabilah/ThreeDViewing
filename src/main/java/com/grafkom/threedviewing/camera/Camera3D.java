package com.grafkom.threedviewing.camera;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera3D {

    private Vector3f eye;
    private Vector3f target;
    private Vector3f up;

    // Constructor
    public Camera3D(Vector3f eye, Vector3f target, Vector3f up) {
        this.eye = new Vector3f(eye);
        this.target = new Vector3f(target);
        this.up = new Vector3f(up);
    }

    // Menghitung View Matrix menggunakan LookAt
    public Matrix4f calculateLookAtMatrix() {

        // View Plane Normal
        Vector3f n = new Vector3f();
        eye.sub(target, n).normalize();

        // Right Vector
        Vector3f u = new Vector3f();
        up.cross(n, u).normalize();

        // True Up Vector
        Vector3f v = new Vector3f();
        n.cross(u, v);

        // Membentuk View Matrix
        Matrix4f viewMatrix = new Matrix4f();

        viewMatrix.m00(u.x).m10(u.y).m20(u.z).m30(-u.dot(eye));
        viewMatrix.m01(v.x).m11(v.y).m21(v.z).m31(-v.dot(eye));
        viewMatrix.m02(n.x).m12(n.y).m22(n.z).m32(-n.dot(eye));
        viewMatrix.m03(0.0f).m13(0.0f).m23(0.0f).m33(1.0f);

        return viewMatrix;
    }

    // Mengatur posisi kamera orbital
    public void updateOrbitalPosition(
            float radius,
            float azimuthDeg,
            float elevationDeg) {

        float azRad = (float) Math.toRadians(azimuthDeg);
        float elRad = (float) Math.toRadians(elevationDeg);

        eye.x = target.x + radius
                * (float) (Math.cos(elRad) * Math.sin(azRad));

        eye.y = target.y + radius
                * (float) Math.sin(elRad);

        eye.z = target.z + radius
                * (float) (Math.cos(elRad) * Math.cos(azRad));
    }

    // Getter
    public Vector3f getEye() {
        return new Vector3f(eye);
    }

    public Vector3f getTarget() {
        return new Vector3f(target);
    }

    public Vector3f getUp() {
        return new Vector3f(up);
    }

    // Setter
    public void setTarget(Vector3f target) {
        this.target.set(target);
    }

    public void setUp(Vector3f up) {
        this.up.set(up);
    }
}