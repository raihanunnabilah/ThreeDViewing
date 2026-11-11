package com.grafkom.threedviewing.shadow;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

public class ShadowMapFBO {

    private int fboId;
    private int depthTextureId;

    private static final int SHADOW_WIDTH = 2048;
    private static final int SHADOW_HEIGHT = 2048;

    public ShadowMapFBO() {

        // Membuat Framebuffer Object.
        fboId = glGenFramebuffers();

        // Membuat Depth Texture.
        depthTextureId = glGenTextures();

        glBindTexture(GL_TEXTURE_2D, depthTextureId);

        glTexImage2D(
                GL_TEXTURE_2D,
                0,
                GL_DEPTH_COMPONENT,
                SHADOW_WIDTH,
                SHADOW_HEIGHT,
                0,
                GL_DEPTH_COMPONENT,
                GL_FLOAT,
                (ByteBuffer) null
        );

        // Pengaturan filtering.
        glTexParameteri(
                GL_TEXTURE_2D,
                GL_TEXTURE_MIN_FILTER,
                GL_NEAREST
        );

        glTexParameteri(
                GL_TEXTURE_2D,
                GL_TEXTURE_MAG_FILTER,
                GL_NEAREST
        );

        // Pengaturan texture wrapping.
        glTexParameteri(
                GL_TEXTURE_2D,
                GL_TEXTURE_WRAP_S,
                GL_CLAMP_TO_BORDER
        );

        glTexParameteri(
                GL_TEXTURE_2D,
                GL_TEXTURE_WRAP_T,
                GL_CLAMP_TO_BORDER
        );

        FloatBuffer borderColor = BufferUtils.createFloatBuffer(4);

        borderColor.put(new float[]{
            1.0f, 1.0f, 1.0f, 1.0f
        }).flip();

        glTexParameterfv(
                GL_TEXTURE_2D,
                GL_TEXTURE_BORDER_COLOR,
                borderColor
        );

        // Menghubungkan Depth Texture ke FBO.
        glBindFramebuffer(GL_FRAMEBUFFER, fboId);

        glFramebufferTexture2D(
                GL_FRAMEBUFFER,
                GL_DEPTH_ATTACHMENT,
                GL_TEXTURE_2D,
                depthTextureId,
                0
        );

        // FBO hanya menyimpan informasi kedalaman.
        glDrawBuffer(GL_NONE);
        glReadBuffer(GL_NONE);

        // Memeriksa kelengkapan FBO.
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER)
                != GL_FRAMEBUFFER_COMPLETE) {

            throw new RuntimeException(
                    "Shadow Map Framebuffer tidak lengkap!"
            );
        }

        // Mengembalikan framebuffer dan texture ke kondisi awal.
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    // Digunakan saat Depth Pass.
    public void bindForWriting() {

        glViewport(
                0,
                0,
                SHADOW_WIDTH,
                SHADOW_HEIGHT
        );

        glBindFramebuffer(GL_FRAMEBUFFER, fboId);

        glClear(GL_DEPTH_BUFFER_BIT);
    }

    // Mengembalikan rendering ke layar utama.
    public void unbind(int screenWidth, int screenHeight) {

        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        glViewport(
                0,
                0,
                screenWidth,
                screenHeight
        );
    }

    public int getDepthTextureId() {
        return depthTextureId;
    }

    public void cleanup() {

        if (depthTextureId != 0) {
            glDeleteTextures(depthTextureId);
        }

        if (fboId != 0) {
            glDeleteFramebuffers(fboId);
        }
    }
}