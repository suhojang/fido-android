package com.kcert.fido;

import com.kcert.fido.fingerprint.FingerPrintSupport;
import com.kcert.fido.parent.FIDOProperties;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class MainActivity extends Activity {
	public FingerPrintSupport fingerprint;
	
	private TextView btn_auth;
	private TextView btn_face;
	private FingerPrintFragment fragment;
	
	private static final String FRAGMENT_TAG	= "FINGERPRINT_DIALOG_FRAGMENT";
	
	private String[] PERMISSIONS1  = {Manifest.permission.READ_PHONE_STATE};
	private String[] PERMISSIONS2  = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
	
	private static final int PERMISSION_REQUEST_CODE1 = 1;
	private static final int PERMISSION_REQUEST_CODE2 = 2;
	
	private static final int FACE_DETECTOR_ACTIVITY	= 1000001;
	
	private static final String LICENSE_KEY	= "TEST_LICENSE_KEY";	//발급받으신 라이센스 키
	private static final String sURL	= "https://192.168.0.10:8443/CLA_MBL_001000/";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        btn_auth	= (TextView) findViewById(R.id.btn_auth);
        btn_auth.setOnClickListener(new View.OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		if(!hasPermissions(PERMISSIONS1)){
        			requestNecessaryPermissions(PERMISSIONS1, PERMISSION_REQUEST_CODE1);
        		}else{
        			fingerprint	= FingerPrintSupport.getInstance(MainActivity.this, LICENSE_KEY, sURL);
    				new Handler().postDelayed(new Runnable() {
    					@Override
    					public void run() {
    						fingerPrintStart();
    					}
    					
    				}, 500);
        		}
        	}
        });
        
        btn_face	= (TextView) findViewById(R.id.btn_face);
        btn_face.setOnClickListener(new View.OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		if(!hasPermissions(PERMISSIONS2)){
        			requestNecessaryPermissions(PERMISSIONS2, PERMISSION_REQUEST_CODE2);
        		}else{
        			Intent intent	= new Intent(MainActivity.this, FaceActivity.class);
        			intent.putExtra("LICENSE_KEY", LICENSE_KEY);
        			intent.putExtra("sURL", sURL);
        			startActivityForResult(intent, FACE_DETECTOR_ACTIVITY);
        		}
        	}
        });
    }
    
    private boolean hasPermissions(String[] permissions) {
        int ret = 0;
        for (String perms : permissions){
            ret = checkCallingOrSelfPermission(perms);
            if (!(ret == PackageManager.PERMISSION_GRANTED)){
                return false;
            }
        }
        return true;
    }

    private void requestNecessaryPermissions(String[] permissions, int PERMISSION_REQUEST_CODE) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(MainActivity.this, permissions, PERMISSION_REQUEST_CODE);
        }
    }
    
    public void fingerPrintStart(){
    	if (fingerprint.FingerPrintDeviceCheck()) {
			fragment	=  new FingerPrintFragment(fingerprint);
			fragment.show(getFragmentManager(), FRAGMENT_TAG);
		}
    }
    
    /**
     * 검증성공
     */
    public void onPurchased(boolean yn) {
        showConfirmation(yn);
    }

    /**
     * 검증 실패
     */
    public void onPurchaseFailed() {
        Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
    }

    private void showConfirmation(boolean yn) {
        if (yn) {
            
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    	super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    	boolean cameraAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
    	boolean writeAccepted   = grantResults[1] == PackageManager.PERMISSION_GRANTED;
    	
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE1:
                if (grantResults.length > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (!cameraAccepted || !writeAccepted) {
                            showDialogforPermission("지문 인증을 실행하려면 권한을 허가하셔야 합니다.", FIDOProperties.FIDO_FINGERPRINT);
                            return;
                        } else {
                        	fingerprint	= FingerPrintSupport.getInstance(MainActivity.this, LICENSE_KEY, sURL);
							
							new Handler().postDelayed(new Runnable() {
								@Override
								public void run() {
									fingerPrintStart();
								}
								
							}, 500);
                        }
                    }
                }
                break;
            case PERMISSION_REQUEST_CODE2:
                if (grantResults.length > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (!cameraAccepted || !writeAccepted) {
                            showDialogforPermission("얼굴 인증을 실행하려면 권한을 허가하셔야 합니다.", FIDOProperties.FIDO_FACE_DETECTION);
                            return;
                        } else {
                        	
                        }
                    }
                }
                break;
        }
    }
    
    private void showDialogforPermission(String msg, final int sec) {
        final AlertDialog.Builder myDialog = new AlertDialog.Builder(MainActivity.this);
        myDialog.setTitle("알림");
        myDialog.setMessage(msg);
        myDialog.setCancelable(false);
        myDialog.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    ActivityCompat.requestPermissions(MainActivity.this, sec==FIDOProperties.FIDO_FINGERPRINT?PERMISSIONS1:PERMISSIONS2, sec==FIDOProperties.FIDO_FINGERPRINT?PERMISSION_REQUEST_CODE1:PERMISSION_REQUEST_CODE2);
                }
            }
        });
        myDialog.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                finish();
            }
        });
        myDialog.show();
    }
    
    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
    	super.startActivityForResult(intent, requestCode);
    	
    	switch (requestCode) {
		case FACE_DETECTOR_ACTIVITY:
			break;

		default:
			break;
		}
    }
}
