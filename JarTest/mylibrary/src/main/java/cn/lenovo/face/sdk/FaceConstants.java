package cn.lenovo.face.sdk;

import cn.face.sdk.FaceInterface;

public interface FaceConstants {
	public static final int FACE_STATE_FOUND = 0;
	public static final int FACE_STATE_MISS = 1;
	public static final int FACE_STATE_GONE = 2;

	public static final int CW_DET_STATE_NULL = -1;
	public static final int CW_DET_STATE_NONE = 0;
	public static final int CW_DET_STATE_DET = 1;
	public static final int CW_DET_STATE_TRACK = 2;
//	public static final int CW_DET_STATE_ALIGN = 3;

	public static final int CW_DET_MODE_TRACK = FaceInterface.cw_op_t.CW_OP_DET | FaceInterface.cw_op_t.CW_OP_TRACK;
	public static final int CW_DET_MODE_DET = CW_DET_MODE_TRACK | FaceInterface.cw_op_t.CW_OP_QUALITY;
	public static final int CW_DET_MODE_ALIGN = CW_DET_MODE_DET | FaceInterface.cw_op_t.CW_OP_ALIGN;

	public static final int CW_DET_MODE_ALIGN_NOTRACK = FaceInterface.cw_op_t.CW_OP_DET| FaceInterface.cw_op_t.CW_OP_QUALITY
			| FaceInterface.cw_op_t.CW_OP_ALIGN;

	public static final int FACE_ID_NULL = -1;
	public static final int FACE_ID_UNREGISTER = -2;

	public static final int FACE_SCORE_QUALITY = 0;
	public static final int FACE_SCORE_BLUR = 1;
	public static final int FACE_SCORE_LIGHT = 2;
	public static final int FACE_SCORE_POSE_YAW = 6;
	public static final int FACE_SCORE_POSE_PITCH = 7;
	public static final int FACE_SCORE_MOG = 16;
	
	public static final int Err_Code_TimeOut = 1;
    public static final int Err_Code_Distance = 2;
	public static final int Err_Code_Pose = 3;
    public static final int Err_Code_Blur = 4;
    //人脸图片清晰度差
    public static final int CW_FACE_MOG_ERROR = -11009;
    //人脸图片头偏转太大
    public static final int CW_FACE_POSE_ERROR = -11010;
	//人脸图片亮度太低
	public static final int CW_FACE_LIGHT_ERROR = -11011;
}
