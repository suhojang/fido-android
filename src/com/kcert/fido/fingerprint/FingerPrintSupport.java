package com.kcert.fido.fingerprint;

import java.util.Locale;

import com.kcert.fido.parent.FIDOProperties;
import com.kcert.fido.parent.FIDOSupport;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.widget.Toast;

@SuppressLint("NewApi")
public class FingerPrintSupport extends FIDOSupport {
	private static FingerPrintSupport instance;
	
	private FingerPrintSupport(){
	}
	
	private FingerPrintSupport(Context context, String serialNo, String server_url) {
		super(context, serialNo, server_url);
	}
	
	public static FingerPrintSupport getInstance(Context context, String serialNo, String server_url){
		synchronized(FingerPrintSupport.class){
			if(instance==null){
				instance	= new FingerPrintSupport(context, serialNo, server_url);
			}
		}
		return instance;
	}
	
	public boolean FingerPrintDeviceCheck() {
		if(!isDeviceCheck())
			return false;
		if(!isLicenseCheck())
			return false;
		if(!isKeyguardSecure())
			return false;
		if(!isHasEnrolledFingerprints())
			return false;
		if(!initSignature())
			return false;
		return true;
	}
	
	public boolean FingerPrintKeyCheck() {
		if(!isKeyguardSecure())
			return false;
		if(!isHasEnrolledFingerprints())
			return false;
		if(!initSignature())
			return false;
		return true;
	}
	
	public boolean isNotFingerPrintModel(){
		boolean isNotFingerPrint	= true;
		for(int i=0;i<FIDOProperties.NOT_FINGERPRINT_MODEL.length;i++){
			if(Build.MODEL.toUpperCase(Locale.KOREA).equals(FIDOProperties.NOT_FINGERPRINT_MODEL[i])){
				isNotFingerPrint	= false;
				break;
			}
		}
		return isNotFingerPrint;
	}
	
	private boolean isDeviceCheck(){
		if(!((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) && isNotFingerPrintModel())){
			Toast.makeText(context, "지문인증을 지원하지 않는 기기 입니다.", Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}
	
	private boolean isKeyguardSecure() {
		if (!mKeyguardManager.isKeyguardSecure()) {
			Toast.makeText(context, "보안 잠금 화면이 설정되지 않았습니다.\n잠금 화면을 설정하려면 [설정 -> 보안 -> 지문]으로 이동 하십시오.", Toast.LENGTH_LONG).show();
			return false;
		}
		return true;
	}
	
	public boolean isHasEnrolledFingerprints() {
		 if (!mFingerprintManager.hasEnrolledFingerprints()) {
			 Toast.makeText(context, "[설정 -> 보안 -> 지문]으로 이동하여 하나 이상의 지문을 등록 하십시오.", Toast.LENGTH_LONG).show();
			 return false;
		 }
		 return true;
	}
}
