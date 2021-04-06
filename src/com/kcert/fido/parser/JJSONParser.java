package com.kcert.fido.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class JJSONParser{
	public static final String RESULT_CODE_KEY			= "AJAX_RESULT";
	public static final String RESULT_CODE_SUCCESS		= "Y";
	public static final String RESULT_CODE_ERROR		= "N";
	public static final String RESULT_MESSAGE_KEY		= "AJAX_MESSAGE";
	public static final String RESULT_OBJECT_KEY		= "AJAX_OBJECT";
	
	public static ArrayList<?> readListValues(String content) throws JsonParseException, JsonMappingException, IOException{
		content	= jsonpToJson(content);

		ObjectMapper mapper	= new ObjectMapper();
		return mapper.readValue(content, ArrayList.class);
	}
	
	public static HashMap<?,?> readMapValues(String content) throws JsonParseException, JsonMappingException, IOException{
		content	= jsonpToJson(content);
		
		ObjectMapper mapper	= new ObjectMapper();
		return mapper.readValue(content, HashMap.class);
	}
	
	public static String toJsonString(Collection<?> obj) throws JsonGenerationException, JsonMappingException, IOException{
		ObjectMapper mapper	= new ObjectMapper();
		return mapper.writeValueAsString(obj);
	}
	
	public static String getRsultCode(Map<?,?> jsonMap){
		return (String) jsonMap.get(RESULT_CODE_KEY);
	}
	public static String getRsultMessage(Map<?,?> jsonMap){
		return (String) jsonMap.get(RESULT_MESSAGE_KEY);
	}
	public static Map<?,?> getAjaxObject(Map<?,?> jsonMap){
		return (Map<?,?>) jsonMap.get(RESULT_OBJECT_KEY);
	}
	public static Object getAjaxValue(Map<?,?> jsonMap,String key){
		return ((Map<?,?>) jsonMap.get(RESULT_OBJECT_KEY)).get(key);
	}
	
	
	public final static String jsonpToJson(String content){
		if(content==null)
			return null;
		content	= content.trim();
		if(content.startsWith("("))
			content	= content.substring(1);
		if(content.endsWith(")"))
			content	= content.substring(0,content.length()-1);
		return content;
	}
}
