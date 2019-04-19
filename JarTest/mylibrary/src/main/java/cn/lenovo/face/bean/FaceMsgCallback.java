package cn.lenovo.face.bean;

import java.util.Map;

import cn.lenovo.face.sdk.FaceDetInfo;


/**
* @Date：2018年2月5日 下午3:24:53  
* @author baohm1
*/

public interface FaceMsgCallback {
	void UpdateFaceMsg(Map<Integer, FaceDetInfo> map, int maxFaceTrackID);
}
