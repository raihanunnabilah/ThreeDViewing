package com.grafkom.threedviewing;

import com.grafkom.threedviewing.camera.Camera3D;
import com.grafkom.threedviewing.input.CameraController;
import com.grafkom.threedviewing.model.Mesh3D;
import com.grafkom.threedviewing.model.GroundPlane;
import com.grafkom.threedviewing.projection.PerspectiveProjection;
import com.grafkom.threedviewing.projection.ProjectionTaxonomyManager;
import com.grafkom.threedviewing.render.StereoAnaglyphRenderer;
import com.grafkom.threedviewing.shader.ShaderProgram;
import com.grafkom.threedviewing.texture.TextureLoader;
import com.grafkom.threedviewing.shadow.ShadowMapFBO;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;

public class ThreeDViewing {

    private long window;

    private Camera3D camera;
    private CameraController cameraController;

    private Mesh3D cube;
    private GroundPlane groundPlane;

    private PerspectiveProjection perspective;
    private StereoAnaglyphRenderer stereoRenderer;

    private ShaderProgram shaderProgram;
    private ShaderProgram depthShaderProgram;

    private ShadowMapFBO shadowMapFBO;

    // Texture Mapping.
    private int textureId;

    // Status filtering terakhir.
    private boolean appliedBilinearFiltering = false;

    // Posisi sumber cahaya.
    private final Vector3f lightPos =
            new Vector3f(3.0f, 4.0f, 3.0f);

    // Matriks Shadow Mapping.
    private Matrix4f lightSpaceMatrix;

    private int width = 1000;
    private int height = 700;

    private boolean running = true;

    public static void main(String[] args) {
        new ThreeDViewing().run();
    }

    public void run() {

        try {
            init();
            loop();

        } finally {

            if (cube != null) {
                cube.cleanup();
            }

            if (groundPlane != null) {
                groundPlane.cleanup();
            }

            if (shaderProgram != null) {
                shaderProgram.cleanup();
            }

            if (depthShaderProgram != null) {
                depthShaderProgram.cleanup();
            }

            if (shadowMapFBO != null) {
                shadowMapFBO.cleanup();
            }

            if (textureId != 0) {
                TextureLoader.cleanup(textureId);
            }

            if (window != 0) {
                glfwDestroyWindow(window);
            }

            glfwTerminate();
        }
    }

    private void init() {

        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit()) {
            throw new IllegalStateException(
                    "Tidak dapat menginisialisasi GLFW."
            );
        }

