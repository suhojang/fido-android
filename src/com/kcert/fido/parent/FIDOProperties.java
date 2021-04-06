package com.kcert.fido.parent;

public class FIDOProperties {
	public final static String ALGORITHM	= "SHA256withECDSA";
	public final static String KEY_NAME		= "KCERT_FIDO_KEY";
	public final static String[] NOT_FINGERPRINT_MODEL	= {
										"SM-G900"
										,"SM-G850"
										,"SM-N910"
										};
	//public final static String LICENSE_URL	= "https://192.168.0.10:8443/CLA_MBL_001000/";
	public final static String LICENSE_URL	= "http://kcert.co.kr/MBL_LCN_CHK.do";
	
	public static final int FIDO_FINGERPRINT	= 0;
	public static final int FIDO_FACE_DETECTION	= 1;
	
	public static final String ORIGINAL_FACE_FILE_NAME		= "FACE_ORIGINAL.JPEG";
	public static final String VERIFY_FACE_FILE_NAME		= "FACE_VERIFY.JPEG";
	public static final String VERIFY_FACE_TMP_FILE_NAME	= "FACE_VERIFY_TMP.JPEG";
}
