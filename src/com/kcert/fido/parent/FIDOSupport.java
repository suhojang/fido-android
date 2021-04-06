package com.kcert.fido.parent;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.UUID;

import com.kcert.fido.io.JByteArrayStream;
import com.kcert.fido.lang.DefinedException;
import com.kcert.fido.parser.JSONRequest;
import com.kcert.fido.parser.JSONResponse;
import com.kcert.fido.telegram.handler.HttpResponseHandler;
import com.kcert.fido.util.Preferences;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.hardware.fingerprint.FingerprintManager;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

@SuppressLint("NewApi")
public class FIDOSupport {
	protected ProgressDialog loading;
	
	private boolean licenseYn;
	
	protected Context context;

	protected String server_url;
	protected long nonce;
	private boolean authenticated;
	
	protected FIDOKeyManager manager;
	
	protected KeyguardManager mKeyguardManager;
	protected FingerprintManager mFingerprintManager;
	protected KeyStore mKeyStore;
	protected KeyPairGenerator mKeyPairGenerator;
	protected Signature mSignature;
	protected Preferences pref;
	
	public FIDOSupport(){
	}
	
	public FIDOSupport(Context context, String serialNo, String server_url){
		this.context				= context;
		this.server_url				= server_url;
		this.mKeyguardManager		= context.getSystemService(KeyguardManager.class);
		this.mFingerprintManager	= context.getSystemService(FingerprintManager.class);
		this.manager				= FIDOKeyManager.getInstance(context);
		this.pref					= new Preferences(context);
		
		this.mKeyPairGenerator		= manager.getKeyPairGenerator();
		this.mKeyStore				= manager.getKeystore();
		this.mSignature				= manager.getSignature(mKeyStore);
		
		init(serialNo);
	}
	
	protected void init(String serialNo){
		//licenseKeyCheck(serialNo);
		setLicenseDevice(true);
		makeKeyCreate();
	}
	
	private void licenseKeyCheck(String serialNo){
		loadingDialog(true);
		try{
			JSONRequest request	= new JSONRequest(context);
			request.setCommand("MBL_LCK_CHK");
			request.setBodyValue("serialNo", serialNo);
			request.request(FIDOProperties.LICENSE_URL, new HttpResponseHandler() {
				@Override
				public void onSuccess(byte[] content) {
					JSONResponse response	= null;
					try{
						response	= new JSONResponse(new String(content));
						if(!response.getResult())
							throw new DefinedException(response.getMessage());
						if("Y".equals(response.getBodyString("result"))){
							setLicenseDevice(true);
							makeKeyCreate();
						}else{
							setLicenseDevice(false);
							Toast.makeText(context, "라이센스 [Serial Number] 인증에 실패 하였습니다. 관리자에게 문의 하시기 바랍니다.", Toast.LENGTH_LONG).show();
						}
					}catch(Exception e){
						loadingDialog(false);
						e.printStackTrace();
					}
				}
				
				@Override
				public void onFinish() {
				}
				
				@Override
				public void onFailure(byte[] responseBody, Throwable error) {
					loadingDialog(false);
					setLicenseDevice(false);
					Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();
				}
			});
		}catch(Exception e){
			loadingDialog(false);
			e.printStackTrace();
		}
	}
	
	public void makeKeyCreate(){
		PrivateKey key = null;
		try{
			mKeyStore.load(null);
			key	= (PrivateKey) mKeyStore.getKey(FIDOProperties.KEY_NAME, null);
		}catch(Exception e){
			loadingDialog(false);
			e.printStackTrace();
		}
		
		if(key==null)
			createKeyPair();
		
		/*loadingDialog(true);
		if ("".equals(pref.getString("tokenId", "")))
			publicKeyEnroll();
		else
			getClientNonce();*/
	}
	
	protected void createKeyPair() {
        try {
            mKeyPairGenerator.initialize(
                    new KeyGenParameterSpec.Builder(FIDOProperties.KEY_NAME,
                            KeyProperties.PURPOSE_SIGN)
                            .setDigests(KeyProperties.DIGEST_SHA256)
                            .setAlgorithmParameterSpec(new ECGenParameterSpec("secp256r1"))
                            .setUserAuthenticationRequired(true)
                            .build());
            mKeyPairGenerator.generateKeyPair();
            
        } catch (InvalidAlgorithmParameterException e) {
        	loadingDialog(false);
            throw new RuntimeException(e);
        }
    }
	
	protected void publicKeyEnroll(){
		String deviceId	= getDeviceUUID();
		try{
			mKeyStore.load(null);
			PublicKey publicKey			= mKeyStore.getCertificate(FIDOProperties.KEY_NAME).getPublicKey();
			KeyFactory factory			= KeyFactory.getInstance(publicKey.getAlgorithm());
			X509EncodedKeySpec spec		= new X509EncodedKeySpec(publicKey.getEncoded());
			PublicKey verificationKey	= factory.generatePublic(spec);
			
			String key		= Base64.encodeToString(verificationKey.getEncoded(), Base64.DEFAULT);
			
			JSONRequest request	= new JSONRequest(context);
			request.setCommand("MBL_KEY_ENROLL");
			request.setBodyValue("key", key);
			request.setBodyValue("deviceId", deviceId);
			request.request(getServerUrl(), new HttpResponseHandler() {
				@Override
				public void onSuccess(byte[] content) {
					JSONResponse response	= null;
					try{
						response	= new JSONResponse(new String(content));
						if(!response.getResult())
							throw new DefinedException(response.getMessage());
						pref.setString("tokenId", response.getBodyString("tokenId"));
					}catch(Exception e){
						loadingDialog(false);
						e.printStackTrace();
					}
				}
				
				@Override
				public void onFinish() {
					getClientNonce();
					loadingDialog(false);
				}
				
				@Override
				public void onFailure(byte[] responseBody, Throwable error) {
					loadingDialog(false);
					Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();
				}
			});
			
		}catch(KeyStoreException | CertificateException | NoSuchAlgorithmException |
                IOException | InvalidKeySpecException e){
			loadingDialog(false);
			e.printStackTrace();
		}catch(Exception e){
			loadingDialog(false);
			e.printStackTrace();
		}
	}
	
