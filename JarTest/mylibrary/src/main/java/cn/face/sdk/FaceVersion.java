package cn.face.sdk;



public class FaceVersion {

	static FaceVersion version = null;
	
	public FaceVersion() {
		FaceCommon.loadLibrarys();
	}

	public static FaceVersion getInstance() {

		if (null == version) {
			version = new FaceVersion();
		}
		return version;
	}


	static public native String cwGetFaceSDKVersion();
	
//	static public native int cwInstallLicence(String sAuthorizedSerial, int iInstallFlag, int iSaveFlag);

	static public native int cwGetMaxHandlesNum(String pLicence);
	
	static public native String cwGetDeviceInfo();
	
	static public native String cwGetLicence(String pCusName, String pCusCode);
	
	static public native String cwGetLicenceForCustom(String pCusName, String pCusCode, String pDeviceInfo);

}
