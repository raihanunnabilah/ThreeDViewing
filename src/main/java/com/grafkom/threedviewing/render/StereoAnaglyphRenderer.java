package com.grafkom.threedviewing.render;

import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL11.*;

public class StereoAnaglyphRenderer {

    // Jarak awal antarpupil mata: 6,4 cm.
    private float ipd = 0.064f;

    // Jarak bidang konvergensi dari kamera.
    private float focalLength = 4.0f;

    // Mengatur jarak antarpupil.
    public void setIpd(float ipd) {

        this.ipd = Math.max(
                0.0f,
                Math.min(0.20f, ipd)
        );
    }

    // Mengatur jarak konvergensi.
    public void setFocalLength(float focalLength) {

        if (focalLength > 0.0f) {
            this.focalLength = focalLength;
        }
    }

    public float getIpd() {
        return ipd;
    }

    public float getFocalLength() {
        return focalLength;
    }

    // Membuat matriks proyeksi Off-Axis Asymmetric Frustum.
    public Matrix4f createEyeProjectionMatrix(
            float fov,
            float aspect,
            float near,
            float far,
            float eyeOffset
    ) {

        float top = near * (float) Math.tan(
                Math.toRadians(fov / 2.0f)
        );

        float bottom = -top;

        float right = top * aspect;
        float left = -right;

        // Pergeseran frustum berdasarkan posisi mata.
        float shift = -eyeOffset * near / focalLength;

        left += shift;
        right += shift;

        Matrix4f projection = new Matrix4f();
        projection.zero();

        projection.m00(
                2.0f * near / (right - left)
        );

        projection.m11(
                2.0f * near / (top - bottom)
        );

        projection.m20(
                (right + left) / (right - left)
        );

        projection.m21(
                (top + bottom) / (top - bottom)
        );

        projection.m22(
                -(far + near) / (far - near)
        );

        projection.m32(
                -(2.0f * far * near) / (far - near)
        );

        projection.m23(-1.0f);
        projection.m33(0.0f);

        return projection;
    }

    // Membuat matriks pandangan untuk masing-masing mata.
    public Matrix4f createEyeViewMatrix(
            Matrix4f baseView,
            float eyeOffset
    ) {

        Matrix4f eyeTranslation = new Matrix4f()
                .translation(-eyeOffset, 0.0f, 0.0f);

        return eyeTranslation.mul(
                new Matrix4f(baseView)
        );
    }

    // Menggabungkan tampilan mata kiri dan kanan
    // menggunakan teknik Red-Cyan Anaglyph.
    public void renderAnaglyph(
            Runnable leftEyeCallback,
            Runnable rightEyeCallback
    ) {

        try {

            // Render mata kiri menggunakan kanal merah.
            glColorMask(true, false, false, true);

            glClear(
                    GL_COLOR_BUFFER_BIT |
                    GL_DEPTH_BUFFER_BIT
            );

            leftEyeCallback.run();

            // Render mata kanan menggunakan kanal hijau dan biru.
            glColorMask(false, true, true, true);

            // Membersihkan depth buffer sebelum render mata kanan.
            glClear(GL_DEPTH_BUFFER_BIT);

            rightEyeCallback.run();

        } finally {

            // Mengembalikan seluruh kanal warna.
            glColorMask(true, true, true, true);
        }
    }
}