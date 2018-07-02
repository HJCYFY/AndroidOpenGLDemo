package com.example.huajun.opengldemo;

import android.content.Context;
import android.graphics.Canvas;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

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

        public void onSurfaceCreated(GL10 gl, EGLConfig config){
            // 设置背景颜色
            GLES20.glClearColor(255,0,0,1.0f);
            mSquare = new Square();
            mTriangle = new Triangle();
        }

        public void onSurfaceChanged(GL10 gl, int width, int height){
            GLES20.glViewport(0,0,width,height);
            float ratio = (float)width/height;
            Matrix.frustumM(mProjectMatrix,0,-ratio,ratio,-1,1,3,7);
        }

        public void onDrawFrame(GL10 gl){
            float[] scratch = new float[16];
            // 画背景颜色
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
            // 设置相机位置
            Matrix.setLookAtM(mViewMatrix,0,0,0,-3,0,0,0,0,1,0);
            // 计算投影及视图变化
            Matrix.multiplyMM(mMVPMatrix,0,mProjectMatrix,0,mViewMatrix,0);
            // 绘制正方形
            mSquare.draw(mMVPMatrix);

            Matrix.setRotateM(mRotateMatrix,0,mAngle,0,0,1.f);
            Matrix.multiplyMM(scratch,0,mMVPMatrix,0,mRotateMatrix,0);
            Matrix.scaleM(scratch,0,mScale,mScale,mScale);

            mTriangle.draw(scratch);
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
    }


}
