#version 330

in vec2 in_pos;
in vec2 in_uv;
in vec3 in_tint;

uniform mat4 projection;
uniform mat4 model;
uniform mat4 view;

out vec2 v_uv;
out vec3 v_tint;

void main(void) {
    mat4 mvp = projection * view * model;
    gl_Position = mvp * vec4(in_pos.x, in_pos.y, 0.0, 1.0);

    v_uv = in_uv;
    v_tint = in_tint;
}
