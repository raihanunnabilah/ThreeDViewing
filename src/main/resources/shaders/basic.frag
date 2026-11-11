#version 330 core

in vec3 FragPos;
in vec3 Normal;
flat in vec3 FlatNormal;
in vec2 TexCoord;

in vec4 FragPosLightSpace;

out vec4 FragColor;

uniform vec3 objectColor;
uniform vec3 lightPos;
uniform vec3 viewPos;

uniform int lightingMode;
uniform int shadingMode;

uniform sampler2D textureSampler;
uniform sampler2D shadowMap;

// Menghitung tingkat bayangan menggunakan Shadow Mapping.
float calculateShadow(vec4 fragPosLightSpace, vec3 normal, vec3 lightDir)
{
    // Mengubah koordinat light space ke rentang 0 sampai 1.
    vec3 projectedCoords = fragPosLightSpace.xyz
            / fragPosLightSpace.w;

    projectedCoords = projectedCoords * 0.5 + 0.5;

    // Jika berada di luar area shadow map, tidak menghasilkan bayangan.
    if (projectedCoords.z > 1.0
            || projectedCoords.x < 0.0
            || projectedCoords.x > 1.0
            || projectedCoords.y < 0.0
            || projectedCoords.y > 1.0) {
        return 0.0;
    }

    // Mengambil kedalaman dari shadow map.
    float closestDepth = texture(
            shadowMap,
            projectedCoords.xy
    ).r;

    float currentDepth = projectedCoords.z;

    // Bias untuk mengurangi Shadow Acne.
    float bias = max(
            0.05 * (1.0 - dot(normal, lightDir)),
            0.005
    );

    // Membandingkan kedalaman fragment dengan shadow map.
    if (currentDepth - bias > closestDepth) {
        return 0.8;
    }

    return 0.0;
}

void main()
{
    vec4 textureColor = texture(textureSampler, TexCoord);

    float ambientStrength = 0.2;
    vec3 ambient = ambientStrength * vec3(1.0);

    vec3 norm;

    // Flat Shading atau Smooth Shading.
    if (shadingMode == 0) {
        norm = normalize(FlatNormal);
    } else {
        norm = normalize(Normal);
    }

    vec3 lightDir = normalize(lightPos - FragPos);
    vec3 viewDir = normalize(viewPos - FragPos);

    // Perhitungan diffuse.
    float diff = max(dot(norm, lightDir), 0.0);
    vec3 diffuse = diff * vec3(1.0);

    // Perhitungan specular.
    float specularStrength = 0.5;
    float specular = 0.0;

    if (lightingMode == 0) {

        // Phong Lighting.
        vec3 reflectDir = reflect(-lightDir, norm);

        specular = pow(
                max(dot(viewDir, reflectDir), 0.0),
                32.0
        );

    } else {

        // Blinn-Phong Lighting.
        vec3 halfwayDir = normalize(lightDir + viewDir);

        specular = pow(
                max(dot(norm, halfwayDir), 0.0),
                32.0
        );
    }

    vec3 specularColor =
            specularStrength * specular * vec3(1.0);

    // Menghitung bayangan.
    float shadow = calculateShadow(
            FragPosLightSpace,
            norm,
            lightDir
    );

    // Area yang terkena bayangan mendapatkan intensitas 0.2.
    float shadowFactor = 1.0 - shadow;

    vec3 lighting =
            ambient
            + shadowFactor * diffuse;

    vec3 result =
            lighting * objectColor * textureColor.rgb
            + shadowFactor * specularColor;

    FragColor = vec4(result, textureColor.a);
}