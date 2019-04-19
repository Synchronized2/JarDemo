/**
 * Created by baohm1 on 2018/3/5.
 */

package cn.lenovo.face.realtime;

import android.content.Context;
import android.graphics.ImageFormat;
import android.os.Handler;
import android.os.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cn.face.sdk.FaceInfo;
import cn.face.sdk.FaceInterface;
import cn.lenovo.face.Contans;
import cn.lenovo.face.bean.FaceMsgCallback;
import cn.lenovo.face.bean.FaceRecogMsgCallback;
import cn.lenovo.face.bean.MsgJsonBean;
import cn.lenovo.face.http.FaceCallback;
import cn.lenovo.face.http.HttpFaceAnalyse;
import cn.lenovo.face.jsonfile.ReadFileUtils;
import cn.lenovo.face.sdk.FaceConstants;
import cn.lenovo.face.sdk.FaceDetInfo;
import cn.lenovo.face.sdk.localFaceSDK;
import cn.lenovo.face.utils.FileUtils;
import cn.lenovo.face.utils.Log;
import cn.lenovo.face.utils.Utils;

/**
 * @author baohm1
 * @email baohm1@lenovo.com
 */
public class FrameProcess implements FaceConstants, FaceCallback {
    private static final String TAG = "FrameProcess";
    private static final int MSG_UPDATE_FACE_DET_INFO_STATE = 1;
    private static FaceMsgCallback msgCallback = null;
    private static FaceRecogMsgCallback msgFaceRecogCallback = null;
    private static Map<Integer, FaceDetInfo> mFaceMap = new HashMap<Integer, FaceDetInfo>();

    private Context mContext = null;
    private localFaceSDK mFaceSDK = null;

    private int mFaceState = FaceConstants.CW_DET_STATE_DET;
    private int mFaceDetMode = FaceConstants.CW_DET_MODE_DET;

    private FaceInfo[] mFaceBuffers = new FaceInfo[Contans.MaxFaceNum];

    private ArrayList<Integer> mRemoveList = new ArrayList<>();
    private int mMaxFaceSizeTrackID = -1;
    private volatile int mLastPostTrackID = -1;
    private volatile int mLastPostFaceID = -1;

    private boolean hasFace = true;
    private int missFaceNumForLog = 0;

    private int mCWFaceFormat = 0;

    private int mWidth, mHeight, mAngle, mMirror;

    public FrameProcess(Context context, int w, int h, int format, int angle, int mirror) {
        mContext = context;
        mFaceSDK = new localFaceSDK();
        boolean ret = mFaceSDK.InitSDK();
        if (!ret) {
            Log.e(TAG, "FrameProcess: Init SDK failed ...");
        }

        for (int i = 0; i < Contans.MaxFaceNum; i++) {
            mFaceBuffers[i] = new FaceInfo();
        }

        mWidth = w;
        mHeight = h;
        setCwFaceFormat(format);
        mAngle = angle;
        mMirror = mirror;
        Log.d(TAG, "Load FrameProcess...");
        Log.d(TAG, "mAngle = " + mAngle + ", mMirror = " + mMirror);
    }

    public static void registerCallBack(FaceMsgCallback faceMsgCallback) {
        msgCallback = faceMsgCallback;
    }

    public static void registerFaceRecogCallBack(FaceRecogMsgCallback faceCallback) {
        msgFaceRecogCallback = faceCallback;
    }

    public static int getFaceIDState(int faceID) {
        for (FaceDetInfo faceDetInfo : mFaceMap.values()) {
            if (faceDetInfo.faceID == faceID) {
                return faceDetInfo.faceState;
            }
        }
        return FaceConstants.FACE_STATE_GONE;
    }

    public int clearCache() {
        mFaceMap.clear();
        updateUIFaceTrack();
        switchState(FaceConstants.CW_DET_STATE_DET);
        return 0;
    }

    public int onPreviewFrame(byte[] imgData) {
        return onPreviewFrame(imgData, mWidth, mHeight, mCWFaceFormat, mAngle, mMirror);
    }

    public void setCameraOrientation(int angle, int mirror) {
        mAngle = angle;
        mMirror = mirror;
    }

