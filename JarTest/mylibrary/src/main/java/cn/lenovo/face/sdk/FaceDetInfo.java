package cn.lenovo.face.sdk;

import android.graphics.RectF;

public class FaceDetInfo {
	public int faceID = FaceConstants.FACE_ID_NULL;
	public int trackID = -1;
	public int missNum = 0;
	public int faceState = FaceConstants.FACE_STATE_GONE;
	public boolean isUsed = false;
	public boolean isPosted = false;

	public RectF rectf;
}
