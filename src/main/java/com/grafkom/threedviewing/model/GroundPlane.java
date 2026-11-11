package com.grafkom.threedviewing.model;

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class GroundPlane {

    private final int vaoId;
    private final int vboId;
    private final int vertexCount;

    public GroundPlane() {

        float size = 10.0f;
        float y = -1.05f;
        float s = size / 2.0f;

        // Format vertex:
        // Posisi (3), UV (2), Normal Smooth (3), Normal Flat (3).
        float[] vertices = {

            // Triangle 1
            -s, y, -s, 0, 0,  0, 1, 0,  0, 1, 0,
             s, y,  s, 4, 4,  0, 1, 0,  0, 1, 0,
             s, y, -s, 4, 0,  0, 1, 0,  0, 1, 0,

            // Triangle 2
            -s, y, -s, 0, 0,  0, 1, 0,  0, 1, 0,
            -s, y,  s, 0, 4,  0, 1, 0,  0, 1, 0,
             s, y,  s, 4, 4,  0, 1, 0,  0, 1, 0
        };

        vertexCount = vertices.length / 11;

        FloatBuffer buffer = BufferUtils.createFloatBuffer(vertices.length);
        buffer.put(vertices).flip();

        vaoId = glGenVertexArrays();
        vboId = glGenBuffers();

        glBindVertexArray(vaoId);

        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);

        int stride = 11 * Float.BYTES;

        // Attribute 0: posisi
        glVertexAttribPointer(
                0, 3, GL_FLOAT, false, stride, 0
        );
        glEnableVertexAttribArray(0);

        // Attribute 1: normal smooth
        glVertexAttribPointer(
                1, 3, GL_FLOAT, false, stride, 5 * Float.BYTES
        );
        glEnableVertexAttribArray(1);

        // Attribute 2: koordinat tekstur
        glVertexAttribPointer(
                2, 2, GL_FLOAT, false, stride, 3 * Float.BYTES
        );
        glEnableVertexAttribArray(2);

        // Attribute 3: normal flat
        glVertexAttribPointer(
                3, 3, GL_FLOAT, false, stride, 8 * Float.BYTES
        );
        glEnableVertexAttribArray(3);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void render() {

        glBindVertexArray(vaoId);

        glDrawArrays(GL_TRIANGLES, 0, vertexCount);

        glBindVertexArray(0);
    }

    public void cleanup() {

        glDeleteBuffers(vboId);
        glDeleteVertexArrays(vaoId);
    }
}