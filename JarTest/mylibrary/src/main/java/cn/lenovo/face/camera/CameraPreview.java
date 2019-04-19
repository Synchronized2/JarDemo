package cn.lenovo.face.camera;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.RelativeLayout;

import cn.face.sdk.FaceInterface;
import cn.lenovo.face.Contans;
import cn.lenovo.face.realtime.FrameManagerThread;
import cn.lenovo.face.utils.CameraUtils;
import cn.lenovo.face.utils.Log;

/**
 * 实时预览帧 setPreviewCallback
 * 
 * @author yusr
 *
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, PreviewCallback {
	private static final String TAG = "CameraPreview";

	private FrameManagerThread mFrameManagerThread = FrameManagerThread.getInstance();

	private Camera mCamera;
	private CameraConfiguration mCameraConfiguration;
	private static Context context = null;

//	Delegate mDelegate;
	private int mOrientation;
	private int mCameraType;
	private int mCameraID = Contans.DEFAULT_CAMERA;
	private boolean mPreviewing = true;
	private int reqPrevW = Contans.PREVIEW_W;
	private int reqPrevH = Contans.PREVIEW_H;
//	private List<Size> mPreviewSizeList;
	private Size mPreviewSize = null;
	private int mAngle, mMirror;

	private boolean isFaceRecoging = false;

	private CameraPreview.LayoutMode mLayoutMode = Contans.DEFAULT_LAYOUT_MODE;
	public static enum LayoutMode {
		FitToParent, // Scale to the size that no side is larger than the parent
		NoBlank // Scale to the size that no side is smaller than the parent
	};

	public CameraPreview(Context context) {
		super(context);
		this.context = context;
	}

	public CameraPreview(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
	}

	public CameraPreview(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
	}

	@Override
	public void surfaceCreated(SurfaceHolder surfaceHolder) {
		Log.d(TAG, "surfaceCreated: ");
	}

	@Override
	public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
		Log.d(TAG, "surfaceChanged: ");
		if (surfaceHolder.getSurface() == null) {
			return;
		}
		mOrientation = context.getResources().getConfiguration().orientation;

		stopCameraPreview();
		showCameraPreview();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
		stopCameraPreview();
	}

	public void showCameraPreview() {
		if (mCamera != null) {
			try {
				mCamera.setPreviewDisplay(getHolder());
				mCamera.startPreview();
				mCamera.cancelAutoFocus();
                if (mCamera.getParameters().getMaxNumDetectedFaces() > 0)
                    mCamera.startFaceDetection();
				Log.d(TAG, "Start camera preview");
				mCamera.setPreviewCallback(CameraPreview.this);
				mPreviewing = true;
				isFaceRecoging = true;

				mAngle = CameraUtils.getCWFaceOrientation(context, mCameraID);
				if (mCameraID == Camera.CameraInfo.CAMERA_FACING_FRONT) {
					mMirror = FaceInterface.cw_img_mirror_t.CW_IMAGE_MIRROR_HOR;
				} else {
					mMirror = FaceInterface.cw_img_mirror_t.CW_IMAGE_MIRROR_NONE;
//					mMirror = FaceInterface.cw_img_mirror_t.CW_IMAGE_MIRROR_HOR;
				}
				mFrameManagerThread.setCameraOrientation(mAngle, mMirror);
			} catch (Exception e) {
//				LogUtils.LOGE(TAG, e.toString());
			}
		}
	}

	public void stopCameraPreview() {
		if (mCamera != null) {
			try {
				mPreviewing = false;
				isFaceRecoging = false;
				mCamera.cancelAutoFocus();
				mCamera.stopFaceDetection();
                mCamera.setPreviewCallback(null);
				mCamera.stopPreview();
			} catch (Exception e) {
//				LogUtils.LOGE(TAG, e.toString());
			}
		}
	}

	/******************************************************************/
	public Size getPreviewSize() {
		if (mCamera == null) {
			return mPreviewSize;
		}
		Camera.Parameters parameters = mCamera.getParameters();
		return parameters.getPreviewSize();
	}

