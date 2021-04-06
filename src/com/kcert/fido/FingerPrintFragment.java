package com.kcert.fido;


import com.kcert.fido.FingerprintUiHelper.FingerprintUiHelperBuilder;
import com.kcert.fido.fingerprint.FingerPrintSupport;

import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

@SuppressLint({ "InlinedApi", "NewApi" })
public class FingerPrintFragment extends DialogFragment implements TextView.OnEditorActionListener, FingerprintUiHelper.Callback {
	private MainActivity mActivity;

	private FingerPrintSupport support;
	private FingerprintUiHelper helper;
	private FingerprintUiHelper.FingerprintUiHelperBuilder builder;

	public FingerPrintFragment(){
	}
	
	public FingerPrintFragment(FingerPrintSupport support){
		this.support	= support;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setRetainInstance(true);
		setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog);
		 
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v	= inflater.inflate(R.layout.fingerprint_dialog_container, container, false);
		
		builder	= new FingerprintUiHelperBuilder(support.getFingerprintManager());
		helper	= builder.build((ImageView) v.findViewById(R.id.fingerprint_icon)
				, (TextView) v.findViewById(R.id.fingerprint_status)
				, this);
		
		return v;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		helper.startListening(new FingerprintManager.CryptoObject(support.getSignature()));
	}
	
	@Override
	public void onPause() {
		super.onPause();
		helper.stopListening();
	}
	
	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		mActivity	= (MainActivity) getActivity();
	}
	
	@Override
	public void onAuthenticated() {
		boolean yn	= support.clientAuthenticated();
		mActivity.onPurchased(yn);
		dismiss();
	}

	@Override
	public void onError() {
		
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		return false;
	}
}
