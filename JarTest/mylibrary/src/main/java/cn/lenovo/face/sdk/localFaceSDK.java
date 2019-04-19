package cn.lenovo.face.sdk;

import android.text.TextUtils;

import cn.face.sdk.FaceDetTrack;
import cn.face.sdk.FaceInterface;
import cn.face.sdk.FaceParam;
import cn.face.sdk.FaceVersion;
import cn.lenovo.face.Contans;


public class localFaceSDK {
    public static final int ERRCODE_MIN = FaceInterface.cw_errcode_t.CW_UNKNOWN_ERR;
    public FaceDetTrack mDet;
    public FaceVersion mVer;

    public int m_detHandle = ERRCODE_MIN;      // 检测句柄

//	public static localFaceSDK getInstance() {
//		if (null == mFaceSDK) {
//			mFaceSDK = new localFaceSDK();
//		}
//		return mFaceSDK;
//	}

    public Boolean InitSDK() {
        //System.out.println("InitSDK()---Enter...");
        mDet = FaceDetTrack.getInstance();
        mVer = FaceVersion.getInstance();

        String sVersion = mVer.cwGetFaceSDKVersion();
        System.out.println("SDK Verison: " + sVersion);
        int maxHandles = mVer.cwGetMaxHandlesNum(Contans.sLicence);
        System.out.println("SDK Max handles: " + maxHandles);

        // 创建检测句柄
        String sModelFolder = Contans.sLicencePath;   // CWModels文件夹路径
        String sDetModel = sModelFolder + "/_configs_dl_traditional.xml";

        m_detHandle = mDet.cwCreateDetHandle(sDetModel, Contans.sLicence);
        if (m_detHandle >= ERRCODE_MIN) {
            System.out.println("创建检测句柄失败，错误码：" + m_detHandle);
            if (!TextUtils.isEmpty(Contans.cwName) && !TextUtils.isEmpty(Contans.cwPass))
                Contans.updateLicence(mVer.cwGetLicence(Contans.cwName, Contans.cwPass));
            m_detHandle = mDet.cwCreateDetHandle(sDetModel, Contans.sLicence);
            if (m_detHandle >= ERRCODE_MIN) {
                return false;
            }
        }
        // 设置检测参数,检测的最大/最小人脸
        FaceParam mFaceParam = new FaceParam();
        mDet.cwGetFaceParam(m_detHandle, mFaceParam);
        mFaceParam.minSize = Contans.MinFaceSize;    // 最小人脸
        mFaceParam.maxSize = Contans.MaxFaceSize;   // 最大人脸
        mFaceParam.frameNumForLost = Contans.MaxLoseNumGone;
        mDet.cwSetFaceParam(m_detHandle, mFaceParam, sDetModel);

        FaceParam mFaceParam2 = new FaceParam();
        mDet.cwGetFaceParam(m_detHandle, mFaceParam2);
        System.out.println("======" + mFaceParam2.maxSize + "--" + mFaceParam2.minSize + "--" + mFaceParam2.frameNumForLost);

        // 设置检测参数,按人脸大小依次输出
        mDet.cwSetFaceBufOrder(m_detHandle, 1);
        return true;
    }

    public void CloseSDK() {
        // 程序退出时，销毁句柄
        if (m_detHandle < ERRCODE_MIN) {
            mDet.cwReleaseDetHandle(m_detHandle);
        }
    }
}