    /**
     * 帧处理：人脸检测
     * param byte[]
     * return
     */
    private synchronized int onPreviewFrame(byte[] imgData, int width, int height, int format, int angle, int mirror) {
        int ret = 0;

        int faceNum = mFaceSDK.mDet.cwFaceDetection(mFaceSDK.m_detHandle,
                imgData, width, height,
                format, angle, mirror,
                mFaceDetMode, mFaceBuffers);
        if (faceNum >= localFaceSDK.ERRCODE_MIN) {
            Log.d(TAG, "检测失败，错误码：" + faceNum);
        } else if (faceNum < 1) {
            if (hasFace) {
                Log.d(TAG, "onPreviewFrame: 未检测到人脸");
                hasFace = false;
            } else {
                missFaceNumForLog++;
                if (missFaceNumForLog > 60) {
                    missFaceNumForLog = 0;
                    Log.d(TAG, ".");                 // 未检测到人脸
                    updateUIFaceTrack();
                }
            }
        } else {
            missFaceNumForLog = 0;
            hasFace = true;
//            Log.d(TAG, "Face Num: " + faceNum);
            int checkFaceNum = 0;
            if (Contans.IsCheckMaxFace == 0) {
                checkFaceNum = faceNum;
            } else {
                checkFaceNum = 1;
            }
            //对人脸图像进行处理,默认从最大开始处理
            for (int i = 0; i < checkFaceNum; i++) {
//				System.out.println("trackID-------------->: " + mFaceBuffers[i].trackId);
                if (mFaceBuffers[i].trackId == -1) {
                    ret = 1;
                    continue;
                }
                FaceInfo faceInfo = mFaceBuffers[i];

                //face是否在ROI有效区域内
                if (Contans.RoiRect != null) {
                    if (!Contans.RoiRect.contains(faceInfo.x, faceInfo.y, faceInfo.x + faceInfo.width, faceInfo.y + faceInfo.height)) {
                        ret = 1;
                        continue;
                    }
                }

                //remark trackID for max face size
                if (i == 0) {
                    mMaxFaceSizeTrackID = faceInfo.trackId;
                }

                //是否已经记录此TrackID, 不存在，则记录
                if (faceInfo.trackId >= 0 && !mFaceMap.containsKey(faceInfo.trackId)) {
                    FaceDetInfo faceDetInfo = new FaceDetInfo();
                    faceDetInfo.trackID = faceInfo.trackId;
                    mFaceMap.put(faceInfo.trackId, faceDetInfo);
                    Log.d(TAG, "============================================>Add trackID = " + faceDetInfo.trackID);
                }

                if (Contans.ENABLE_FACE_VIEW) {
                    FaceDetInfo faceDetInfo2 = mFaceMap.get(faceInfo.trackId);
                    if (faceDetInfo2 != null) {
                        boolean isNeedUpdate = Utils.UpdateRectIfNeed(faceDetInfo2, faceInfo.x, faceInfo.y,
                                faceInfo.x + faceInfo.width, faceInfo.y + faceInfo.height);
                        if (isNeedUpdate) {
//                            android.util.Log.d(TAG, "onPreviewFrame: updateUIFaceTrack()");
                            updateUIFaceTrack();
                        }
                    }
                }

                //检测人脸姿态、清晰度、亮度和综合质量
                if (faceInfo.errcode == FaceInterface.cw_quality_errcode_t.CW_QUALITY_OK
//                        && faceInfo.scores[FaceConstants.FACE_SCORE_MOG] >= Contans.FaceMogOK
                        && faceInfo.scores[FaceConstants.FACE_SCORE_QUALITY] >= Contans.FaceQualityOK
                        && faceInfo.scores[FaceConstants.FACE_SCORE_BLUR] >= Contans.FaceBlurOK
                        && faceInfo.scores[FaceConstants.FACE_SCORE_LIGHT] >= Contans.FaceLightLowThreshold
                        && faceInfo.scores[FaceConstants.FACE_SCORE_LIGHT] <= Contans.FaceLightHighThreshold
                        && faceInfo.scores[FaceConstants.FACE_SCORE_POSE_YAW] >= Contans.FacePoseOK
                        && faceInfo.scores[FaceConstants.FACE_SCORE_POSE_PITCH] >= Contans.FacePoseOK) {
                    /*Log.d(TAG, "mog = " + faceInfo.scores[FACE_SCORE_MOG]
                            + ", total quality = " + faceInfo.scores[FaceConstants.FACE_SCORE_QUALITY]
                            + ", blur = " + faceInfo.scores[FACE_SCORE_BLUR]
                            + ", light = " + faceInfo.scores[FaceConstants.FACE_SCORE_LIGHT]
                            + ", pose yaw = " + faceInfo.scores[FACE_SCORE_POSE_YAW]
                            + ", pose pitch = " + faceInfo.scores[FACE_SCORE_POSE_PITCH]
                            + ", pose yaw_du = " + faceInfo.scores[3]
                            + ", pose pitch_du = " + faceInfo.scores[4]);*/
                    int faceSize = faceInfo.width;
                    if (faceSize < faceInfo.height) {
                        faceSize = faceInfo.height;
                    }
                    if (mFaceMap.containsKey(faceInfo.trackId)) {
//                        Log.d(TAG, "onPreviewFrame: Handle trackID = " + mFaceBuffers[i].trackId);
                        FaceDetInfo faceDetInfo = mFaceMap.get(faceInfo.trackId);
                        if (!faceDetInfo.isUsed && faceSize >= Contans.MinFaceSize) {
//                            int ret_face = faceRecog_align(imgData, width, height, format, angle, mirror, faceDetInfo.trackID);
                            int ret_face = faceRecog_Img(imgData, faceInfo, width, height, angle, mirror);
                            if (ret_face == 0) {
                                //暂时设为已经识别，防止重复发送图片给服务器，等1.5s后再确认是否真正识别。
                                faceDetInfo.isUsed = true;

                                Message msg = Message.obtain();
                                msg.arg1 = faceDetInfo.trackID;
                                msg.what = MSG_UPDATE_FACE_DET_INFO_STATE;
                                mHandler.sendMessageDelayed(msg, 1000);
                            }
                            //TODO模拟识别
//                            if (msgFaceRecogCallback != null) {
//                                msgFaceRecogCallback.FaceRecogListening(300, true, false);
//                                mFaceMap.get(mFaceBuffers[i].trackId).faceID = 300;
//                            }
                        }
                    } else {
                        Log.d(TAG, "ERROR: mFaceMap = " + mFaceMap.toString());
                        Log.d(TAG, "ERROR: trackId = " + faceInfo.trackId);
                    }
//                    Log.d(TAG, "FACE_QUALITY: " + mFaceBuffers[i].scores[0]);
                } else {
                    //跟踪模式下，当前最大人脸不是上次上报的trackID，且此trackID有faceID，则上报识别到消息
                    if ((Contans.IsCheckMaxFace != 0)
                            && (mLastPostTrackID != mMaxFaceSizeTrackID)
                            && (mFaceMap.get(mMaxFaceSizeTrackID).faceID > 0)) {
                        postFaceRecogListening(mMaxFaceSizeTrackID, mFaceMap.get(mMaxFaceSizeTrackID).faceID, true, false);
                    }

                    if (mFaceState == FaceConstants.CW_DET_STATE_DET) {
//						System.out.println("mFaceBuffers[i].errcode = " + mFaceBuffers[i].errcode);
//                        if (faceInfo.errcode== FaceInterface.cw_quality_errcode_t.CW_QUALITY_OK) {
//                            Log.d(TAG, "mog = " + faceInfo.scores[FACE_SCORE_MOG]
//                                    + ", total quality = " + faceInfo.scores[FaceConstants.FACE_SCORE_QUALITY]
//                                    + ", blur = " + faceInfo.scores[FACE_SCORE_BLUR]
//                                    + ", light = " + faceInfo.scores[FaceConstants.FACE_SCORE_LIGHT]
//                                    + ", pose yaw = " + faceInfo.scores[FACE_SCORE_POSE_YAW]
//                                    + ", pose pitch = " + faceInfo.scores[FACE_SCORE_POSE_PITCH]
//                                    + ", pose yaw_du = " + faceInfo.scores[3]
//                                    + ", pose pitch_du = " + faceInfo.scores[4]);
//                        }
                    }
                }
            }
        }
        //检查是否需要切换状态
        checkState();
        //检查是否有丢失的track；丢失超过一定帧，则移除track并发送gone消息
        checkMissFrame(faceNum, mFaceBuffers);

//        updateUIFaceTrack();
        return ret;
    }

