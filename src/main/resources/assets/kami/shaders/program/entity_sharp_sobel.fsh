#version 110

uniform sampler2D DiffuseSampler;

varying vec2 texCoord;
varying vec2 oneTexel;

void main(){
    vec4 center = texture2D(DiffuseSampler, texCoord);
    vec4 left = texture2D(DiffuseSampler, texCoord - vec2(oneTexel.x, 0.0));
    vec4 right = texture2D(DiffuseSampler, texCoord + vec2(oneTexel.x, 0.0));
    vec4 up = texture2D(DiffuseSampler, texCoord - vec2(0.0, oneTexel.y));
    vec4 down = texture2D(DiffuseSampler, texCoord + vec2(0.0, oneTexel.y));
    float leftDiff  = left.a - center.a;
    float rightDiff = right.a - center.a;
    float upDiff    = up.a - center.a;
    float downDiff  = down.a - center.a;
    float total = leftDiff + rightDiff + downDiff + upDiff;
    vec3 outColor = center.rgb * center.a + left.rgb * left.a + right.rgb * right.a + up.rgb * up.a + down.rgb * down.a;
    gl_FragColor = vec4(outColor / total, clamp(total, 0.0, 1.0));
}