//	public void startFaceRecoging() {
//		isFaceRecoging = true;
//	}
//
//	public void stopFaceRecoging() {
//		isFaceRecoging = false;
//	}

	/**
	 * 打开摄像头开始预览，但是并未开始识别
	 */
	public boolean xfStartCamera() {
		if (mCamera != null) {
			return false;
		}

		try {
			mCamera = Camera.open(mCameraID);
			Log.d(TAG, "Open camera OK ...");
		} catch (Exception e) {
			Log.d(TAG, "Fail to open camera");
			return false;
		}
		setupCamera();
		return true;
	}

	/**
	 * 打开摄像头开始预览，但是并未开始识别
	 */
	public void xfStartCamera(int cameraID) {
		mCameraID = cameraID;
		xfStartCamera();
	}

	/**
	 * 关闭摄像头预览，并且隐藏扫描框
	 */
	public void xfStopCamera() {
		if (mCamera != null) {
			stopCameraPreview();

			mCamera.release();
			mCamera = null;
			mFrameManagerThread.stop();
		}
	}

	public boolean adjustSurfaceLayoutSize(int availableWidth, int availableHeight) {
		return adjustSurfaceLayoutSize(availableWidth, availableHeight, mLayoutMode);
	}

	public boolean adjustSurfaceLayoutSize(int availableWidth, int availableHeight, LayoutMode layoutMode) {
		if (mPreviewSize == null) {
			Log.d(TAG, "adjustSurfaceLayoutSize: mPreviewSize is null");
			return false;
		}
		mLayoutMode = layoutMode;

		boolean portrait = isPortrait();
		float tmpLayoutHeight, tmpLayoutWidth;
		if (portrait) {
			tmpLayoutHeight = mPreviewSize.width;
			tmpLayoutWidth = mPreviewSize.height;
		} else {
			tmpLayoutHeight = mPreviewSize.height;
			tmpLayoutWidth = mPreviewSize.width;
		}

		float factH, factW, fact;
		factH = availableHeight / tmpLayoutHeight;
		factW = availableWidth / tmpLayoutWidth;
		if (mLayoutMode == LayoutMode.FitToParent) {
			// Select smaller factor, because the surface cannot be set to the size larger than display metrics.
			if (factH < factW) {
				fact = factH;
			} else {
				fact = factW;
			}
		} else {
			if (factH < factW) {
				fact = factW;
			} else {
				fact = factH;
			}
		}

		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)this.getLayoutParams();

		int layoutHeight = (int) (tmpLayoutHeight * fact);
		int layoutWidth = (int) (tmpLayoutWidth * fact);
//		if (true) {
//			Log.v(TAG, "current Layout Size - w: " + getWidth() + ", h: " + getHeight());
//			Log.v(TAG, "Preview Layout Size - w: " + layoutWidth + ", h: " + layoutHeight);
//			Log.v(TAG, "Scale factor: " + fact);
//		}

		boolean layoutChanged;
		if ((layoutWidth != this.getWidth()) || (layoutHeight != this.getHeight())) {
			layoutParams.height = layoutHeight;
			layoutParams.width = layoutWidth;
			this.setLayoutParams(layoutParams); // this will trigger another surfaceChanged invocation.
			layoutChanged = true;
		} else {
			layoutChanged = false;
		}

		return layoutChanged;
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
//		Log.d(TAG, "onPreviewFrame: =====================");
//		int cameraType;
//		if (mCameraID == Camera.CameraInfo.CAMERA_FACING_FRONT) {// 前置
//			if (Configuration.ORIENTATION_PORTRAIT == mOrientation) {// 竖屏
//				cameraType = Contans.FRONT_PORTRAIT;
//			} else {// 横屏 水平镜像
//				cameraType = Contans.FRONT_LANDSCAPE;
//			}
//		} else {// 后置
//			if (Configuration.ORIENTATION_PORTRAIT == mOrientation) {// 竖屏 旋转90
//				cameraType = Contans.BACK_PORTRAIT;
//			} else {
//				// 横屏不做处理
//				cameraType = Contans.BACK_LANDSCAPE;
//			}
//		}
		if (isFaceRecoging) {
			mFrameManagerThread.pushFrame(data);
		}
//		LocalFaceSDK.getInstance(context).xfPushFrame(data, mPreviewSize.width, mPreviewSize.height,
//				Contants.PREVIEW_DATA_FORMAT, cameraType);
	}

	/**
	 * 切换摄像头
	 */
	public int switchCamera() {
		xfStopCamera();
		if (mCameraID == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			mCameraID = Camera.CameraInfo.CAMERA_FACING_BACK;
		} else {
			mCameraID = Camera.CameraInfo.CAMERA_FACING_FRONT;
		}
		xfStartCamera();
		return mCameraID;
	}

	/**
	 * setReqPrevWH:设置希望的预览分辨率.
	 */
	public void setReqPrevWH(int reqPrevW,int reqPrevH) {
		this.reqPrevW = reqPrevW;
		this.reqPrevH = reqPrevH;
	}

	private void setupCamera() {
		if (mCamera != null) {
//			setCwFaceFormat(Contans.PREVIEW_DATA_FORMAT);
			mCameraConfiguration = new CameraConfiguration(context);
			mPreviewSize = mCameraConfiguration.setCameraParameters(mCamera, mCameraID, reqPrevW,
					reqPrevH, Contans.PREVIEW_DATA_FORMAT);

			//TODO
			Point point = CameraUtils.getAdjustSize(context, mCamera);
			adjustSurfaceLayoutSize(point.x, point.y);
			Log.d(TAG, "setupCamera: mPreviewSize = " + mPreviewSize.width + "*" + mPreviewSize.height);

			mAngle = CameraUtils.getCWFaceOrientation(context, mCameraID);
//			if (mCameraID == Camera.CameraInfo.CAMERA_FACING_FRONT) {
				mMirror = FaceInterface.cw_img_mirror_t.CW_IMAGE_MIRROR_HOR;
//			} else {
//				mMirror = FaceInterface.cw_img_mirror_t.CW_IMAGE_MIRROR_NONE;
//			}
			mFrameManagerThread.init(context, mPreviewSize.width, mPreviewSize.height,
					Contans.PREVIEW_DATA_FORMAT, mAngle, mMirror);
			mFrameManagerThread.start();
			getHolder().addCallback(this);
			if (mPreviewing) {
				requestLayout();
			} else {
				showCameraPreview();
			}
		}
	}

	private boolean isPortrait() {
		return (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
	}

	public boolean isPreview() {
		return mPreviewing;
	}
}