    /**
     * 检查是否有丢失的track；丢失超过一定帧，则移除track并发送gone消息
     *
     * @param faceNum
     * @param faceBufs
     */
    private void checkMissFrame(int faceNum, FaceInfo[] faceBufs) {
        if (mFaceMap.isEmpty()) {
            return;
        }

        //Add Miss Number
        if (faceNum >= localFaceSDK.ERRCODE_MIN || faceNum < 1) {
            for (FaceDetInfo faceDetInfo : mFaceMap.values()) {
                faceDetInfo.missNum++;
                if (faceDetInfo.missNum >= Contans.MaxLoseNumGone) {
                    mRemoveList.add(faceDetInfo.trackID);
                    faceDetInfo.faceState = FaceConstants.FACE_STATE_GONE;
                } else if (faceDetInfo.missNum >= Contans.MaxLoseNumMiss) {
                    faceDetInfo.faceState = FaceConstants.FACE_STATE_MISS;
                }
            }
        } else {
            for (FaceDetInfo faceDetInfo : mFaceMap.values()) {
                boolean hasMiss = true;
                for (int i = 0; i < faceNum; i++) {
                    if (faceBufs[i].trackId == faceDetInfo.trackID) {
                        hasMiss = false;
                        faceDetInfo.missNum = 0;
                        faceDetInfo.faceState = FaceConstants.FACE_STATE_FOUND;
                        break;
                    }
                }
                if (hasMiss) {
                    faceDetInfo.missNum++;
                    if (faceDetInfo.missNum >= Contans.MaxLoseNumGone) {
                        mRemoveList.add(faceDetInfo.trackID);
                        faceDetInfo.faceState = FaceConstants.FACE_STATE_GONE;
                    } else if (faceDetInfo.missNum >= Contans.MaxLoseNumMiss) {
                        faceDetInfo.faceState = FaceConstants.FACE_STATE_MISS;
                    }
                }
            }
        }

        /*
         * 确认是否有离开的人脸，如果有，则从mFaceMap移除并发送gone消息
         * */
        if (mRemoveList.isEmpty()) {
            return;
        }
        int numRecog = 0;
        for (Integer trackID : mRemoveList) {
            Log.d(TAG, "trackID(" + trackID + ")-faceID(" + mFaceMap.get(trackID).faceID + ")-has gone");

            FaceDetInfo removeFaceDetInfo = mFaceMap.get(trackID);
            mFaceMap.remove(trackID);

            //TODO, post GONE message
            if (msgFaceRecogCallback != null) {
                numRecog = 0;
                for (FaceDetInfo faceDetInfo : mFaceMap.values()) {
                    if (faceDetInfo.faceID > 0)
                        numRecog++;
                }
                if (numRecog >= 1) {
                    postFaceRecogListening(trackID, removeFaceDetInfo.faceID, false, false);
                } else {
                    postFaceRecogListening(trackID, removeFaceDetInfo.faceID, false, true);
                }
            }
            System.out.println("====================0========================>Remove trackID = " + trackID);
        }
        mRemoveList.clear();
        updateUIFaceTrack();
    }

