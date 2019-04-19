package cn.lenovo.face.realtime;

import android.content.Context;
import android.graphics.ImageFormat;

import java.util.Timer;
import java.util.TimerTask;

import cn.lenovo.face.Contans;
import cn.lenovo.face.utils.Log;


/**
 * Created by baohm1 on 2018/3/5.
 */

public class FrameManagerThread {
    private static final String TAG = "FrameManagerThread";
    private FrameManager mFManager = null;
    private FrameProcess mFrameProcess = null;
    private Context mContext = null;

    private boolean isFrameGrabbing = false;
    private boolean isFrameDetecting = false;

    private Timer mTimerFaceDetect = null;

    private int mLength;
    private boolean isInit = false;

    private static FrameManagerThread sFrameManagerThread = null;

    public static FrameManagerThread getInstance() {
        if (null == sFrameManagerThread) {
            sFrameManagerThread = new FrameManagerThread();
        }
        return sFrameManagerThread;
    }

    public void init(Context context, int w, int h, int format, int angle, int mirror) {
        if (isInit || w == 0 || h == 0)
            return;

        mContext = context;
        isInit = true;
        mLength = (int)(w * h * SIZE_FORMAT(format));
        mFrameProcess = new FrameProcess(context, w, h, format, angle, mirror);
        initFaceFrameManager(mLength);
        Log.d(TAG, "FrameManagerThread init ...");
    }

    public void start() {
        Log.d(TAG, "start: ");
        startFaceDetect();
    }

    public void stop() {
        Log.d(TAG, "stop: ");
        stopFaceDetect();
        while(isFrameGrabbing || isFrameDetecting) {
            try {
                Log.d(TAG, "Wait frame handle...");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        mFrameProcess.clearCache();
    }

    public int pushFrame(byte[] data) {
        if (isFrameGrabbing)
            return 0;

        isFrameGrabbing = true;
        FrameManager.FrameBuffer fBuffer = mFManager.queueBuffer();
        if (fBuffer != null) {
//			System.out.println("=========Write Buffer:" + fBuffer.index + ", counter = " + fBuffer.counter);
//            Log.d(TAG, "putFrameData: 0");
            System.arraycopy(data, 0, fBuffer.data, 0, mLength);
//            Log.d(TAG, "putFrameData: 1");
            fBuffer.isBusy = false;
            fBuffer.isVaild = true;
        }
        isFrameGrabbing = false;
        return mLength;
    }

    public void setCameraOrientation(int angle, int mirror) {
        mFrameProcess.setCameraOrientation(angle, mirror);
    }

    private void initFaceFrameManager(int length) {
        mFManager = new FrameManager();
        for (int i=0; i<FrameManager.FRAME_BUFFER_NUM; i++) {
            mFManager.frameBuffers[i].data = new byte[length];
            mFManager.frameBuffers[i].length = length;
        }
        Log.d(TAG, "initFaceFrameManager: Frame length = " + length);
    }

    private void startFaceDetect() {
        TimerTask faceDetect = new TimerTask() {
            @Override
            public void run() {
                isFrameDetecting = true;
                FrameManager.FrameBuffer fBuffer = mFManager.dequeueBuffer();
                if (fBuffer != null) {
//					System.out.println("=========Read Buffer:" + fBuffer.index + ", counter = " + fBuffer.counter);
                    mFrameProcess.onPreviewFrame(fBuffer.data);
//					int faceNum = mFrameProcess.onPreviewFrame(fBuffer.frame);
//					if (faceNum > 0) {
//						ImgUtils.saveImage(fBuffer.frame);
//					}
                    fBuffer.isBusy = false;
                    fBuffer.isVaild = false;

//					mFps.fps();
//					saveImage(fBuffer.frame);
                }
                isFrameDetecting = false;
            }
        };
        mTimerFaceDetect = new Timer();
        mTimerFaceDetect.schedule(faceDetect, 0, Contans.FrameDetectPeriod);
        return;
    }

    private void stopFaceDetect() {
        if (mTimerFaceDetect != null) {
            mTimerFaceDetect.cancel();
            mTimerFaceDetect = null;
        }
        return;
    }

    private float SIZE_FORMAT(int format) {
        float ret = 2;
        switch (format) {
            case ImageFormat.YV12:
                break;
            case ImageFormat.NV21:
                ret = 1.5f;
                break;
        }

        return ret;
    }
}
