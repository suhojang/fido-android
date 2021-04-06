package com.kcert.fido.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Telephony;
import android.view.Display;
import android.view.WindowManager;

public class Utils {
    @SuppressLint("NewApi")
    public static Intent sendSMS(Context context, String message){
    	if (Build.VERSION.SDK_INT > 18) {
	    	String defaultSmsPackageName = Telephony.Sms.getDefaultSmsPackage(context);
	    	Intent intent = new Intent(Intent.ACTION_SEND);
	    	intent.setType("text/plain");
	    	intent.putExtra(Intent.EXTRA_TEXT, message);
	    	if (defaultSmsPackageName != null) {
	    		intent.setPackage(defaultSmsPackageName);
	    	}
	    	return intent;
    	} else {
    		//API Level 18 이하는 기존코드(JellyBean)
    		Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("sms:"));
	    	intent.putExtra("sms_body", message);
	    	return intent;
    	}
    }
    
    @SuppressLint("NewApi")
    public static Intent sendSMS(Context context, String message, String phoneNumber){
    	Intent	intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("sms:"+phoneNumber));
    	intent.putExtra("sms_body", message);
    	return intent;
    }
    
    /**
     *전화번호 포맷 설정
     * @param phoneNo
     * @return
     */
    public static String phoneFormat(String phoneNo){
		if (phoneNo.length() == 0){
			return phoneNo;
		}
	      
		String strTel = phoneNo;
		String[] strDDD = {"02" , "031", "032", "033", "041", "042", "043",
				"051", "052", "053", "054", "055", "061", "062",
				"063", "064", "010", "011", "012", "013", "015",
				"016", "017", "018", "019","070"};
		
		if (strTel.length() < 9) {
			return strTel;
		} else if (strTel.substring(0,2).equals(strDDD[0])) {
			strTel = strTel.substring(0,2) + '-' + strTel.substring(2, strTel.length()-4)
			+ '-' + strTel.substring(strTel.length() -4, strTel.length());
		} else {
			for(int i=1; i < strDDD.length; i++) {
				if (strTel.substring(0,3).equals(strDDD[i])) {
					strTel = strTel.substring(0,3) + '-' + strTel.substring(3, strTel.length()-4)
					+ '-' + strTel.substring(strTel.length() -4, strTel.length());
				}
			}
		}
		return strTel;
	}
    
    /**
     * 단말기 density 구함
     * @param con
     * 사용법 : if(getDensity(context) == 2f && (float으로 형변환해서 사용 해야함.)
     */
    public static float getDensity(Context con) {
        float density = 0.0f;
        density  = con.getResources().getDisplayMetrics().density;
        return density;
    }
     
    /**
     * px을 dp로 변환
     * @param con
     * @param px
     * @return dp
     */
    public static int getPxToDp(Context con, int px) {
        float density = 0.0f;
        density  = con.getResources().getDisplayMetrics().density;
        return (int)(px / density);
    }
     
    /**
     *  dp를 px로 변환
     * @param con
     * @param dp
     * @return px
     */
    public static int getDpToPix(Context con, double dp) {
        float density = 0.0f;
        density  = con.getResources().getDisplayMetrics().density;
        return (int)(dp * density + 0.5);
    }
     
    /** 
     * 단말기 가로 해상도 구하기
     * @param activity
     * @return width
     */
    @SuppressWarnings("deprecation")
	public static int getScreenWidth(Activity activity) {
        int width = 0;
        width = activity.getWindowManager().getDefaultDisplay().getWidth();
        return width;
    }
     
    /** 
     * 단말기 세로 해상도 구하기
     * @param activity
     * @return hight
     */
	@SuppressWarnings("deprecation")
    public static int getScreenHeight(Activity activity) {
        int height = 0;
        height = activity.getWindowManager().getDefaultDisplay().getHeight();
        return height;
    }
     
    /** 
     * 단말기 가로 해상도 구하기
     * @param context
     */
    @SuppressWarnings("deprecation")
    public static int getScreenWidth(Context context) {
        Display dis = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay(); 
        int width = dis.getWidth();  
        return width;
    }
     
    /** 
     * 단말기 세로 해상도 구하기
     * @param context
     */
    @SuppressWarnings("deprecation")
	public static int getScreenHeight(Context context) {
        Display dis = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay(); 
        int height = dis.getHeight(); 
        return height;
    }
    
    public static String getSizeType(Context context){
    	/*
    	-일반 스마트폰 
		ldpi:약 230x320;약 3인치 
		mdpi:약 320x480;약 3.5인치; 480x800;약 5인치 
		hdpi:약 480x800;약 4인치 
		xhdpi:약 1280x800;약 4.5인치 
		xxhdpi:약 1920x1080; 약 5인치
		xxxhdpi:약 1440x2560 약 5.5인치
		
		-태블릿
		large-hdpi:약 1024x600;5인치 이상 
		xlarge-mdpi:1280x800;약 8인치 이상
		*/ 
    	int height	= getScreenHeight(context);
    	int width	= getScreenWidth(context);

    	if (width*height >= 1440*2560) {
			return "xxxhdpi";
		} else if (width*height < 1440*2560 && width*height >= 1280*800) {
			return "xxhdpi";
		} else if (width*height < 1280*800 && width*height >= 480*800){
			return "xhdpi";
		} else if (width*height < 480*800 && width*height >= 320*480){
			return "hdpi";
		} else if (width*height < 320*480 && width*height >= 230*320){
			return "mdpi";
		} else if (width*height < 230*320){
			return "ldpi";
		}
    	return "xxhdpi";
    }
}
