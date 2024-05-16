/*
 Copyright (c) 2014-2022, Stephen Gold

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the copyright holder nor the names of its contributors
      may be used to endorse or promote products derived from this software
      without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/*
 * fragment shader used by dome20.j3md
 */
#import "Common/ShaderLib/GLSLCompat.glsllib"
uniform vec4 m_ClearColor;
varying vec2 skyTexCoord;

#ifdef HAS_STARS
        uniform sampler2D m_StarsColorMap;
#endif

#ifdef HAS_OBJECT0
        uniform vec4 m_Object0Color;
	uniform sampler2D m_Object0ColorMap;
	varying vec2 object0Coord;
#endif

#ifdef HAS_OBJECT1
        uniform vec4 m_Object1Color;
	uniform sampler2D m_Object1ColorMap;
	varying vec2 object1Coord;
#endif

#ifdef HAS_HAZE
        uniform sampler2D m_HazeAlphaMap;
        uniform vec4 m_HazeColor;
#endif

vec4 mixColors(vec4 color0, vec4 color1) {
        vec4 result;
        float a0 = color0.a * (1.0 - color1.a);
        result.a = a0 + color1.a;
        if (result.a > 0.0) {
                result.rgb = (a0 * color0.rgb + color1.a * color1.rgb)/result.a;
        } else {
                result.rgb = vec3(0.0);
        }
        return result;
}

void main() {
        #ifdef HAS_STARS
                vec4 stars = texture2D(m_StarsColorMap, skyTexCoord);
        #else
                vec4 stars = vec4(0.0);
        #endif

        vec4 objects = vec4(0.0);

        #ifdef HAS_OBJECT0
                if (floor(object0Coord.s) == 0.0 &&
                    floor(object0Coord.t) == 0.0) {
                        objects = m_Object0Color;
                        objects *= texture2D(m_Object0ColorMap, object0Coord);
                }
	#endif

        #ifdef HAS_OBJECT1
                if (floor(object1Coord.s) == 0.0 &&
                    floor(object1Coord.t) == 0.0) {
                        vec4 object1 = m_Object1Color;
                        object1 *= texture2D(m_Object1ColorMap, object1Coord);
                        objects = mixColors(objects, object1);
                }
	#endif

        vec4 color = mixColors(stars, objects);

        vec4 clear = m_ClearColor;
	#ifdef HAS_HAZE
                vec4 haze = m_HazeColor;
                haze.a *= texture2D(m_HazeAlphaMap, skyTexCoord).r;
	        clear = mixColors(clear, haze);
	#endif
        color = mixColors(color, clear);
        // Bright parts of objects shine through the clear areas.
        color.rgb += objects.rgb * objects.a * (1.0 - clear.rgb) * clear.a;

	gl_FragColor = color;
}