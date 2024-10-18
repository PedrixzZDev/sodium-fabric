#version 330 core

#import <sodium:include/fog.glsl>
#import <sodium:include/chunk_vertex.glsl>
#import <sodium:include/chunk_matrices.glsl>
#import <sodium:include/chunk_material.glsl>

out mediump vec4 v_Color;
out mediump vec2 v_TexCoord;

out float v_MaterialMipBias;
out float v_MaterialAlphaCutoff;

out float v_FragDistance;

uniform int u_FogShape;
uniform vec3 u_RegionOffset;

uniform sampler2D u_LightTex;

uvec3 _get_relative_chunk_coord(uint pos) {
    return uvec3(pos) >> uvec3(5, 0, 2);
}

vec3 _get_draw_translation(uint pos) {
    return _get_relative_chunk_coord(pos) * vec3(16.0);
}

void main() {
    _vert_init();

    vec3 translation = u_RegionOffset + _get_draw_translation(_draw_id);
    vec3 position = _vert_position;
    position += translation;

    v_FragDistance = getFragDistance(u_FogShape, position);

    gl_Position = u_ProjectionMatrix * u_ModelViewMatrix * vec4(position, 1.0);

    vec4 lightColor = texture(u_LightTex, _vert_tex_light_coord);
    v_Color = _vert_color * lightColor;
    v_TexCoord = _vert_tex_diffuse_coord;

    v_MaterialMipBias = _material_params.mipBias;
    v_MaterialAlphaCutoff = _material_params.alphaCutoff;
}
