package com.kcert.fido.parser;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import com.kcert.fido.telegram.JHttpClient;
import com.kcert.fido.telegram.handler.HttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

public class JSONRequest  extends JSONProcess{
private String logTag	= this.getClass().getName();
	
	private SimpleDateFormat sf	= new SimpleDateFormat("yyyyMMddHHmmss",Locale.KOREA);
	
	private Context context;
	/**
	 * request parameters
	 * */
    private RequestParams params = new RequestParams();
    
    /**
     * 기본 JSONRequest 생성
     * @param context android.content.Context
     * */
	public JSONRequest(Context context) throws Exception{
		init(context,JSON_REQUEST_HEADER,JSON_REQUEST_BODY);
	}
	
    /**
     * JSONRequest 생성
     * @param context android.content.Context
     * @param headerName headerArea name
     * @param bodyName bodyArea name
     * */
	public JSONRequest(Context context,String headerName,String bodyName) throws Exception{
		init(context,headerName,bodyName);
	}
	
    /**
     * JSONRequest 초기화
     * @param context android.content.Context
     * @param headerName headerArea name
     * @param bodyName bodyArea name
     * */
	private void init(Context context,String headerName,String bodyName){
		this.context	= context;
		this.headerName	= headerName;
		this.bodyName	= bodyName;
		
		json		= new JSONObject();
		header		= new JSONObject();
		body		= new JSONObject();
		try {
			json.put(headerName, header);
			json.put(bodyName, body);
		} catch (JSONException e) {
		}
		setHeaderValue("H_CN_TP", "1");	//접속구분
		setHeaderValue("H_MAC", Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));	//android id
	}
	
    /**
     * 전문코드 설정
     * @param command 전문코드
     * */
	public void setCommand(String command) throws Exception{
		setHeaderValue("H_CMD_ID", command);	//전문코드
	}
	
    /**
     * 파일 파라미터 저장
     * @param entityName 파라미터명
     * @param file 파일
     * */
	public void setFile(String entityName,File file) throws Exception{
		params.put(entityName, file);
	}
	
    /**
     * Http 요청 전송
     * @param handler HttpResponseHandler
     * */
	public void request(String url, HttpResponseHandler handler) throws Exception{
		setHeaderValue("H_REQ_TM",sf.format(Calendar.getInstance().getTime()));//요청시각설정
		
		Log.d(logTag,"---------- request json -------------");
        Log.d(logTag,json.toString());
        
        params.put(JSON_REQUEST_PARAM, json.toString());//json data 저장
		
		//http 요청 전송
        try{
        	JHttpClient.newInstance(context).connect(url, params, handler);
        }catch(Exception e){
        	Log.e(logTag, "", e);
        	throw e;
        }
	}
	
	public void setTokenId(String tokenId){
		setHeaderValue("H_TKN_ID", tokenId);	//TOKEN ID문코드
	}
}
