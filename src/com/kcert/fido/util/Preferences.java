package com.kcert.fido.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

@SuppressLint({ "CommitPrefEdits", "WorldReadableFiles" })
public class Preferences {
	private final SharedPreferences pref;
	private final SharedPreferences.Editor editor ;

	@SuppressWarnings("deprecation")
	public Preferences(Context context) {
		pref = context.getSharedPreferences(context.getPackageName().replace(".", ""), Context.MODE_WORLD_READABLE);
		editor = pref.edit();
	}
	public long getLong(String keydata,long data) {
		return pref.getLong(keydata,data);
	}
	public void setLong(String keydatam,long data) {
		editor.putLong(keydatam, data);
		editor.commit();
	}	
	public String getString(String keydata,String data) {
		return pref.getString(keydata,data);
	}
	public void setString(String keydatam,String data) {
		editor.putString(keydatam, data);
		editor.commit();
	}
	public boolean getBoolean(String keydata,boolean data) {
		return pref.getBoolean(keydata,data);
	}
	public void setBoolean(String keydatam,boolean data) {
		editor.putBoolean(keydatam, data);
		editor.commit();
	}
	public int getInt(String keydata,int data) {
		return pref.getInt(keydata,data);
	}
	public void setInt(String keydatam,int data) {
		editor.putInt(keydatam, data);
		editor.commit();
	}
	public float getInt(String keydata,float data) {
		return pref.getFloat(keydata,data);
	}
	public void setInt(String keydatam,float data) {
		editor.putFloat(keydatam, data);
		editor.commit();
	}
	public void removePref(String keyData) {
		editor.remove(keyData);
		editor.commit();
	}
	public void removePref() {
		editor.clear();
		editor.commit();
	}
}
