package cn.lenovo.face.http;


import cn.lenovo.face.bean.MsgJsonBean;

/**
 * Created by baohm1 on 2018/3/12.
 */

public interface FaceCallback {
    int FaceRecognitionCB(MsgJsonBean msgBean);
    int HttpTimeOutCB();
}