    /**
     * 检查是否需要切换状态
     */
    private void checkState() {
        boolean needDet = false;
        if (mFaceMap.isEmpty()) {
            if (mFaceState != FaceConstants.CW_DET_STATE_DET) {
                switchState(FaceConstants.CW_DET_STATE_DET);
            }
            return;
        }

        for (FaceDetInfo faceDetInfo : mFaceMap.values()) {
            if (!faceDetInfo.isUsed) {
                needDet = true;
            }
        }
        if (needDet && mFaceState != FaceConstants.CW_DET_STATE_DET) {
            switchState(FaceConstants.CW_DET_STATE_DET);
        } else if (!needDet && mFaceState != FaceConstants.CW_DET_STATE_TRACK) {
            switchState(FaceConstants.CW_DET_STATE_TRACK);
        }
    }

    /**
     * 切换状态,并切换人脸检测模式
     *
     * @param state
     */
    private void switchState(int state) {
        if (mFaceState == state)
            return;
        Log.d(TAG, "Switch to state(1-det;2-track):" + state);

        mFaceState = state;
        switch (state) {
            case FaceConstants.CW_DET_STATE_DET:
                mFaceDetMode = FaceConstants.CW_DET_MODE_DET;
                break;
            case FaceConstants.CW_DET_STATE_TRACK:
                mFaceDetMode = FaceConstants.CW_DET_MODE_TRACK;
                break;
        }
    }

