package com.kcert.fido.io;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class JByteArrayStream {
	public static byte[] toByteArray(String tokenId, Long nonce){
		ByteArrayOutputStream baos	= null;
		DataOutputStream dos		= null;
		try{
			baos	= new ByteArrayOutputStream();
			dos		= new DataOutputStream(baos);
			dos.writeUTF(tokenId);
			dos.writeLong(nonce);
			
			return baos.toByteArray();
		}catch(IOException e){
			throw new RuntimeException(e);
		}finally{
			try{if(dos!=null)dos.close();}catch(IOException e){}
			try{if(baos!=null)baos.close();}catch(IOException e){}
		}
	}
}
