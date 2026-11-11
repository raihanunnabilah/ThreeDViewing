package com.grafkom.threedviewing.projection;

import org.joml.Matrix4f;

public class ProjectionTaxonomyManager {

    // Klasifikasi jenis proyeksi.
    public enum ProjectionType {

        ORTHOGRAPHIC,
        ISOMETRIC,
        OBLIQUE_CAVALIER,
        PERSPECTIVE_1POINT,
        PERSPECTIVE_2POINT,
        PERSPECTIVE_3POINT
    }

    // Parameter frustum.
    private static final float NEAR_PLANE = 0.1f;
    private static final float FAR_PLANE = 100.0f;
    private static final float FOV = 45.0f;

    // Membuat matriks proyeksi berdasarkan jenis proyeksi.
    public static Matrix4f createProjectionMatrix(
            ProjectionType type,
            float aspect
    ) {

        if (type == null) {
            throw new IllegalArgumentException(
                    "Jenis proyeksi tidak boleh null."
            );
        }

        if (aspect <= 0.0f) {
            throw new IllegalArgumentException(
                    "Aspect ratio harus lebih besar dari nol."
            );
        }

        Matrix4f projection = new Matrix4f();

        switch (type) {

            // Parallel Projection.
            case ORTHOGRAPHIC:
            case ISOMETRIC:
            case OBLIQUE_CAVALIER:

                projection.ortho(
                        -3.0f * aspect,
                        3.0f * aspect,
                        -3.0f,
                        3.0f,
                        NEAR_PLANE,
                        FAR_PLANE
                );

                break;

            // Perspective Projection.
            case PERSPECTIVE_1POINT:
            case PERSPECTIVE_2POINT:
            case PERSPECTIVE_3POINT:

                projection.perspective(
                        (float) Math.toRadians(FOV),
                        aspect,
                        NEAR_PLANE,
                        FAR_PLANE
                );

                break;
        }

        return projection;
    }

    // Membuat matriks orientasi berdasarkan jenis proyeksi.
    public static Matrix4f createOrientationMatrix(
            ProjectionType type
    ) {

        if (type == null) {
            throw new IllegalArgumentException(
                    "Jenis proyeksi tidak boleh null."
            );
        }

        Matrix4f orientation = new Matrix4f();

        switch (type) {

            // 1. Orthographic Projection.
            // Tidak menggunakan rotasi atau shear tambahan.
            case ORTHOGRAPHIC:

                break;

            // 2. Isometric Projection.
            // Menggunakan rotasi pada sumbu X dan Y.
            case ISOMETRIC:

                orientation.rotateX(
                        (float) Math.toRadians(35.264f)
                );

                orientation.rotateY(
                        (float) Math.toRadians(45.0f)
                );

                break;

            // 3. Oblique Cavalier Projection.
            // Kedalaman diproyeksikan secara miring
            // dengan sudut 45 derajat dan faktor skala 1.
            case OBLIQUE_CAVALIER:

                float angle = (float) Math.toRadians(45.0f);

                Matrix4f shear = new Matrix4f();

                shear.m20((float) Math.cos(angle));
                shear.m21((float) Math.sin(angle));

                orientation.mul(shear);

                break;

            // 4. Perspective 1-Point.
            // Satu arah kedalaman mengarah ke satu titik hilang.
            case PERSPECTIVE_1POINT:

                break;

            // 5. Perspective 2-Point.
            // Rotasi horizontal membuat dua arah horizontal
            // objek tampak menuju dua titik hilang.
            case PERSPECTIVE_2POINT:

                orientation.rotateY(
                        (float) Math.toRadians(35.0f)
                );

                break;

            // 6. Perspective 3-Point.
            // Rotasi horizontal dan vertikal membuat
            // tiga arah utama objek memiliki titik hilang.
            case PERSPECTIVE_3POINT:

                orientation.rotateY(
                        (float) Math.toRadians(35.0f)
                );

                orientation.rotateX(
                        (float) Math.toRadians(25.0f)
                );

                break;
        }

        return orientation;
    }

    // Memilih jenis proyeksi berdasarkan tombol keyboard.
    public static ProjectionType getProjectionFromKey(int key) {

        switch (key) {

            case 1:
                return ProjectionType.ORTHOGRAPHIC;

            case 2:
                return ProjectionType.ISOMETRIC;

            case 3:
                return ProjectionType.OBLIQUE_CAVALIER;

            case 4:
                return ProjectionType.PERSPECTIVE_1POINT;

            case 5:
                return ProjectionType.PERSPECTIVE_2POINT;

            case 6:
                return ProjectionType.PERSPECTIVE_3POINT;

            default:
                return null;
        }
    }
}