    /**
     * 删除mFaceMap里具有相同faceID而trackID不同的那个values
     *
     * @param trackID return true:delete same trackID; false: no same trackID
     */
    private boolean deleteSameTrackID(int trackID) {
        int removeTrack = -1;
        int usedFaceID = mFaceMap.get(trackID).faceID;
        for (FaceDetInfo faceDetInfo : mFaceMap.values()) {
            if (faceDetInfo.faceID == usedFaceID && faceDetInfo.trackID != trackID) {
                removeTrack = faceDetInfo.trackID;
                //同一faceID的trackID发生变化时，重新上报识别记录，是为了符合动画要求
                postFaceRecogListening(trackID, faceDetInfo.faceID, true, false);
            }
        }
        if (removeTrack != -1) {
            mFaceMap.remove(removeTrack);
            mLastPostTrackID = trackID;
            System.out.println("====================1========================>Remove trackID = " + removeTrack);
            updateUIFaceTrack();
            return true;
        }
        return false;
    }

    /**
     * 人脸识别,返回faceID
     *
     * @param faceInfo
     */
    private int faceRecog(FaceInfo faceInfo, int trackID) {
        Log.d(TAG, "faceRecog: ===========================>");

        //TODO, face Recog message will be in FaceRecognitionCB()
        return HttpFaceAnalyse.FaceRecog(faceInfo, this, trackID);
    }

    /**
     * 获取对齐头像并人脸识别
     * param imgData
     * param trackID
     */
    private synchronized int faceRecog_align(byte[] imgData, int width, int height, int format, int angle, int mirror, int trackID) {
        Log.d(TAG, "faceRecog_align trackID = " + trackID);
        int ret = -1;
        FaceInfo[] faceBufs = new FaceInfo[Contans.MaxFaceNum];
        for (int i = 0; i < Contans.MaxFaceNum; i++) {
            faceBufs[i] = new FaceInfo();
        }

        int faceNum = mFaceSDK.mDet.cwFaceDetection(mFaceSDK.m_detHandle,
                imgData, width, height,
                format, angle, mirror,
                FaceConstants.CW_DET_MODE_ALIGN, faceBufs);
        if (faceNum >= localFaceSDK.ERRCODE_MIN) {
//            System.out.println("检测失败，错误码：" + faceNum);   // 检测异常
        } else if (faceNum < 1) {
//			System.out.println("未检测到人脸");                 // 未检测到人脸
        } else {
            // 输出人脸位置
            for (int i = 0; i < faceNum; i++) {
                //是否已经记录此TrackID,不存在，则记录
                if (faceBufs[i].trackId == trackID) {
                    FaceInfo faceInfo = faceBufs[i];
                    if (faceInfo.alignedW > 0 && faceInfo.alignedH > 0) {
                        //faceRecog
                        ret = faceRecog(faceInfo, trackID);
                        //save IMG
                        String file = FileUtils.saveCropJpg(imgData, width, height, faceInfo.x, faceInfo.y, faceInfo.width, faceInfo.height, angle, mirror);
                        if (file != null) {
                            HttpFaceAnalyse.uploadJpg(file);
                        }
                    }
                    break;
                }
            }
        }
        return ret;
    }

    /**
     * 获取对齐头像并人脸识别
     * param imgData
     * param trackID
     */
    private int faceRecog_Img(byte[] imgData, FaceInfo faceInfo, int width, int height, int angle, int mirror) {
        int trackID = faceInfo.trackId;
        Log.d(TAG, "faceRecog_Img trackID = " + trackID);
        int ret = -1;
        //save IMG
        String file = FileUtils.saveCropJpg(imgData, width, height, faceInfo.x, faceInfo.y, faceInfo.width, faceInfo.height, angle, mirror);
        if (file != null) {
            ret = HttpFaceAnalyse.FaceRecogImg(this, file, trackID);
        }

        return ret;
    }

    private void updateUIFaceTrack() {
        if (msgCallback != null && Contans.ENABLE_FACE_VIEW) {
            msgCallback.UpdateFaceMsg(mFaceMap, mMaxFaceSizeTrackID);
            return;
        }
    }

    private int setCwFaceFormat(int preview_format) {
        switch (preview_format) {
            case ImageFormat.YV12:
                mCWFaceFormat = FaceInterface.cw_img_form_t.CW_IMAGE_NV12;
                break;
            case ImageFormat.NV21:
                mCWFaceFormat = FaceInterface.cw_img_form_t.CW_IMAGE_NV21;
                break;
        }
        return mCWFaceFormat;
    }

