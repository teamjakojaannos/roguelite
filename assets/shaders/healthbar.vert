#version 150

in vec2 in_pos;
in float in_percent;

uniform mat4 projection;
uniform mat4 model;
uniform mat4 view;

out float v_percent;

void main(void) {
    mat4 mvp = projection * view * model;
    gl_Position = mvp * vec4(in_pos.x, in_pos.y, 0.0, 1.0);
    v_percent = in_percent;
}
