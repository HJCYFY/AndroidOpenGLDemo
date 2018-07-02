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
            // this matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "attribute vec4 vPosition;" +
            "attribute vec2 aTexCoord;" +
            "varying vec2 vTexCoord;" +
//            "uniform mat4 uMVPMatrix;" +
            "void main() {" +
            // the matrix must be included as a modifier of gl_Position.
            // Note that the uMVPMatrix factor *must be first* in order
            // for the matrix multiplication product to be correct.
            " gl_Position = vPosition;" +
            " vTexCoord = aTexCoord;" +
            "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
            "uniform vec4 vColor;" +
            "varying vec2 vTexCoord;" +
            "uniform sampler2D sTexture;" +
            "void main() {" +
            "gl_FragColor = vColor;" +
            "gl_FragColor = texture2D(sTexture,vTexCoord);" +
            "}";

    private FloatBuffer vertexBuffer;
    private FloatBuffer textureBuffer;
    private int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mTexCoordHandle;
    private int mTexSamplerHandler;
    private int[] texName;

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
            1.f,0.f,
            1.f,1.f,
            0.f,0.f,
            0.f,1.f
    };

    private final int vertexStride = COORDS_PER_VERTEX * 4; // COORDS_PER_VERTEX * sizeof(float)

    float color[] = {0.f,0.f,1.f,0.0f};

    public Square(){
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                squareCoords.length * 4);
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
        int vertexShader = GLView.GLRender.loadShader(
                GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = GLView.GLRender.loadShader(
                GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);


        texName = new int[1];
        GLES20.glGenTextures(1,texName,0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,texName[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_REPEAT);

    }

    public void draw(float[] mvpMatrix) {

        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);
        GLES20.glDisable(GLES20.GL_BLEND);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(
                mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        // add Texture (预先在存储中放一张图像)
        Bitmap bitmap = BitmapFactory.decodeFile("/storage/emulated/0/DCIM/opengl.jpg");
        Log.d("HJ","w = "+bitmap.getWidth());
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D,0,bitmap,0);
        bitmap.recycle();

        mTexCoordHandle = GLES20.glGetAttribLocation(mProgram,"aTexCoord");
        GLES20.glEnableVertexAttribArray(mTexCoordHandle);
        GLES20.glVertexAttribPointer(mTexCoordHandle,2,GLES20.GL_FLOAT,false,8,textureBuffer);

        mTexSamplerHandler = GLES20.glGetAttribLocation(mProgram,"sTexture");
        GLES20.glUniform1i(mTexSamplerHandler,0);

        // Draw the square
        GLES20.glDrawArrays(
                GLES20.GL_TRIANGLE_STRIP,0,4);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTexCoordHandle);
    }
}
