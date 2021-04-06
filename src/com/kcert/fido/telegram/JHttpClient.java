package com.kcert.fido.telegram;

import java.util.Locale;

import com.kcert.fido.lang.DefinedException;
import com.kcert.fido.network.Network;
import com.kcert.fido.telegram.handler.HttpResponseHandler;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.content.Context;
import cz.msebera.android.httpclient.Header;

public class JHttpClient{
	private AsyncHttpClient client;
	private Context context;
	private static JHttpClient instance;
	public static final int DEFAULT_TIME_OUT	= 10*1000;	//milliseconds

	/**
	 * get public JHttpClient instance - can keep session
	 * */
	public static JHttpClient getInstance(Context context){
		synchronized(JHttpClient.class){
			if(instance==null)
				instance	= new JHttpClient(context);
		}
		return instance;
	}
	
	/**
	 * make new JHttpClient
	 * */
	public static JHttpClient newInstance(Context context){
		return new JHttpClient(context);
	}
	/**
	 * constructor
	 * */
	public JHttpClient(Context context) {
		this.context = context;
		client = new AsyncHttpClient();
		client.setTimeout(DEFAULT_TIME_OUT);	//client default = 10sec
	}
	
	
	public AsyncHttpClient getAsyncHttpsClient(int sslPort){
		client = new AsyncHttpClient(true, 80, sslPort);
		client.setTimeout(DEFAULT_TIME_OUT);	//client default = 10sec
		
		return client;
	}
	
	/**
	 * connect http
	 * 
	 * @param url host url
	 * @param params Request parameters
	 * */
	public void connect(String url,RequestParams params,final HttpResponseHandler handler) throws Exception{
		connect(url,params,handler,DEFAULT_TIME_OUT);
	}
	/**
	 * connect http
	 * 
	 * @param url host url
	 * @param params Request parameters
	 * */
	public void connect(String url,RequestParams params,final HttpResponseHandler handler,int timeout) throws Exception{
		if(!checkOnline())
			throw new DefinedException("���ͳݿ� ������ �� �����ϴ�.");

		if(url.toUpperCase(Locale.KOREA).startsWith("HTTPS")){
			String ssl	= url;
			ssl			= ssl.replaceAll("https://", "");
			String port	= "443";
			if (ssl.indexOf(":")>=0) {
				port	= ssl.substring(ssl.indexOf(":")+1);
				port	= port.substring(0, port.indexOf("/"));
			}
			client	= getAsyncHttpsClient(Integer.parseInt(port));
		}
		
		client.setTimeout(timeout);
		client.post(context,url,params,new AsyncHttpResponseHandler(){
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody){
				handler.onSuccess(responseBody);
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, java.lang.Throwable error){
				DefinedException e	= new DefinedException(error); 
				handler.onFailure(responseBody,e);
			}
			@Override
			public void onFinish(){
				super.onFinish();
				handler.onFinish();
			}
		});
	}
	/**
	 * check network status
	 * 
	 * */
	private boolean checkOnline(){
		return Network.getNetworkType(context)==Network.NETWORK_STATUS_DISABLE?false:true;
	}
}
