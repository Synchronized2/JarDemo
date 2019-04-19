package cn.lenovo.face.camera;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import cn.lenovo.face.utils.Log;


final class CameraConfiguration {
	private static final String TAG = "CameraConfiguration";

	private static final int TEN_DESIRED_ZOOM = 27;
	private static final Pattern COMMA_PATTERN = Pattern.compile(",");
	private final Context mContext;

	public static Camera.Parameters parameters;

	public CameraConfiguration(Context context) {
		mContext = context;
	}

	/**
	 * 
	 * setCameraParameters:设置参数(用于实时预览数据回掉).

	 */
	public Size setCameraParameters(Camera camera, int caremaId, int previewW, int previewH, int format) {
		parameters = camera.getParameters();
		List<String> focusModes = parameters.getSupportedFocusModes();
		if (focusModes.contains("continuous-video")) {
			parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
		}
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
		List<int[]> range = parameters.getSupportedPreviewFpsRange();
		for (int j = 0; j < range.size(); j++) {
			int[] r = range.get(j);
			for (int k = 0; k < r.length; k++) {
//				Log.d(TAG, "" + r[k]);
			}
		}
		Size localSize = getOptimalPreviewSize(parameters.getSupportedPreviewSizes(), previewW, previewH);
		if (localSize != null) {
			parameters.setPreviewSize(localSize.width, localSize.height);
		} else {
			Log.e(TAG, "setCameraParameters: 设置预览失败");
		}
		parameters.setPreviewFormat(format);
		setZoom(parameters);
		camera.setDisplayOrientation(getDisplayOrientation(caremaId));
    	if(parameters.isAutoExposureLockSupported()) {
			parameters.setAutoExposureLock(false);
		}
//		int[] fpsRange = new int[2];
//		parameters.getPreviewFpsRange(fpsRange);
//		android.util.Log.d(TAG, "setCameraParameters: " + fpsRange[0] + ", " + fpsRange[1]);
		parameters.setPreviewFpsRange(10*1000,25*1000);
		camera.setParameters(parameters);
		return localSize;
	}

	/**
	 * 
	 * getOptimalPreviewSize:获取最接近预览分辨率
	 */
	private Size getOptimalPreviewSize(List<Size> localList, int w, int h) {
		Size optimalSize = null;
		try {
			ArrayList<Size> localArrayList = new ArrayList<Size>();
			Iterator<Size> localIterator = localList.iterator();
			while (localIterator.hasNext()) {
				Size localSize = localIterator.next();
				if (localSize.width > localSize.height) {
					localArrayList.add(localSize);
				}
				Log.d(TAG, "getOptimalPreviewSize: localSize = " + localSize.width + "*" + localSize.height);
			}
			Collections.sort(localArrayList, new PreviewComparator(w, h));
			optimalSize = localArrayList.get(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return optimalSize;
	}

	class PreviewComparator implements Comparator<Size> {
		int w, h;
		public PreviewComparator(int w, int h) {
			this.w = w;
			this.h = h;

		}

		@Override
		public int compare(Size paramSize1, Size paramSize2) {
			return Math.abs(paramSize1.width * paramSize1.height - this.w * this.h)
					- Math.abs(paramSize2.width * paramSize2.height - this.w * this.h);
		}

	}

	public int getDisplayOrientation(int caremaId) {
		Camera.CameraInfo info = new Camera.CameraInfo();
		Camera.getCameraInfo(caremaId, info);
		WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();

		int rotation = display.getRotation();
		int degrees = 0;
		switch (rotation) {
		case Surface.ROTATION_0:
			degrees = 0;
			break;
		case Surface.ROTATION_90:
			degrees = 90;
			break;
		case Surface.ROTATION_180:
			degrees = 180;
			break;
		case Surface.ROTATION_270:
			degrees = 270;
			break;
		}

		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			result = (360 - result) % 360;
		} else {
			result = (info.orientation - degrees + 360) % 360;
		}
		Log.d(TAG, "getDisplayOrientation: info.orientation = " + info.orientation);
		Log.d(TAG, "getDisplayOrientation: degrees = " + degrees);
		Log.d(TAG, "getDisplayOrientation: result = " + result);
		return result;
	}

	private static int findBestMotZoomValue(CharSequence stringValues, int tenDesiredZoom) {
		int tenBestValue = 0;
		for (String stringValue : COMMA_PATTERN.split(stringValues)) {
			stringValue = stringValue.trim();
			double value;
			try {
				value = Double.parseDouble(stringValue);
			} catch (NumberFormatException nfe) {
				return tenDesiredZoom;
			}
			int tenValue = (int) (10.0 * value);
			if (Math.abs(tenDesiredZoom - value) < Math.abs(tenDesiredZoom - tenBestValue)) {
				tenBestValue = tenValue;
			}
		}
		return tenBestValue;
	}

	private void setZoom(Camera.Parameters parameters) {
		String zoomSupportedString = parameters.get("zoom-supported");
		if (zoomSupportedString != null && !Boolean.parseBoolean(zoomSupportedString)) {
			return;
		}

		int tenDesiredZoom = TEN_DESIRED_ZOOM;

		String maxZoomString = parameters.get("max-zoom");
		if (maxZoomString != null) {
			try {
				int tenMaxZoom = (int) (10.0 * Double.parseDouble(maxZoomString));
				if (tenDesiredZoom > tenMaxZoom) {
					tenDesiredZoom = tenMaxZoom;
				}
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			}
		}

		String takingPictureZoomMaxString = parameters.get("taking-picture-zoom-max");
		if (takingPictureZoomMaxString != null) {
			try {
				int tenMaxZoom = Integer.parseInt(takingPictureZoomMaxString);
				if (tenDesiredZoom > tenMaxZoom) {
					tenDesiredZoom = tenMaxZoom;
				}
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			}
		}

		String motZoomValuesString = parameters.get("mot-zoom-values");
		if (motZoomValuesString != null) {
			tenDesiredZoom = findBestMotZoomValue(motZoomValuesString, tenDesiredZoom);
		}

		String motZoomStepString = parameters.get("mot-zoom-step");
		if (motZoomStepString != null) {
			try {
				double motZoomStep = Double.parseDouble(motZoomStepString.trim());
				int tenZoomStep = (int) (10.0 * motZoomStep);
				if (tenZoomStep > 1) {
					tenDesiredZoom -= tenDesiredZoom % tenZoomStep;
				}
			} catch (NumberFormatException nfe) {
				// continue
			}
		}
		if (maxZoomString != null || motZoomValuesString != null) {
			parameters.set("zoom", String.valueOf(tenDesiredZoom / 10.0));
		}
		if (takingPictureZoomMaxString != null) {
			parameters.set("taking-picture-zoom", tenDesiredZoom);
		}
	}
}