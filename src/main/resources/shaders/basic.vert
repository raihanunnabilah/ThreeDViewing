#version 330 core

layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;
layout (location = 2) in vec2 aTexCoord;
layout (location = 3) in vec3 aFlatNormal;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;
uniform mat4 lightSpaceMatrix;

out vec3 FragPos;
out vec3 Normal;
flat out vec3 FlatNormal;
out vec2 TexCoord;

out vec4 FragPosLightSpace;

void main()
{
    vec4 worldPosition = model * vec4(aPos, 1.0);

    FragPos = worldPosition.xyz;

    Normal = mat3(transpose(inverse(model))) * aNormal;
    FlatNormal = mat3(transpose(inverse(model))) * aFlatNormal;

    TexCoord = aTexCoord;

    // Posisi vertex dari sudut pandang sumber cahaya.
    FragPosLightSpace = lightSpaceMatrix * worldPosition;

    gl_Position = projection * view * worldPosition;
}