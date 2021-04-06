package com.kcert.fido.telegram.handler;

public interface HttpResponseHandler {
	public void onSuccess(byte[] content);
	public void onFailure(byte[] responseBody, java.lang.Throwable error);
	public void onFinish();
}
