#version 150

#moj_import <fog.glsl>

uniform mat4 ProjMat;
uniform mediump vec4 ColorModulator;
uniform mediump vec4 FogColor;
uniform float FogStart;
uniform float FogEnd;

in mediump vec4 vertexColor;
in float vertexDistance;

out mediump vec4 fragColor;

void main() {
    vec4 color = vertexColor * ColorModulator;

    if (color.a < 0.1) {
        discard;
    }

    float newWidth = (FogEnd - FogStart) * 4.0;
    float fade = clamp((FogStart + newWidth - vertexDistance) / newWidth, 0.0, 1.0);

    fragColor.rgb = FogColor.rgb * 0.3 + color.rgb * 0.7;
    fragColor.a = clamp(color.a * fade, 0.0, 1.0);
}
