package com.example.huajun.opengldemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.effect.Effect;
import android.media.effect.EffectContext;
import android.media.effect.EffectFactory;
import android.opengl.GLES10;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.nio.ByteBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES10.GL_MAX_TEXTURE_UNITS;
import static android.opengl.GLES10.GL_TEXTURE_2D;
import static android.opengl.GLES10.glBindTexture;
import static android.opengl.GLES10.glEnable;

/**
 * Created by huajun on 18-6-28.
 */

public class GLView extends GLSurfaceView implements SurfaceHolder.Callback{
    private Context mContext = null;
    private GLRender mGLRender = null;

    GLView(Context context) {
        super(context);
        mContext = context;
        setEGLContextClientVersion(2);
        mGLRender = new GLRender();

        setRenderer(mGLRender);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    private final float TOUCH_SCALE_FACTOR = 180.f/320;
    private float mPreviousX;
    private float mPreviousY;
    private float mPreviousDistance;
    private boolean mUpdateDistance = true;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int pointerCount = event.getPointerCount();
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                mUpdateDistance = true;
                break;
            case MotionEvent.ACTION_MOVE:
                if(pointerCount == 1){
                    float x = event.getX();
                    float y = event.getY();
                    float dx = x - mPreviousX;
                    float dy = y - mPreviousY;
                    if(y > getHeight()/2){
                        dx = dx * -1;
                    }
                    if(x <getWidth()/2) {
                        dy = dy * -1;
                    }
                    mGLRender.setAngle(mGLRender.getAngle() + ((dx+dy)*TOUCH_SCALE_FACTOR));
                    mPreviousY = y;
                    mPreviousX = x;
                } else if(pointerCount == 2) {
                    float x1 = event.getX(0);
                    float y1 = event.getY(0);
                    float x2 = event.getX(1);
                    float y2 = event.getY(1);

                    if(mUpdateDistance) {
                        mPreviousDistance =  (float)Math.sqrt(((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2)));
                        mUpdateDistance = false;
                        Log.d("HJ"," mPreviousDistance init "+mPreviousDistance);
                    }
                    else {
                        float distance = (float) Math.sqrt(((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2)));

                        mGLRender.setScale(mGLRender.getScale() + (distance - mPreviousDistance) * 0.001f);

                        mPreviousDistance = distance;
                        Log.d("HJ"," mPreviousDistance update "+mPreviousDistance);
                    }
                }
                requestRender();
                break;

        }
        return true;
    }



    public static class GLRender implements GLSurfaceView.Renderer {
        private static final String TAG = "GLRender";

        private  final  float[] mMVPMatrix = new float[16];
        private  final  float[] mProjectMatrix = new float[16];
        private  final  float[] mViewMatrix = new float[16];
        private  final  float[] mRotateMatrix = new float[16];

        private float mAngle;
        private Square mSquare;
        private Triangle mTriangle;
        private float mScale = 1.f;
        private int[] mTexName;

        int mWidth,mHeight;

        int mSquareProgram;
        private int mTexSamplerHandle;
        private int mTexSamplerHandle2;

        public void onSurfaceCreated(GL10 gl, EGLConfig config){
            // 设置背景颜色
            GLES20.glClearColor(255,0,0,1.0f);

            mSquare = new Square();
            mSquareProgram = mSquare.getProgram();
            mTriangle = new Triangle();

            mTexName = new int[2];
            GLES20.glGenTextures(2,mTexName,0);
            Log.d("HJ","mTexName "+mTexName[0]+" "+mTexName[1]);

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexName[0]);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);


            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexName[1]);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        }

        public void onSurfaceChanged(GL10 gl, int width, int height){
            GLES20.glViewport(0,0,width,height);
            mWidth = width;
            mHeight = height;
            float ratio = (float)width/height;
            Matrix.frustumM(mProjectMatrix,0,-ratio,ratio,-1,1,3,7);

            getPicture();
        }

        public void onDrawFrame(GL10 gl){
            // 画背景颜色
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
            // 设置相机位置
            Matrix.setLookAtM(mViewMatrix,0,0,0,-3,0,0,0,0,1,0);
            // 计算投影及视图变化
            Matrix.multiplyMM(mMVPMatrix,0,mProjectMatrix,0,mViewMatrix,0);
            // 绘制正方形
            mSquare.draw(mTexName);

        }

        public static int loadShader(int type,String shaderCode) {
            // 创建 vertex shader 或者 fragment shader 类型
            int shader = GLES20.glCreateShader(type);
            GLES20.glShaderSource(shader,shaderCode);
            GLES20.glCompileShader(shader);
            return shader;
        }

        public static void checkGLError(String glOperation) {
            int error;
            while ((error = GLES20.glGetError())!=GLES20.GL_NO_ERROR) {
                Log.e(TAG,glOperation+": glError "+error);
                throw new RuntimeException(glOperation+"glError "+error);
            }
        }

        public float getAngle(){
            return mAngle;
        }

        public void setAngle(float angle) {
            mAngle = angle;
        }

        public float getScale() {
            return mScale;
        }
        public void setScale(float scale) {
            mScale = scale;
            Log.d("HJ","scale "+scale);
        }

        public void getPicture() {

            mTexSamplerHandle = GLES20.glGetUniformLocation(mSquareProgram,"sTexture");
            checkGLError("glGetUniformLocation 1");
            Log.d("HJ","handle1 " +mTexSamplerHandle);
            mTexSamplerHandle2 = GLES20.glGetUniformLocation(mSquareProgram,"sTexture2");
            checkGLError("glGetUniformLocation 2");
            Log.d("HJ","handle2 " +mTexSamplerHandle2);
            Bitmap photo2;
            photo2 = BitmapFactory.decodeFile("/storage/emulated/0/DCIM/opengl2.jpg");
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D,mTexName[0]);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, photo2, 0);
            GLES20.glUniform1i(mTexSamplerHandle,0);
            photo2.recycle();

            Bitmap photo;
            photo = BitmapFactory.decodeFile("/storage/emulated/0/DCIM/opengl.jpg");
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexName[1]);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, photo, 0);
            GLES20.glUniform1i(mTexSamplerHandle2,1);
            photo.recycle();
        }
    }


}
