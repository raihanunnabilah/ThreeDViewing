package com.grafkom.threedviewing.texture;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL30.*;

public class TextureLoader {

    public static int loadTexture(String resourcePath) {

        ByteBuffer imageBuffer;
        int textureId;

        try (InputStream input = TextureLoader.class
                .getClassLoader()
                .getResourceAsStream(resourcePath)) {

            if (input == null) {
                throw new RuntimeException(
                        "Texture tidak ditemukan: " + resourcePath
                );
            }

            byte[] imageBytes = input.readAllBytes();

            imageBuffer = BufferUtils.createByteBuffer(imageBytes.length);
            imageBuffer.put(imageBytes);
            imageBuffer.flip();

        } catch (IOException e) {
            throw new RuntimeException(
                    "Gagal membaca file texture: " + resourcePath, e
            );
        }

        IntBuffer width = BufferUtils.createIntBuffer(1);
        IntBuffer height = BufferUtils.createIntBuffer(1);
        IntBuffer channels = BufferUtils.createIntBuffer(1);

        ByteBuffer image = STBImage.stbi_load_from_memory(
                imageBuffer,
                width,
                height,
                channels,
                4
        );

        if (image == null) {
            throw new RuntimeException(
                    "Gagal memuat texture: " + STBImage.stbi_failure_reason()
            );
        }

        textureId = glGenTextures();

        glBindTexture(GL_TEXTURE_2D, textureId);

        glTexImage2D(
                GL_TEXTURE_2D,
                0,
                GL_RGBA,
                width.get(0),
                height.get(0),
                0,
                GL_RGBA,
                GL_UNSIGNED_BYTE,
                image
        );

        // Menghasilkan mipmap
        glGenerateMipmap(GL_TEXTURE_2D);

        // Pengaturan awal: Nearest Filtering
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

        // Pengaturan pengulangan tekstur
        glTexParameteri(
                GL_TEXTURE_2D,
                GL_TEXTURE_WRAP_S,
                GL_REPEAT
        );

        glTexParameteri(
                GL_TEXTURE_2D,
                GL_TEXTURE_WRAP_T,
                GL_REPEAT
        );

        glBindTexture(GL_TEXTURE_2D, 0);

        STBImage.stbi_image_free(image);

        return textureId;
    }

    public static void setFiltering(int textureId, boolean bilinear) {

        glBindTexture(GL_TEXTURE_2D, textureId);

        if (bilinear) {

            glTexParameteri(
                    GL_TEXTURE_2D,
                    GL_TEXTURE_MIN_FILTER,
                    GL_LINEAR_MIPMAP_LINEAR
            );

            glTexParameteri(
                    GL_TEXTURE_2D,
                    GL_TEXTURE_MAG_FILTER,
                    GL_LINEAR
            );

        } else {

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
        }

        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public static void cleanup(int textureId) {
        glDeleteTextures(textureId);
    }
}