    @Override
    public int FaceRecognitionCB(MsgJsonBean msgBean) {
        if (msgBean.getData() == null || msgBean.getData().getUser() == null) {
            Log.d(TAG, "recordonResponse = " + msgBean.getStatus() + ", error msg = " + msgBean.getErrMsg());
            return 0;
        }

        int trackID = msgBean.getData().getUser().getTrackID();
        int faceID = msgBean.getData().getUser().getFaceID();
        double similar = msgBean.getData().getUser().getSimilar();
        if (!mFaceMap.containsKey(trackID)) {
            return 0;
        }

        //return error, unregister
        if (msgBean.getStatus() != 0) {
            Log.d(TAG, "recordonResponse = " + msgBean.getStatus() + ", error msg = " + msgBean.getErrMsg());

            mFaceMap.get(trackID).faceID = faceID;
            //上报未注册faceID
            postFaceRecogListening(trackID, faceID, true, false);

            if (msgBean.getStatus() == FaceConstants.CW_FACE_POSE_ERROR) {
                postListenError(FaceConstants.Err_Code_Pose);
            }
            if (msgBean.getStatus() == FaceConstants.CW_FACE_MOG_ERROR) {
                postListenError(FaceConstants.Err_Code_Blur);
            }
            return 0;
        }

        //recog as register in server, however, smaller than Contans.FaceSimilarThreshold in pad
        if (similar >= Contans.FaceSimilarThreshold) {
            Log.d(TAG, "自-faceID = " + faceID + ", similar = " + msgBean.getData().getUser().getSimilar() + ">>>高于阈值不通过");
            return 0;
        }


        mFaceMap.get(trackID).faceID = faceID;
        deleteSameTrackID(trackID);
        if (faceID != -1) {
            //TODO post msg
            postFaceRecogListening(trackID, faceID, true, false);
            Log.d(TAG, "faceID = " + faceID + ", similar = " + similar);
            updateUIFaceTrack();
            return faceID;
        }
        return 0;
    }

    private void postFaceRecogListening(int trackID, int faceID, boolean isFound, boolean isAllGone) {
        if (msgFaceRecogCallback == null || faceID == FaceConstants.FACE_ID_NULL) {
            return;
        }
        //检测到人脸
        if (isFound) {
            if (mLastPostTrackID == trackID && mLastPostFaceID == faceID && faceID != FaceConstants.FACE_ID_UNREGISTER)
                return;
            mLastPostTrackID = trackID;
            mLastPostFaceID = faceID;
            msgFaceRecogCallback.FaceRecogListening(faceID, isFound, isAllGone);
        } else {
            //画面中没有人
            if (isAllGone) {
                mLastPostTrackID = -1;
                msgFaceRecogCallback.FaceRecogListening(faceID, isFound, isAllGone);
            } else {
                if (Contans.IsCheckMaxFace == 0) {
                    mLastPostTrackID = -1;
                    msgFaceRecogCallback.FaceRecogListening(faceID, isFound, isAllGone);
                }
            }
        }
    }

    @Override
    public int HttpTimeOutCB() {
        if (msgFaceRecogCallback != null) {
            msgFaceRecogCallback.FaceRecogListenError(FaceConstants.Err_Code_TimeOut);
        }
        return 0;
    }

    private long lastPostErrTime = 0;

    private void postListenError(int errCode) {
        long curTime = System.currentTimeMillis();
        //每个1000ms上报一次错误
        if (msgFaceRecogCallback != null && (curTime - lastPostErrTime) > 1000) {
            msgFaceRecogCallback.FaceRecogListenError(errCode);
            lastPostErrTime = System.currentTimeMillis();
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int id = msg.what;
            switch (id) {
                case MSG_UPDATE_FACE_DET_INFO_STATE:
                    int trackID = msg.arg1;
                    FaceDetInfo faceDetInfo = mFaceMap.get(trackID);
                    if (faceDetInfo != null) {
                        if (faceDetInfo.faceID >= 0) {
                            faceDetInfo.isUsed = true;
                        } else {
                            faceDetInfo.isUsed = false;
                        }
                    }
                    break;
            }
        }
    };
}
