package com.example.huajun.opengldemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.effect.Effect;
import android.media.effect.EffectContext;
import android.media.effect.EffectFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by huajun on 18-6-28.
 */

public class Square {
    public final String vertexShaderCode =
            "attribute vec4 vPosition;" +
            "attribute vec2 aTexCoord;" +
            "varying vec2 vTexCoord;" +
            "void main() {" +
            "   gl_Position = vPosition;" +
            "   vTexCoord = aTexCoord;" +
            "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
            "varying vec2 vTexCoord;" +
            "uniform sampler2D sTexture;" +
            "void main() {" +
            "   gl_FragColor = texture2D(sTexture,vTexCoord);" +
            "}";

    private FloatBuffer vertexBuffer;
    private FloatBuffer textureBuffer;
    private int mProgram;
    private int mPositionHandle;
    private int mTexCoordHandle;
    private int mTexSamplerHandle;

    // 每个顶点的坐标数量
    static final int COORDS_PER_VERTEX = 3;
    static float squareCoords[] = {
            -1.f,  -1.f, 0.0f,   // top left
            1.f, -1.f, 0.0f,   // bottom left
            -1.f, 1.f, 0.0f,   // bottom right
            1.f, 1.f, 0.0f
    };

    // 坐标系以屏幕左上角为原点
    private static float texCoords[] = {
            0.f,1.f,
            1.f,1.f,
            0.f,0.f,
            1.f,0.f,
    };

    private final int vertexStride = COORDS_PER_VERTEX * 4; // COORDS_PER_VERTEX * sizeof(float)

    public Square(){
        ByteBuffer bb = ByteBuffer.allocateDirect(squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);

        ByteBuffer tb = ByteBuffer.allocateDirect(texCoords.length * 4);
        tb.order(ByteOrder.nativeOrder());
        textureBuffer = tb.asFloatBuffer();
        textureBuffer.put(texCoords);
        textureBuffer.position(0);

        // prepare shaders and OpenGL program
        int vertexShader = GLView.GLRender.loadShader( GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = GLView.GLRender.loadShader( GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);
    }

    public void draw(int texture) {
        // 创建帧缓冲对象 FBO
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,0);
        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);
        // 渲染过程中禁止颜色混合
//        GLES20.glDisable(GLES20.GL_BLEND);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer( mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

        mTexCoordHandle = GLES20.glGetAttribLocation(mProgram,"aTexCoord");
        GLES20.glEnableVertexAttribArray(mTexCoordHandle);
        GLES20.glVertexAttribPointer(mTexCoordHandle,2,GLES20.GL_FLOAT,false,8,textureBuffer);


        mTexSamplerHandle = GLES20.glGetAttribLocation(mProgram,"sTexture");
        GLES20.glUniform1i(mTexSamplerHandle,0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,texture);
        // Draw the square
        GLES20.glDrawArrays(
                GLES20.GL_TRIANGLE_STRIP,0,4);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTexCoordHandle);
    }
}
