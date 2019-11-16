#version 330

in vec2 v_uv;
in vec3 v_tint;

uniform sampler2D in_texture;

out vec4 out_fragColor;

void main(void) {
    out_fragColor = texture(in_texture, v_uv) * vec4(v_tint, 1.0);
}
