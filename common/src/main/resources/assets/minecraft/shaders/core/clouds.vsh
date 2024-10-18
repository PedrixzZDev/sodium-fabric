#version 150

in mediump vec3 Position;
in mediump vec4 Color;

uniform highp mat4 ModelViewMat;
uniform highp mat4 ProjMat;

out mediump float vertexDistance;
out mediump vec4 vertexColor;

void main() {
    vec4 pos = ModelViewMat * vec4(Position, 1.0);
    gl_Position = ProjMat * pos;
    vertexDistance = distance(vec3(0.0), pos.xyz);
    vertexColor = Color;
}