	protected void getClientNonce(){
		try{
			JSONRequest request	= new JSONRequest(context);
			request.setCommand("MBL_CLA_NONCE");
			request.setBodyValue("tokenId", getTokenId());
			request.request(getServerUrl(), new HttpResponseHandler() {
				@Override
				public void onSuccess(byte[] content) {
					JSONResponse response	= null;
					try{
						response	= new JSONResponse(new String(content));
						if(!response.getResult())
							throw new DefinedException(response.getMessage());
						
						nonce	= response.getBodyNumber("nonce");
					}catch(Exception e){
						loadingDialog(false);
						e.printStackTrace();
					}
				}
	
				@Override
				public void onFailure(byte[] responseBody, Throwable error) {
					loadingDialog(false);
					Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();
				}
	
				@Override
				public void onFinish() {
					loadingDialog(false);
				}
			});
		}catch(Exception e){
			loadingDialog(false);
			e.printStackTrace();
		}
	}
	
	protected boolean initSignature() {
		try {
            mKeyStore.load(null);
            PrivateKey key = (PrivateKey) mKeyStore.getKey(FIDOProperties.KEY_NAME, null);
            mSignature.initSign(key);
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {
        	Toast.makeText(context, "[개인키 초기화에 실패 하였습니다.] : " + e.getMessage(), Toast.LENGTH_LONG).show();
            return false;
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {
        	Toast.makeText(context, "[개인키 초기화에 실패 하였습니다.] : " + e.getMessage(), Toast.LENGTH_LONG).show();
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }
	
	public boolean clientAuthenticated(){
		return true;
	}
	
	public boolean serverAuthenticated(){
		loadingDialog(true);
		try{
			byte[] b	= JByteArrayStream.toByteArray(getTokenId(), nonce);
			
			mSignature.update(b);
			final byte[] sigByte	= mSignature.sign();
			
			String encodeByte	= Base64.encodeToString(sigByte, Base64.DEFAULT);
			
			JSONRequest request	= new JSONRequest(context);
			request.setCommand("MBL_KEY_VERIFY");
			request.setBodyValue("tokenId", getTokenId());
			request.setBodyValue("nonce", nonce);
			request.setBodyValue("encodeByte", encodeByte);
			request.request(getServerUrl(), new HttpResponseHandler() {
				@Override
				public void onSuccess(byte[] content) {
					JSONResponse response	= null;
					try{
						response	= new JSONResponse(new String(content));
						if(!response.getResult())
							throw new DefinedException(response.getMessage());
						
						String result	= response.getBodyString("result");
						if ("AUTH_Y".equals(result)) {
							setAuthenticated(true);
							Toast.makeText(context, "인증이 완료 되었습니다.", Toast.LENGTH_LONG).show();
						} else {
							setAuthenticated(false);
							Toast.makeText(context, "[인증 실패 : 관리자에게 문의 하세요.]", Toast.LENGTH_LONG).show();
						}
						
						
					}catch(Exception e){
						e.printStackTrace();
						throw new RuntimeException(e);
					}
				}
	
				@Override
				public void onFailure(byte[] responseBody, Throwable error) {
					setAuthenticated(false);
					Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();
				}
	
				@Override
				public void onFinish() {
					loadingDialog(false);
				}
			});
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return getAuthenticated();
	}
	
	private String getDeviceUUID(){
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        
        String tmDevice		= tm.getDeviceId();
        String tmSerial		= tm.getSimSerialNumber();
        String androidId	= android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        return new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode()).toString();
    }
	
	protected boolean isLicenseCheck(){
		if(!getLicenseDevice()){
			Toast.makeText(context, "라이센스 [Serial Number] 인증에 실패 하였습니다. 관리자에게 문의 하시기 바랍니다.", Toast.LENGTH_LONG).show();
			return false;
		}
		return true;
	}
	
	private void setLicenseDevice(boolean yn){
		this.licenseYn	= yn;
	}
	
	protected boolean getLicenseDevice(){
		return licenseYn;
	}

	private String getServerUrl(){
		return server_url;
	}
	
	public Signature getSignature(){
		return mSignature;
	}
	
	private String getTokenId(){
		return pref.getString("tokenId", "");
	}
	
	public FingerprintManager getFingerprintManager(){
		return mFingerprintManager;
	}
	
	private void setAuthenticated(boolean yn){
		authenticated	= yn;
	}
	
	private boolean getAuthenticated(){
		return authenticated;
	}
	
	public void loadingDialog(boolean yn){
		if(yn){
			try{if(loading!=null)loading.dismiss();}
			catch (NullPointerException e) {
				Log.e("",e.getMessage());
			} catch (IndexOutOfBoundsException e) {
				Log.e("",e.getMessage());
			} catch (ClassCastException e) {
				Log.e("",e.getMessage());
			} catch (Exception e) {
				Log.e("",e.getMessage());
			}
			loading	= ProgressDialog.show(context, null, "처리중입니다. 잠시만 기다려 주십시오.");
			loading.setCancelable(false);
			loading.setOnCancelListener(new OnCancelListener(){
				@Override
				public void onCancel(DialogInterface arg0) {
					loading.dismiss();
				}
			});
		}else{
			if(loading!=null)
				loading.dismiss();
		}
	}
}
