package com.kcert.fido.parser;

import org.json.JSONObject;

import android.util.Log;

public class JSONResponse extends JSONProcess{
	
	public JSONResponse(String jsonData) throws Exception{
    	Log.d(logTag,"---------- request json -------------");
    	Log.d(logTag,jsonData);
		if(jsonData==null || "".equals(jsonData))
			throw new Exception("Invalid json data.["+jsonData+"]");
		try{
			json		= new JSONObject(jsonData);
		}catch(Exception e){
			throw e;
		}
		
		headerName	= JSON_REQUEST_HEADER;
		bodyName	= JSON_REQUEST_BODY;
		
		header		= json.getJSONObject(headerName);
		body		= json.getJSONObject(bodyName);
	}
	
	public JSONResponse(String jsonData, String headerName,String bodyName) throws Exception{
    	Log.d(logTag,"---------- request json -------------");
    	Log.d(logTag,jsonData);
		if(jsonData==null || "".equals(jsonData))
			throw new Exception("Invalid json data.["+jsonData+"]");
		try{
			json		= new JSONObject(jsonData);
		}catch(Exception e){
			throw e;
		}
		
		this.headerName	= headerName;
		this.bodyName	= bodyName;
		
		header	= json.getJSONObject(headerName);
		body		= json.getJSONObject(bodyName);
	}
	/**
	 * 통신결과 반환
	 * */
	public boolean getResult(){
		if(JSON_RESULT_CODE_ERR.equals(getHeaderString(JSON_RESULT_CODE)))
			return false;
		else
			return true;
	}
	/**
	 * 오류 메시지 반환
	 * */
	public String getMessage(){
		return getHeaderString(JSON_RESULT_MESSAGE);
	}
}
