const uint POSITION_BITS        = 20u;
const uint POSITION_MAX_COORD   = 1u << POSITION_BITS;
const uint POSITION_MAX_VALUE   = POSITION_MAX_COORD - 1u;

const uint TEXTURE_BITS         = 15u;
const uint TEXTURE_MAX_COORD    = 1u << TEXTURE_BITS;
const uint TEXTURE_MAX_VALUE    = TEXTURE_MAX_COORD - 1u;

const float VERTEX_SCALE_FACTOR = 32.0 / float(POSITION_MAX_COORD);
const float VERTEX_OFFSET_VALUE = -8.0;
const float TEXTURE_FUZZ_FACTOR = 1.0 / 64.0;
const float TEXTURE_GROW_FACTOR_VALUE = (1.0 - TEXTURE_FUZZ_FACTOR) / TEXTURE_MAX_COORD;

in highp uvec2 a_Position;
in highp vec4 a_Color;
in highp uvec2 a_TexCoord;
in highp uvec4 a_LightAndData;

uvec3 _deinterleave_u20x3(uvec2 data) {
    return (data.xxyy >> uvec3(0, 10, 20)) & 0x3FFu;
}

vec2 _get_texcoord() {
    return vec2(a_TexCoord & TEXTURE_MAX_VALUE) / float(TEXTURE_MAX_COORD);
}

vec2 _get_texcoord_bias() {
    return mix(-TEXTURE_GROW_FACTOR_VALUE, TEXTURE_GROW_FACTOR_VALUE, step(a_TexCoord, uvec2(TEXTURE_BITS)));
}

void _vert_init() {
    _vert_position = (_deinterleave_u20x3(a_Position) * VERTEX_SCALE_FACTOR) + VERTEX_OFFSET_VALUE;
    _vert_color = a_Color;
    _vert_tex_diffuse_coord = _get_texcoord() + _get_texcoord_bias();

    _vert_tex_light_coord = vec2(a_LightAndData.xy) / vec2(256.0);

    _material_params = a_LightAndData[2];
    _draw_id = a_LightAndData[3];
}
