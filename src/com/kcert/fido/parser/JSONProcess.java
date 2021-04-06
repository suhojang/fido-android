package com.kcert.fido.parser;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class JSONProcess {
	protected String logTag	= this.getClass().getName();
	
	/** json type  **/
	public static final String CONTENT_TYPE_JSON				= "application/json; charset=UTF-8";
	
	public static final String JSON_REQUEST_PARAM				= "requestJSONData";
	public static final String JSON_REQUEST_HEADER				= "HeaderArea";
	public static final String JSON_REQUEST_BODY				= "BodyArea";
	
	public static final String JSON_RESULT_CODE					= "H_RST_CD";
	public static final String JSON_RESULT_MESSAGE				= "H_RST_MSG";
	public static final String JSON_RECEIVE_TIME				= "H_RCV_TM";
	public static final String JSON_RESPONSE_TIME				= "H_RES_TM";
	public static final String JSON_RESULT_CODE_SUCC			= "000";
	public static final String JSON_RESULT_CODE_ERR				= "999";
	public static final String JSON_RESULT_CODE_WARN			= "888";
	
	protected JSONObject	json;
	protected JSONObject	header;
	protected JSONObject	body;

	protected String headerName;
	protected String bodyName;
	
	public JSONObject getHeader(){
		return header;
	}
	public String getHeaderString(String key){
		String val	= "";
		try{
			val	= header.getString(key);
		}catch(Exception e){
		}
		return val==null?"":val;
	}
	public long getHeaderNumber(String key){
		String val	= "";
		try{
			val	= header.getString(key);
		}catch(Exception e){
		}
		return (val==null||"".equals(val))?0:Long.parseLong(val);
	}
	public void removeHeaderValue(String key){
		try{
			header.remove(key);
		}catch(Exception e){
		}
	}
	public void setHeaderValue(String key,Object val){
		try{
			header.put(key, val==null?"":val);
		}catch(Exception e){
		}
	}
	public void setHeader(Map<String,?> map){
		try{
			String key	= null;
			Set<String> keys		= map.keySet();
			Iterator<String> iter	= keys.iterator();
			while(iter.hasNext()){
				key	= iter.next();
				header.put(key, map.get(key));
			}
		}catch(Exception e){
		}
	}
	
	public JSONObject getBody(){
		return body;
	}
	public String getBodyString(String key){
		String val	= "";
		try{
			val	= body.getString(key);
		}catch(Exception e){
		}
		return val==null?"":val;
	}
	public void setBodyValue(String key,Object val){
		try{
			body.put(key, val==null?"":val);
		}catch(Exception e){
		}
	}
	public void removeBodyValue(String key){
		try{
			body.remove(key);
		}catch(Exception e){
		}
	}
	public long getBodyNumber(String key){
		String val	= "";
		try{
			val	= body.getString(key);
		}catch(Exception e){
		}
		return (val==null||"".equals(val))?0:Long.parseLong(val);
	}
	public void setBody(Map<String,?> map){
		try{
			String key	= null;
			Set<String> keys		= map.keySet();
			Iterator<String> iter	= keys.iterator();
			while(iter.hasNext()){
				key	= iter.next();
				body.put(key, map.get(key));
			}
		}catch(Exception e){
		}
	}
	public String toString(){
		try{
			json.put(headerName, header);
			json.put(bodyName, body);
		} catch (JSONException e) {
		}
		return json.toString();
	}
}
