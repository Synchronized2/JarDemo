package cn.lenovo.face.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.hardware.Camera;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import java.lang.reflect.Method;

import cn.face.sdk.FaceInterface;

public class CameraUtils {
    private static final String TAG = "CameraUtils";

    public static int getCWFaceOrientation(Context context, int cameraID) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraID, info);

        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int rotation = display.getRotation();
        int orientation;
        switch (rotation) {
            case Surface.ROTATION_0:
            default:
                orientation = FaceInterface.cw_img_angle_t.CW_IMAGE_ANGLE_0;
                break;
            case Surface.ROTATION_90:
                orientation = FaceInterface.cw_img_angle_t.CW_IMAGE_ANGLE_90;
                break;
            case Surface.ROTATION_180:
                orientation = FaceInterface.cw_img_angle_t.CW_IMAGE_ANGLE_180;
                break;
            case Surface.ROTATION_270:
                orientation = FaceInterface.cw_img_angle_t.CW_IMAGE_ANGLE_270;
                break;
        }
        orientation = (orientation + info.orientation/90)%4;

        Log.d(TAG, "getCWFaceOrientation: info.orientation = " + info.orientation);
        Log.d(TAG, "getCWFaceOrientation: rotation = " + rotation);
        Log.d(TAG, "getCWFaceOrientation: orientation = " + orientation);
        return orientation;
    }

    /**
     * 通过屏幕参数、相机预览尺寸计算布局参数
     */
    public static Point getAdjustSize(Context context, Camera camera) {
        Camera.Size previewSize = camera.getParameters().getPreviewSize();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        try {
            @SuppressWarnings("rawtypes")
            Class c = Class.forName("android.view.Display");
            @SuppressWarnings("unchecked")
            Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
            method.invoke(display, metrics);
        } catch (Exception e) {
            e.printStackTrace();
        }
        float scale;
        if (isPortrait(context)) {
            scale = Math.min(metrics.widthPixels * 1.0f / previewSize.height,
                    metrics.heightPixels * 1.0f / previewSize.width);
        } else {
            scale = Math.min(metrics.heightPixels * 1.0f / previewSize.height,
                    metrics.widthPixels * 1.0f / previewSize.width);
        }
        int layout_width = (int) (scale * previewSize.height);
        int layout_height = (int) (scale * previewSize.width);
        if (isPortrait(context)) {
            return new Point(layout_width, layout_height);
        } else {
            return new Point(layout_height, layout_width);
        }
    }

    public static boolean isPortrait(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }
}
