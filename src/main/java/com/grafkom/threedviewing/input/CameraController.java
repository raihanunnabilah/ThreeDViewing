package com.grafkom.threedviewing.input;

import com.grafkom.threedviewing.camera.Camera3D;
import com.grafkom.threedviewing.projection.ProjectionTaxonomyManager;

import static org.lwjgl.glfw.GLFW.*;

public class CameraController {

    private final long window;
    private final Camera3D camera;

    private float radius;
    private float azimuth;
    private float elevation;

    private float lastX;
    private float lastY;

    private boolean dragging = false;
    private boolean anaglyphEnabled = false;

    // Pengaturan metode pencahayaan.
    private boolean blinnPhongEnabled = false;

    // Pengaturan metode shading.
    private boolean smoothShadingEnabled = false;

    // Pengaturan Hidden Surface Removal.
    private boolean zBufferEnabled = true;
    private boolean backFaceCullingEnabled = false;

    // Pengaturan Texture Filtering.
    // false = Nearest, true = Bilinear Mipmapped.
    private boolean bilinearFilteringEnabled = false;

    // Pengaturan Stereo Anaglyph.
    private float stereoIpd = 0.064f;
    private float focalLength = 7.8f;

    // Pengaturan Field of View.
    private float fovDegree = 45.0f;

    // Jenis proyeksi yang sedang digunakan.
    private ProjectionTaxonomyManager.ProjectionType projectionType
            = ProjectionTaxonomyManager.ProjectionType.PERSPECTIVE_1POINT;

    private static final float ROTATION_SENSITIVITY = 0.3f;
    private static final float ZOOM_SENSITIVITY = 0.5f;

    private static final float MIN_RADIUS = 1.0f;
    private static final float MAX_RADIUS = 30.0f;

    public CameraController(long window, Camera3D camera) {

        this.window = window;
        this.camera = camera;

        calculateInitialCameraPosition();
        registerCallbacks();
    }

    private void calculateInitialCameraPosition() {

        var eye = camera.getEye();
        var target = camera.getTarget();

        float dx = eye.x - target.x;
        float dy = eye.y - target.y;
        float dz = eye.z - target.z;

        radius = (float) Math.sqrt(
                dx * dx + dy * dy + dz * dz
        );

        if (radius < MIN_RADIUS) {
            radius = 5.0f;
        }

        azimuth = (float) Math.toDegrees(
                Math.atan2(dx, dz)
        );

        elevation = (float) Math.toDegrees(
                Math.asin(dy / radius)
        );
    }

