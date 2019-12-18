#version 330

in float v_percent;

uniform float health;

out vec4 out_fragColor;

void main(void) {
    if (v_percent <= health){
        out_fragColor = vec4(0.0, 1.0, 0.0, 1.0);
    } else {
        out_fragColor = vec4(1.0, 0.0, 0.0, 1.0);
    }
}