        // Meminta OpenGL 3.3 Core Profile.
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_DEPTH_BITS, 24);

        glfwWindowHint(
                GLFW_OPENGL_PROFILE,
                GLFW_OPENGL_CORE_PROFILE
        );

        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);

        window = glfwCreateWindow(
                width,
                height,
                "3D Visual Realism Suite",
                0,
                0
        );

        if (window == 0) {
            throw new RuntimeException(
                    "Gagal membuat window OpenGL 3.3."
            );
        }

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);

        GL.createCapabilities();

        // Mengaktifkan Depth Testing.
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LESS);

        // Mengatur urutan vertex depan.
        glFrontFace(GL_CCW);

        System.out.println(
                "OpenGL Version: " + glGetString(GL_VERSION)
        );

        glViewport(0, 0, width, height);

        glfwSetFramebufferSizeCallback(window, (handle, newWidth, newHeight) -> {

            width = Math.max(newWidth, 1);
            height = Math.max(newHeight, 1);

            glViewport(0, 0, width, height);
        });

        glClearColor(0.12f, 0.12f, 0.16f, 1.0f);

        // ==========================================
        // INISIALISASI KAMERA
        // ==========================================

        camera = new Camera3D(
                new Vector3f(4, 3, 6),
                new Vector3f(0, 0, 0),
                new Vector3f(0, 1, 0)
        );

        cameraController = new CameraController(window, camera);

        // ==========================================
        // INISIALISASI SHADER UTAMA
        // ==========================================

        shaderProgram = new ShaderProgram(
                "shaders/basic.vert",
                "shaders/basic.frag"
        );

        // ==========================================
        // INISIALISASI DEPTH SHADER
        // ==========================================

        depthShaderProgram = new ShaderProgram(
                "shaders/shadow_depth.vert",
                "shaders/shadow_depth.frag"
        );

        // ==========================================
        // INISIALISASI OBJEK 3D
        // ==========================================

        cube = new Mesh3D(2.0f);

        groundPlane = new GroundPlane();

        // ==========================================
        // INISIALISASI SHADOW MAP
        // ==========================================

        shadowMapFBO = new ShadowMapFBO();

        // ==========================================
        // INISIALISASI TEXTURE MAPPING
        // ==========================================

        textureId = TextureLoader.loadTexture(
                "textures/cube_texture.jpg"
        );

        TextureLoader.setFiltering(textureId, false);

        // ==========================================
        // INISIALISASI PROYEKSI PERSPEKTIF
        // ==========================================

        perspective = new PerspectiveProjection(
                45.0f,
                (float) width / height,
                0.1f,
                100.0f
        );

        // ==========================================
        // INISIALISASI STEREO ANAGLYPH
        // ==========================================

        stereoRenderer = new StereoAnaglyphRenderer();

        System.out.println(
                "3D Visual Realism Suite berhasil dijalankan."
        );

        System.out.println("Kontrol:");
        System.out.println("- Klik kiri + geser mouse: rotasi kamera");
        System.out.println("- Scroll mouse: zoom");
        System.out.println("- Tombol 1-6: pilihan proyeksi");
        System.out.println("- Tombol A: Stereo Anaglyph");
        System.out.println("- Tombol B: Phong / Blinn-Phong");
        System.out.println("- Tombol F: Flat / Smooth Shading");
        System.out.println("- Tombol Z: Z-Buffer ON / OFF");
        System.out.println("- Tombol C: Back-Face Culling ON / OFF");
        System.out.println("- Tombol T: Nearest / Bilinear Mipmapped");
        System.out.println("- Page Up: memperkecil FOV");
        System.out.println("- Page Down: memperbesar FOV");
        System.out.println("- Tombol [ dan ]: mengatur IPD Stereo");
        System.out.println("- Tombol H: mengurangi focal length");
        System.out.println("- Tombol G: menambah focal length");
        System.out.println("- Shadow Mapping: aktif");
    }

    private void loop() {

        while (running && !glfwWindowShouldClose(window)) {

            glfwPollEvents();

            render();

            glfwSwapBuffers(window);
        }
    }

    private void render() {

        // ==========================================
        // PENGATURAN HIDDEN SURFACE REMOVAL
        // ==========================================

        if (cameraController.isZBufferEnabled()) {

            glEnable(GL_DEPTH_TEST);

        } else {

            glDisable(GL_DEPTH_TEST);
        }

        if (cameraController.isBackFaceCullingEnabled()) {

            glEnable(GL_CULL_FACE);
            glCullFace(GL_BACK);
            glFrontFace(GL_CCW);

        } else {

            glDisable(GL_CULL_FACE);
        }

        // ==========================================
        // PENGATURAN TEXTURE FILTERING
        // ==========================================

        boolean currentFiltering =
                cameraController.isBilinearFilteringEnabled();

        if (currentFiltering != appliedBilinearFiltering) {

            TextureLoader.setFiltering(
                    textureId,
                    currentFiltering
            );

            appliedBilinearFiltering = currentFiltering;
        }

        // ==========================================
        // MENGHITUNG LIGHT SPACE MATRIX
        // ==========================================

        lightSpaceMatrix = new Matrix4f()
                .ortho(
                        -8.0f, 8.0f,
                        -8.0f, 8.0f,
                        0.1f, 20.0f
                )
                .lookAt(
                        lightPos,
                        new Vector3f(0, 0, 0),
                        new Vector3f(0, 1, 0)
                );

        // ==========================================
        // SHADOW MAPPING - PASS 1
        // ==========================================

        renderDepthPass();

        // Mengembalikan viewport ke ukuran window.
        shadowMapFBO.unbind(width, height);

        // Mengembalikan pengaturan Depth Testing.
        if (cameraController.isZBufferEnabled()) {
            glEnable(GL_DEPTH_TEST);
        } else {
            glDisable(GL_DEPTH_TEST);
        }

        // ==========================================
        // INFORMASI PROYEKSI DAN KAMERA
        // ==========================================

        ProjectionTaxonomyManager.ProjectionType type =
                cameraController.getProjectionType();

        float currentFov = cameraController.getFovDegree();
        float currentIpd = cameraController.getStereoIpd();
        float currentFocalLength = cameraController.getFocalLength();

        String stereoStatus =
                cameraController.isAnaglyphEnabled() ? "ON" : "OFF";

        String lightingStatus =
                cameraController.isBlinnPhongEnabled()
                        ? "Blinn-Phong" : "Phong";

        String shadingStatus =
                cameraController.isSmoothShadingEnabled()
                        ? "Smooth" : "Flat";

        String zBufferStatus =
                cameraController.isZBufferEnabled() ? "ON" : "OFF";

        String cullingStatus =
                cameraController.isBackFaceCullingEnabled() ? "ON" : "OFF";

        String filteringStatus =
                currentFiltering ? "Bilinear Mipmapped" : "Nearest";

        String windowTitle = String.format(
                "3D Viewing | Projection: %s | FOV: %.1f | IPD: %.3f | Focal Length: %.1f | Stereo: %s | Lighting: %s | Shading: %s | Z-Buffer: %s | Culling: %s | Filtering: %s | Shadow: ON",
                type,
                currentFov,
                currentIpd,
                currentFocalLength,
                stereoStatus,
                lightingStatus,
                shadingStatus,
                zBufferStatus,
                cullingStatus,
                filteringStatus
        );

        glfwSetWindowTitle(window, windowTitle);

        // ==========================================
        // MENGHITUNG MATRIKS PROYEKSI
        // ==========================================

        float aspect = (float) width / (float) height;

        perspective.setFovDegree(currentFov);
        perspective.setAspectRatio(aspect);

        Matrix4f projectionMatrix;

        switch (type) {

            case ORTHOGRAPHIC:
            case ISOMETRIC:
            case OBLIQUE_CAVALIER:

                projectionMatrix =
                        ProjectionTaxonomyManager.createProjectionMatrix(
                                type,
                                aspect
                        );

                break;

            case PERSPECTIVE_1POINT:
            case PERSPECTIVE_2POINT:
            case PERSPECTIVE_3POINT:

                projectionMatrix =
                        perspective.calculateProjectionMatrix();

                break;

            default:

                projectionMatrix =
                        perspective.calculateProjectionMatrix();

                break;
        }

        // ==========================================
        // MENGHITUNG MATRIKS KAMERA
        // ==========================================

        Matrix4f viewMatrix = camera.calculateLookAtMatrix();

        Matrix4f orientationMatrix =
                ProjectionTaxonomyManager.createOrientationMatrix(type);

        viewMatrix.mul(orientationMatrix);

        // ==========================================
        // MEMBERSIHKAN FRAMEBUFFER UTAMA
        // ==========================================

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        // ==========================================
        // STEREO ANAGLYPH
        // ==========================================

        if (cameraController.isAnaglyphEnabled()) {

            stereoRenderer.setIpd(currentIpd);
            stereoRenderer.setFocalLength(currentFocalLength);

            float leftOffset = -currentIpd / 2.0f;
            float rightOffset = currentIpd / 2.0f;

            Matrix4f leftProjection =
                    stereoRenderer.createEyeProjectionMatrix(
                            currentFov,
                            aspect,
                            0.1f,
                            100.0f,
                            leftOffset
                    );

            Matrix4f rightProjection =
                    stereoRenderer.createEyeProjectionMatrix(
                            currentFov,
                            aspect,
                            0.1f,
                            100.0f,
                            rightOffset
                    );

            Matrix4f leftView =
                    stereoRenderer.createEyeViewMatrix(
                            viewMatrix,
                            leftOffset
                    );

            Matrix4f rightView =
                    stereoRenderer.createEyeViewMatrix(
                            viewMatrix,
                            rightOffset
                    );

            stereoRenderer.renderAnaglyph(
                    () -> renderStereoParallaxScene(
                            leftProjection,
                            leftView
                    ),
                    () -> renderStereoParallaxScene(
                            rightProjection,
                            rightView
                    )
            );

        } else {

            renderScene(projectionMatrix, viewMatrix);
        }
    }

    // ==========================================
    // SHADOW MAPPING - PASS 1: DEPTH PASS
    // ==========================================

    private void renderDepthPass() {

        // Shadow map selalu membutuhkan Depth Testing.
        glEnable(GL_DEPTH_TEST);

        shadowMapFBO.bindForWriting();

        glClear(GL_DEPTH_BUFFER_BIT);

        depthShaderProgram.bind();

        depthShaderProgram.setUniform(
                "lightSpaceMatrix",
                lightSpaceMatrix
        );

        // Ground Plane.
        renderDepthObject(new Matrix4f(), true);

        // Kubus utama.
        if (cameraController.isAnaglyphEnabled()) {

            float focalLength = cameraController.getFocalLength();

            Matrix4f firstModel = new Matrix4f()
                    .translate(
                            -1.8f,
                            0.0f,
                            -(focalLength - 1.0f)
                    );

            Matrix4f secondModel = new Matrix4f()
                    .translate(
                            0.0f,
                            0.0f,
                            -focalLength
                    );

            Matrix4f thirdModel = new Matrix4f()
                    .translate(
                            1.8f,
                            0.0f,
                            -(focalLength + 1.0f)
                    );

            renderDepthObject(firstModel, false);
            renderDepthObject(secondModel, false);
            renderDepthObject(thirdModel, false);

        } else {

            renderDepthObject(new Matrix4f(), false);
        }

        depthShaderProgram.unbind();
    }

    private void renderDepthObject(
            Matrix4f modelMatrix,
            boolean ground
    ) {

        depthShaderProgram.setUniform(
                "model",
                modelMatrix
        );

        if (ground) {
            groundPlane.render();
        } else {
            cube.render();
        }
    }

    // ==========================================
    // SHADOW MAPPING - PASS 2: NORMAL RENDERING
    // ==========================================

    private void renderScene(
            Matrix4f projectionMatrix,
            Matrix4f viewMatrix
    ) {

        // Ground Plane dirender terlebih dahulu.
        renderObject(
                projectionMatrix,
                viewMatrix,
                new Matrix4f(),
                new Vector3f(0.65f, 0.65f, 0.65f),
                true
        );

        // Kubus utama.
        renderObject(
                projectionMatrix,
                viewMatrix,
                new Matrix4f(),
                new Vector3f(0.2f, 0.5f, 0.9f),
                false
        );
    }

    // ==========================================
    // RENDER STEREO PARALLAX
    // ==========================================

    private void renderStereoParallaxScene(
            Matrix4f projectionMatrix,
            Matrix4f viewMatrix
    ) {

        float focalLength = cameraController.getFocalLength();

        // Ground Plane.
        renderObject(
                projectionMatrix,
                viewMatrix,
                new Matrix4f(),
                new Vector3f(0.65f, 0.65f, 0.65f),
                true
        );

        // Objek pertama.
        Matrix4f firstModel = new Matrix4f()
                .translate(
                        -1.8f,
                        0.0f,
                        -(focalLength - 1.0f)
                );

        renderObject(
                projectionMatrix,
                viewMatrix,
                firstModel,
                new Vector3f(0.9f, 0.3f, 0.3f),
                false
        );

        // Objek kedua.
        Matrix4f secondModel = new Matrix4f()
                .translate(
                        0.0f,
                        0.0f,
                        -focalLength
                );

        renderObject(
                projectionMatrix,
                viewMatrix,
                secondModel,
                new Vector3f(0.3f, 0.8f, 0.4f),
                false
        );

        // Objek ketiga.
        Matrix4f thirdModel = new Matrix4f()
                .translate(
                        1.8f,
                        0.0f,
                        -(focalLength + 1.0f)
                );

        renderObject(
                projectionMatrix,
                viewMatrix,
                thirdModel,
                new Vector3f(0.3f, 0.5f, 0.9f),
                false
        );
    }

    // ==========================================
    // RENDER OBJECT
    // ==========================================

    private void renderObject(
            Matrix4f projectionMatrix,
            Matrix4f viewMatrix,
            Matrix4f modelMatrix,
            Vector3f objectColor,
            boolean ground
    ) {

        shaderProgram.bind();

        shaderProgram.setUniform(
                "model",
                modelMatrix
        );

        shaderProgram.setUniform(
                "view",
                viewMatrix
        );

        shaderProgram.setUniform(
                "projection",
                projectionMatrix
        );

        shaderProgram.setUniform(
                "lightSpaceMatrix",
                lightSpaceMatrix
        );

        shaderProgram.setUniform(
                "objectColor",
                objectColor
        );

        shaderProgram.setUniform(
                "lightPos",
                lightPos
        );

        shaderProgram.setUniform(
                "viewPos",
                camera.getEye()
        );

        // Mode pencahayaan.
        shaderProgram.setUniform(
                "lightingMode",
                cameraController.isBlinnPhongEnabled() ? 1 : 0
        );

        // Mode shading.
        shaderProgram.setUniform(
                "shadingMode",
                cameraController.isSmoothShadingEnabled() ? 1 : 0
        );

        // Texture unit 0: tekstur objek.
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureId);

        shaderProgram.setUniform(
                "textureSampler",
                0
        );

        // Texture unit 1: Shadow Map.
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(
                GL_TEXTURE_2D,
                shadowMapFBO.getDepthTextureId()
        );

        shaderProgram.setUniform(
                "shadowMap",
                1
        );

        // Menggambar objek.
        if (ground) {
            groundPlane.render();
        } else {
            cube.render();
        }

        // Melepaskan texture.
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, 0);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, 0);

        shaderProgram.unbind();
    }
}