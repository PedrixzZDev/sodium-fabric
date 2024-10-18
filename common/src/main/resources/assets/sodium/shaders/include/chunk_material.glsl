const uint MATERIAL_USE_MIP_OFFSET = 0u;
const uint MATERIAL_ALPHA_CUTOFF_OFFSET = 1u;

const float ALPHA_CUTOFF_0 = 0.0;
const float ALPHA_CUTOFF_1 = 0.1;
const float ALPHA_CUTOFF_2 = 0.1;
const float ALPHA_CUTOFF_3 = 1.0;

float _material_mip_bias(uint material) {
    return (material & (1u << MATERIAL_USE_MIP_OFFSET)) != 0u ? 0.0 : -4.0;
}

float _material_alpha_cutoff(uint material) {
    uint index = (material >> MATERIAL_ALPHA_CUTOFF_OFFSET) & 3u;
    return index == 0u ? ALPHA_CUTOFF_0 :
           index == 1u ? ALPHA_CUTOFF_1 :
           index == 2u ? ALPHA_CUTOFF_2 :
           ALPHA_CUTOFF_3;
}
