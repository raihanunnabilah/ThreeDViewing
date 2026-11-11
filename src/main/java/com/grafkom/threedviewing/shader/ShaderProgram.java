package com.grafkom.threedviewing.shader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import static org.lwjgl.opengl.GL20.*;

public class ShaderProgram {

    private final int programId;

    public ShaderProgram(
            String vertexShaderPath,
            String fragmentShaderPath
    ) {

        int vertexShader = createShader(
                vertexShaderPath,
                GL_VERTEX_SHADER
        );

        int fragmentShader = createShader(
                fragmentShaderPath,
                GL_FRAGMENT_SHADER
        );

        programId = glCreateProgram();

        glAttachShader(programId, vertexShader);
        glAttachShader(programId, fragmentShader);

        glLinkProgram(programId);

        if (glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE) {

            String error = glGetProgramInfoLog(programId);

            glDeleteProgram(programId);

            throw new RuntimeException(
                    "Shader linking gagal:\n" + error
            );
        }

        glDetachShader(programId, vertexShader);
        glDetachShader(programId, fragmentShader);

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
    }

    private int createShader(String path, int type) {

        String source = loadShaderSource(path);

        int shaderId = glCreateShader(type);

        glShaderSource(shaderId, source);
        glCompileShader(shaderId);

        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == GL_FALSE) {

            String error = glGetShaderInfoLog(shaderId);

            glDeleteShader(shaderId);

            throw new RuntimeException(
                    "Shader compilation gagal pada "
                    + path + ":\n" + error
            );
        }

        return shaderId;
    }

    private String loadShaderSource(String path) {

        try (InputStream input = getClass()
                .getClassLoader()
                .getResourceAsStream(path)) {

            if (input == null) {
                throw new RuntimeException(
                        "File shader tidak ditemukan: " + path
                );
            }

            return new String(
                    input.readAllBytes(),
                    StandardCharsets.UTF_8
            );

        } catch (IOException e) {

            throw new RuntimeException(
                    "Gagal membaca file shader: " + path,
                    e
            );
        }
    }

    public void bind() {
        glUseProgram(programId);
    }

    public void unbind() {
        glUseProgram(0);
    }

    public void setUniform(
            String name,
            int value
    ) {

        int location = glGetUniformLocation(programId, name);

        if (location != -1) {
            glUniform1i(location, value);
        }
    }

    public void setUniform(
            String name,
            float value
    ) {

        int location = glGetUniformLocation(programId, name);

        if (location != -1) {
            glUniform1f(location, value);
        }
    }

    public void setUniform(
            String name,
            Vector3f value
    ) {

        int location = glGetUniformLocation(programId, name);

        if (location != -1) {

            glUniform3f(
                    location,
                    value.x,
                    value.y,
                    value.z
            );
        }
    }

    public void setUniform(
            String name,
            Matrix4f value
    ) {

        int location = glGetUniformLocation(programId, name);

        if (location != -1) {

            FloatBuffer buffer = BufferUtils
                    .createFloatBuffer(16);

            value.get(buffer);

            glUniformMatrix4fv(
                    location,
                    false,
                    buffer
            );
        }
    }

    public void cleanup() {

        unbind();

        glDeleteProgram(programId);
    }

    public int getProgramId() {
        return programId;
    }
}