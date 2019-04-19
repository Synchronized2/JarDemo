package cn.lenovo.face;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;

import cn.lenovo.face.camera.CameraPreview;
import cn.lenovo.face.jsonfile.NisBean;
import cn.lenovo.face.jsonfile.ReadFileUtils;


/**
 * Created by baohm1 on 2018/3/5.
 */

public class Contans {
    //是否显示人脸信息
    public static boolean ENABLE_FACE_VIEW = true;
    //是否打印log
    public static boolean LOG_DEBUG = true;

    public static String PAD_ID = "IN_001";
    /**
     * Camera Param
     */
    // 摄像头分辨率
    public static int PREVIEW_W = 1920;
    public static int PREVIEW_H = 1200;

    //ROI,人脸识别有效区域
    public static Rect RoiRect = null;
//    public static final Rect RoiRect = new Rect(0, 0, PREVIEW_H, PREVIEW_W/2);

    //预览画面分辨率
    public static int PREVIEW_SURFACE_W = 1800;
    public static int PREVIEW_SURFACE_H = 2560;

    //FitToParent：自适应，长宽比例不变；NoBlank：画面填满view框，长宽比例可变
    public static CameraPreview.LayoutMode DEFAULT_LAYOUT_MODE = CameraPreview.LayoutMode.FitToParent;
    //cameraID，前置摄像头
    public static int DEFAULT_CAMERA = Camera.CameraInfo.CAMERA_FACING_FRONT;
    //camera数据格式，这个最好不要修改
    public static int PREVIEW_DATA_FORMAT = ImageFormat.NV21;
    //保存临时文件的地址
    public static String CROP_IMG_PATH = "/sdcard/faces";
    public static int MAX_NUMBER_CROP_IMG = 10000;
    public static int REPEAT_TIME_CHECK_CACHE = 24 * 3600 * 1000; //ms

    /**
     * Face Det and Track param
     */
    public static String sLicencePath = Environment.getExternalStorageDirectory() + File.separator + "CWModels";
    // 授权码 ，由云从科技提供，也可调用网络授权接口cwGetLicence获取
    public static String sLicence="";

    public static float FaceSimilarThreshold = 1.05f;    //人脸相似度阈值，默认为0.84，范围[0.82, 0.92]

    // 人脸检测最小最大人脸
    public static int MinFaceSize = 150, MaxFaceSize = 1000;
    //跟踪人脸丢失的帧数
    public static int MaxLoseNumGone = 15;
    //上报人离开消息的跟踪人脸丢失的帧数
    public static int MaxLoseNumMiss = 5;
    //一屏检测的最多人脸数
    public static int MaxFaceNum = 10;
    //人脸质量阈值
    public static float FaceQualityOK = 0.70f;
    //人脸图片清晰度质量阈值
    public static float FaceMogOK = 0.20f;
    //人脸图片模糊度质量阈值
    public static float FaceBlurOK = 0.75f;
    //人脸图片角度质量阈值 0.50对应±30°
    public static float FacePoseOK = 0.50f;
    //人脸图片亮度阈值
    public static float FaceLightHighThreshold = 0.80f;
    public static float FaceLightLowThreshold = 0.10f;

    //人脸检测模式：最大人脸（非0），所有人脸（0）
    public static int IsCheckMaxFace = 1;

    //人脸检测的帧率（1000/FrameDetectPeriod）
    public static int FrameDetectPeriod = 40;//ms

    public static String ip = "10.100.207.229";
    public static String port = "50001";
    //人脸识别服务器的url
    public static String url_server = "http://"+ip+":"+ port + "/pad/recognize";
    //保存临时识别照片的url
    public static String url_img_server = "http://10.100.207.229:50001/pad/upload";

    public static String cwName = "";
    public static String cwPass = "";

    public static void updateLogin(String name, String pass){
        cwName = name;
        cwPass = pass;
    }

    public static NisBean mNisBean=new NisBean();
    public static void updateLicence(String Licence) {
            mNisBean.getCw_face().setLicence(Licence);
            sLicence = Licence;
            ReadFileUtils.save();
    }
}
