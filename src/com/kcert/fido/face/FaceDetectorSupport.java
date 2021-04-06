package com.kcert.fido.face;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;
import com.google.android.gms.vision.face.LargestFaceFocusingProcessor;
import com.kcert.fido.face.patch.SafeFaceDetector;
import com.kcert.fido.face.ui.camera.CameraSourcePreview;
import com.kcert.fido.face.ui.camera.GraphicOverlay;
import com.kcert.fido.face.verify.FaceLocVerify;
import com.kcert.fido.fingerprint.FingerPrintSupport;
import com.kcert.fido.parent.FIDOProperties;
import com.kcert.fido.parent.FIDOSupport;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

public class FaceDetectorSupport extends FIDOSupport {
	private static FaceDetectorSupport instance;
	
	private Context mContext;
	private CameraSource mCameraSource;
    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;
    
    private boolean mIsFrontFacing = true;
    private static final int RC_HANDLE_GMS = 9001;

	private FaceDetectorSupport(){
	}
	
	private FaceDetectorSupport(Context context, String serialNo, String server_url, CameraSourcePreview preview, GraphicOverlay overlay) {
		super(context, serialNo, server_url);
		this.mContext			= context;
		this.mPreview			= preview;
		this.mGraphicOverlay	= overlay;
		
		init();
	}
	
	public static FaceDetectorSupport getInstance(Context context, String serialNo, String server_url, CameraSourcePreview preview, GraphicOverlay overlay){
		synchronized(FingerPrintSupport.class){
			if(instance==null){
				instance	= new FaceDetectorSupport(context, serialNo, server_url, preview, overlay);
			}
		}
		return instance;
	}
	
	public void init(){
		createCameraSource();
	}
	
	private void createCameraSource() {
		FaceDetector detector = createFaceDetector(mContext);
		int facing = CameraSource.CAMERA_FACING_FRONT;

		mCameraSource = new CameraSource.Builder(context, detector)
				.setFacing(facing)
				.setRequestedPreviewSize(320, 240)
				.setRequestedFps(60.0f)
				.setAutoFocusEnabled(true)
				.build();
    }
	
	@NonNull
    private FaceDetector createFaceDetector(Context context) {
		FaceDetector detector = new FaceDetector.Builder(context)
				.setLandmarkType(FaceDetector.ALL_LANDMARKS)
				.setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
				.setTrackingEnabled(true)
				.setMode(FaceDetector.FAST_MODE)
				.setProminentFaceOnly(mIsFrontFacing)
				.setMinFaceSize(mIsFrontFacing ? 0.35f : 0.15f)
				.build();

		Detector.Processor<Face> processor;
		if (mIsFrontFacing) {
			Tracker<Face> tracker = new FaceTracker(mGraphicOverlay);
			processor = new LargestFaceFocusingProcessor.Builder(detector, tracker).build();
		} else {
			MultiProcessor.Factory<Face> factory = new MultiProcessor.Factory<Face>() {
				@Override
				public Tracker<Face> create(Face face) {
					return new FaceTracker(mGraphicOverlay);
				}
			};
			processor = new MultiProcessor.Builder<>(factory).build();
		}
		detector.setProcessor(processor);

		if (!detector.isOperational()) {
			IntentFilter lowStorageFilter	= new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
			boolean hasLowStorage			= mContext.registerReceiver(null, lowStorageFilter) != null;

			if (hasLowStorage) {
				Toast.makeText(mContext, "디바이스 저장공간이 부족하여 얼굴인식 프로세스를 다운로드 할 수 없습니다.\n저장공간을 확보해 주세요.", Toast.LENGTH_LONG).show();
			}
		}
        return detector;
    }
	
	public void startCameraSource() {
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(mContext);
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg = GoogleApiAvailability.getInstance().getErrorDialog((Activity)mContext, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(this.getClass().getName(), "카메라 소스를 시작할 수 없습니다.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }
	
	public CameraSource getCameraSource(){
		return mCameraSource;
	}
	
	public void mOnDestroy(){
		if (mCameraSource != null)
			mCameraSource.release();
	}
	
	public void mOnPause(){
		mPreview.stop();
	}
	
	/**
	 * 얼굴 검증 시작
	 * @return
	 */
	public boolean getFaceVerify(String path){
		FaceLocVerify flvf	= FaceLocVerify.getInstance();
		
		return flvf.getFaceVerify(
				mContext, 
				Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + mContext.getPackageName() + File.separator + FIDOProperties.ORIGINAL_FACE_FILE_NAME, 
				path
				);
	}
	
	@SuppressLint("UseSparseArrays")
	public Map<Integer,Map<String,String>> getFaceLocation(String path){
		Map<Integer, Map<String, String>> map	= new HashMap<Integer, Map<String, String>>();
		InputStream stream = null;
		
        try {
            stream = new FileInputStream(new File(path));

            Bitmap bitmap = BitmapFactory.decodeStream(stream);

            FaceDetector detector = new FaceDetector.Builder(mContext)
                    .setTrackingEnabled(false)
                    .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                    .build();
            Detector<Face> safeDetector = new SafeFaceDetector(detector);

            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<Face> faces = safeDetector.detect(frame);

            for (int i = 0; i < faces.size(); ++i) {
                Face face = faces.valueAt(i);
                for (Landmark landmark : face.getLandmarks()) {
                    int cx = (int) (landmark.getPosition().x);
                    int cy = (int) (landmark.getPosition().y);

                    Map<String,String> loc	= new HashMap<String,String>();
                    loc.put("x", String.valueOf(cx));
                    loc.put("y", String.valueOf(cy));
                    
                    map.put(landmark.getType(), loc);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
		
		return map;
	}
}
