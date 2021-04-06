package com.kcert.fido.parent;

import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Signature;

import android.annotation.SuppressLint;
import android.content.Context;
import android.security.keystore.KeyProperties;

public class FIDOKeyManager {
	private static FIDOKeyManager instance;
	
	private FIDOKeyManager(Context context){
	}
	
	public static FIDOKeyManager getInstance(Context context){
		synchronized(FIDOKeyManager.class){
			if(instance==null){
				instance	= new FIDOKeyManager(context);
			}
		}
		return instance;
	}
	
	@SuppressLint({ "InlinedApi", "TrulyRandom" })
	public KeyPairGenerator getKeyPairGenerator() {
        try {
            return KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException("KeyPairGenerator 객체 생성 실패", e);
        }
    }
	
	public KeyStore getKeystore() {
        try {
            return KeyStore.getInstance("AndroidKeyStore");
        } catch (KeyStoreException e) {
            throw new RuntimeException("KeyStore 객체 생성 실패", e);
        }
    }
	
	public Signature getSignature(KeyStore keyStore) {
		try {
			return Signature.getInstance(FIDOProperties.ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Signature 객체 생성 실패", e);
		}
	}
}
