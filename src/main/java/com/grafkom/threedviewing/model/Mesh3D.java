package com.grafkom.threedviewing.model;

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Mesh3D {

    private final int vaoId;
    private final int vboId;
    private final int vertexCount;

    public Mesh3D(float size) {

        float s = size / 2.0f;

        float[] vertices = {
            // Posisi                 // UV       // Normal Flat
            // Front
            -s,-s, s, 0,0,  0,0,1,  0,0,1,
             s,-s, s, 1,0,  0,0,1,  0,0,1,
             s, s, s, 1,1,  0,0,1,  0,0,1,
            -s,-s, s, 0,0,  0,0,1,  0,0,1,
             s, s, s, 1,1,  0,0,1,  0,0,1,
            -s, s, s, 0,1,  0,0,1,  0,0,1,

            // Back
             s,-s,-s, 0,0,  0,0,-1,  0,0,-1,
            -s,-s,-s, 1,0,  0,0,-1,  0,0,-1,
            -s, s,-s, 1,1,  0,0,-1,  0,0,-1,
             s,-s,-s, 0,0,  0,0,-1,  0,0,-1,
            -s, s,-s, 1,1,  0,0,-1,  0,0,-1,
             s, s,-s, 0,1,  0,0,-1,  0,0,-1,

            // Left
            -s,-s,-s, 0,0, -1,0,0, -1,0,0,
            -s,-s, s, 1,0, -1,0,0, -1,0,0,
            -s, s, s, 1,1, -1,0,0, -1,0,0,
            -s,-s,-s, 0,0, -1,0,0, -1,0,0,
            -s, s, s, 1,1, -1,0,0, -1,0,0,
            -s, s,-s, 0,1, -1,0,0, -1,0,0,

            // Right
             s,-s, s, 0,0,  1,0,0,  1,0,0,
             s,-s,-s, 1,0,  1,0,0,  1,0,0,
             s, s,-s, 1,1,  1,0,0,  1,0,0,
             s,-s, s, 0,0,  1,0,0,  1,0,0,
             s, s,-s, 1,1,  1,0,0,  1,0,0,
             s, s, s, 0,1,  1,0,0,  1,0,0,

            // Top
            -s, s, s, 0,0,  0,1,0,  0,1,0,
             s, s, s, 1,0,  0,1,0,  0,1,0,
             s, s,-s, 1,1,  0,1,0,  0,1,0,
            -s, s, s, 0,0,  0,1,0,  0,1,0,
             s, s,-s, 1,1,  0,1,0,  0,1,0,
            -s, s,-s, 0,1,  0,1,0,  0,1,0,

            // Bottom
            -s,-s,-s, 0,0,  0,-1,0,  0,-1,0,
             s,-s,-s, 1,0,  0,-1,0,  0,-1,0,
             s,-s, s, 1,1,  0,-1,0,  0,-1,0,
            -s,-s,-s, 0,0,  0,-1,0,  0,-1,0,
             s,-s, s, 1,1,  0,-1,0,  0,-1,0,
            -s,-s, s, 0,1,  0,-1,0,  0,-1,0
        };

        // Tambahkan normal smooth berdasarkan posisi vertex.
        // Format awal: posisi (3), UV (2), normal smooth (3), normal flat (3).
        float[] expandedVertices = new float[vertices.length / 11 * 11];

        for (int i = 0; i < vertices.length; i += 11) {

            float x = vertices[i];
            float y = vertices[i + 1];
            float z = vertices[i + 2];

            float length = (float) Math.sqrt(x * x + y * y + z * z);

            expandedVertices[i] = x;
            expandedVertices[i + 1] = y;
            expandedVertices[i + 2] = z;

            expandedVertices[i + 3] = vertices[i + 3];
            expandedVertices[i + 4] = vertices[i + 4];

            expandedVertices[i + 5] = x / length;
            expandedVertices[i + 6] = y / length;
            expandedVertices[i + 7] = z / length;

            expandedVertices[i + 8] = vertices[i + 8];
            expandedVertices[i + 9] = vertices[i + 9];
            expandedVertices[i + 10] = vertices[i + 10];
        }

        vertexCount = expandedVertices.length / 11;

        FloatBuffer buffer = BufferUtils.createFloatBuffer(expandedVertices.length);
        buffer.put(expandedVertices).flip();

        vaoId = glGenVertexArrays();
        vboId = glGenBuffers();

        glBindVertexArray(vaoId);

        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);

        int stride = 11 * Float.BYTES;

        // Attribute 0: posisi
        glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0);
        glEnableVertexAttribArray(0);

        // Attribute 1: normal smooth
        glVertexAttribPointer(1, 3, GL_FLOAT, false, stride, 5 * Float.BYTES);
        glEnableVertexAttribArray(1);

        // Attribute 2: koordinat tekstur
        glVertexAttribPointer(2, 2, GL_FLOAT, false, stride, 3 * Float.BYTES);
        glEnableVertexAttribArray(2);

        // Attribute 3: normal flat
        glVertexAttribPointer(3, 3, GL_FLOAT, false, stride, 8 * Float.BYTES);
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