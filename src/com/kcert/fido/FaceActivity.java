package com.kcert.fido;

import com.kcert.fido.face.FaceDetectorSupport;
import com.kcert.fido.face.ui.camera.CameraSourcePreview;
import com.kcert.fido.face.ui.camera.GraphicOverlay;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

@SuppressLint("NewApi")
public final class FaceActivity extends Activity {
    private FaceDetectorSupport support;
    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;

    /**
     * 얼굴 감지기 생성
     */
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_face);

        mPreview		= (CameraSourcePreview) findViewById(R.id.preview);
        mGraphicOverlay	= (GraphicOverlay) findViewById(R.id.faceOverlay);
        
        support			= FaceDetectorSupport.getInstance(this, bundle.getString("LICENSE_KEY"), bundle.getString("sURL"), mPreview, mGraphicOverlay);
    }

    /**
     * 카메라 재시작
     */
    @Override
    protected void onResume() {
        super.onResume();
        
        if (support!=null)
        	support.startCameraSource();
    }

    /**
     * 카메라 중지
     */
    @Override
    protected void onPause() {
        super.onPause();
        
        if (support!=null)
			support.mOnPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        if (support!=null)
        	support.mOnDestroy();
    }

    public void faceRecognition(String path){
    	if (support.getFaceVerify(path)) {
    		showDialog("얼굴인식이 완료 되었습니다.");
		} else {
			showDialog("원본 얼굴과 다릅니다.\n자신의 얼굴을 카메라 정면으로 올바르게 인식시켜 주십시오.");
		}
    }
    
    private void showDialog(String msg) {
        final AlertDialog.Builder myDialog = new AlertDialog.Builder(FaceActivity.this);
        myDialog.setTitle("알림");
        myDialog.setMessage(msg);
        myDialog.setCancelable(false);
        myDialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                finish();
            }
        });
        myDialog.show();
    }
}