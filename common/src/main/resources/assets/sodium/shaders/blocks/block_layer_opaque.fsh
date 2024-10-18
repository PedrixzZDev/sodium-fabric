#version 330 core

#import <sodium:include/fog.glsl>

in mediump vec4 v_Color;
in mediump vec2 v_TexCoord;
in float v_FragDistance;

in float v_MaterialMipBias;
in float v_MaterialAlphaCutoff;

uniform sampler2D u_BlockTex;
uniform vec4 u_FogColor;
uniform float u_FogStart;
uniform float u_FogEnd;

out vec4 fragColor;

void main() {
    vec4 blockColor = texture(u_BlockTex, v_TexCoord, v_MaterialMipBias);
    vec4 diffuseColor = blockColor * v_Color;

    if (diffuseColor.a < v_MaterialAlphaCutoff) {
        discard;
    }

    float fogFactor = (u_FogEnd - v_FragDistance) / (u_FogEnd - u_FogStart);
    fogFactor = clamp(fogFactor, 0.0, 1.0);

    fragColor = mix(u_FogColor, diffuseColor, fogFactor);
}