    private void registerCallbacks() {

        // Mengatur input mouse.
        glfwSetMouseButtonCallback(window, (handle, button, action, mods) -> {

            if (button == GLFW_MOUSE_BUTTON_LEFT) {
                dragging = action == GLFW_PRESS;
            }
        });

        // Mengatur rotasi kamera menggunakan mouse.
        glfwSetCursorPosCallback(window, (handle, xpos, ypos) -> {

            if (dragging) {

                float deltaX = (float) (xpos - lastX);
                float deltaY = (float) (ypos - lastY);

                azimuth += deltaX * ROTATION_SENSITIVITY;
                elevation -= deltaY * ROTATION_SENSITIVITY;

                elevation = Math.max(
                        -89.0f,
                        Math.min(89.0f, elevation)
                );

                updateCameraPosition();
            }

            lastX = (float) xpos;
            lastY = (float) ypos;
        });

        // Mengatur zoom menggunakan scroll.
        glfwSetScrollCallback(window, (handle, xoffset, yoffset) -> {

            radius -= (float) yoffset * ZOOM_SENSITIVITY;

            radius = Math.max(
                    MIN_RADIUS,
                    Math.min(MAX_RADIUS, radius)
            );

            updateCameraPosition();
        });

        // Mengatur keyboard.
        glfwSetKeyCallback(window, (handle, key, scancode, action, mods) -> {

            if (action != GLFW_PRESS) {
                return;
            }

            switch (key) {

                case GLFW_KEY_1:
                    projectionType
                            = ProjectionTaxonomyManager.ProjectionType.ORTHOGRAPHIC;
                    break;

                case GLFW_KEY_2:
                    projectionType
                            = ProjectionTaxonomyManager.ProjectionType.ISOMETRIC;
                    break;

                case GLFW_KEY_3:
                    projectionType
                            = ProjectionTaxonomyManager.ProjectionType.OBLIQUE_CAVALIER;
                    break;

                case GLFW_KEY_4:
                    projectionType
                            = ProjectionTaxonomyManager.ProjectionType.PERSPECTIVE_1POINT;
                    break;

                case GLFW_KEY_5:
                    projectionType
                            = ProjectionTaxonomyManager.ProjectionType.PERSPECTIVE_2POINT;
                    break;

                case GLFW_KEY_6:
                    projectionType
                            = ProjectionTaxonomyManager.ProjectionType.PERSPECTIVE_3POINT;
                    break;

                // Mengaktifkan atau menonaktifkan Stereo Anaglyph.
                case GLFW_KEY_A:
                    anaglyphEnabled = !anaglyphEnabled;
                    break;

                // Mengganti Phong dan Blinn-Phong.
                case GLFW_KEY_B:
                    blinnPhongEnabled = !blinnPhongEnabled;
                    break;

                // Mengganti Flat dan Smooth Shading.
                case GLFW_KEY_F:
                    smoothShadingEnabled = !smoothShadingEnabled;
                    break;

                // Mengaktifkan atau menonaktifkan Z-Buffer.
                case GLFW_KEY_Z:
                    zBufferEnabled = !zBufferEnabled;
                    break;

                // Mengaktifkan atau menonaktifkan Back-Face Culling.
                case GLFW_KEY_C:
                    backFaceCullingEnabled = !backFaceCullingEnabled;
                    break;

                // Mengganti Nearest dan Bilinear Mipmapped Filtering.
                case GLFW_KEY_T:
                    bilinearFilteringEnabled = !bilinearFilteringEnabled;
                    break;

                // Mengurangi IPD.
                case GLFW_KEY_LEFT_BRACKET:
                    stereoIpd = Math.max(
                            0.0f,
                            stereoIpd - 0.005f
                    );
                    break;

                // Menambah IPD.
                case GLFW_KEY_RIGHT_BRACKET:
                    stereoIpd = Math.min(
                            0.20f,
                            stereoIpd + 0.005f
                    );
                    break;

                // Mengurangi FOV.
                case GLFW_KEY_PAGE_UP:
                    fovDegree = Math.max(
                            20.0f,
                            fovDegree - 2.0f
                    );
                    break;

                // Menambah FOV.
                case GLFW_KEY_PAGE_DOWN:
                    fovDegree = Math.min(
                            90.0f,
                            fovDegree + 2.0f
                    );
                    break;

                // Mengurangi focal length.
                case GLFW_KEY_H:
                    focalLength = Math.max(
                            1.0f,
                            focalLength - 0.2f
                    );
                    break;

                // Menambah focal length.
                case GLFW_KEY_G:
                    focalLength = Math.min(
                            15.0f,
                            focalLength + 0.2f
                    );
                    break;
            }
        });
    }

    private void updateCameraPosition() {

        camera.updateOrbitalPosition(
                radius,
                azimuth,
                elevation
        );
    }

    public ProjectionTaxonomyManager.ProjectionType getProjectionType() {
        return projectionType;
    }

    public boolean isAnaglyphEnabled() {
        return anaglyphEnabled;
    }

    public boolean isBlinnPhongEnabled() {
        return blinnPhongEnabled;
    }

    public boolean isSmoothShadingEnabled() {
        return smoothShadingEnabled;
    }

    public boolean isZBufferEnabled() {
        return zBufferEnabled;
    }

    public boolean isBackFaceCullingEnabled() {
        return backFaceCullingEnabled;
    }

    public boolean isBilinearFilteringEnabled() {
        return bilinearFilteringEnabled;
    }

    public float getRadius() {
        return radius;
    }

    public float getAzimuth() {
        return azimuth;
    }

    public float getElevation() {
        return elevation;
    }

    public float getStereoIpd() {
        return stereoIpd;
    }

    public float getFovDegree() {
        return fovDegree;
    }

    public float getFocalLength() {
        return focalLength;
    }
}