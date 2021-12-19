package si.matjazcerkvenik.alertmonitor.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5 {
	
	/**
	 * Return MD5 checksum of a file. If file does not exist, 0 is returned.
	 * @param file
	 * @return checksum
	 */
	public static String getChecksum(File file) {
		
		if (!file.exists()) {
			return "0";
		}
		
		StringBuffer sb = new StringBuffer("");
		
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
			FileInputStream fis = new FileInputStream(file);
		    byte[] dataBytes = new byte[1024];
		 
		    int nread = 0; 
		 
		    while ((nread = fis.read(dataBytes)) != -1) {
		    	md.update(dataBytes, 0, nread);
		    }
		 
		    byte[] mdbytes = md.digest();
		 
		    //convert the byte to hex format
		    for (int i = 0; i < mdbytes.length; i++) {
		    	sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
		    }
		    
		    fis.close();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return "0";
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return "0";
		} catch (IOException e) {
			e.printStackTrace();
			return "0";
		}
		
	    return sb.toString();
	    
	}
	
	/**
	 * Return MD5 checksum of a string.
	 * @param s
	 * @return checksum
	 */
	public static String getChecksum(String s) {
		
		StringBuffer sb = new StringBuffer("");
		
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
		    byte[] dataBytes = s.getBytes();
		    
		    md.update(dataBytes, 0, dataBytes.length);
		 
		    byte[] mdbytes = md.digest();
		 
		    //convert the byte to hex format
		    for (int i = 0; i < mdbytes.length; i++) {
		    	sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
		    }
		    
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
	    return sb.toString();
	    
	}
	
	
}
