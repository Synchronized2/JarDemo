package cn.face.sdk;

public class FaceCommon {
	private static boolean isLoaded = false;
	
	private static void loadLibrary(String libraryName) {
		System.loadLibrary(libraryName);
	}

	public static void loadLibrarys() {
		if (!isLoaded) {
			isLoaded = true;
			loadLibrary("CWFaceDetTrack");
			loadLibrary("CwRecog");
			loadLibrary("CWFaceSDK");
			loadLibrary("CWFaceSDKJni");
		}
	}
}
