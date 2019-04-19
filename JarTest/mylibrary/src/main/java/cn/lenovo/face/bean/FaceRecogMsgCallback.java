package cn.lenovo.face.bean;

/**
 * Created by baohm1 on 2018/3/15.
 */

public interface FaceRecogMsgCallback {
    void FaceRecogListening(int faceID, boolean isFound, boolean isAllGone);
    void FaceRecogListenError(int errCode);
}
