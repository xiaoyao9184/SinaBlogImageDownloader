package com.xiaoyao.web.http;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageReaderUtil {
	/**
     * @param imageUrl
     * @return byte array of the file
     */
    public static byte[] getBytesByConnection(String imageUrl) throws Exception {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         BufferedInputStream bis = null;
         HttpURLConnection urlconnection = null;
         URL url = null;
         byte[] buf = new byte[1024];
         try {
             url = new URL(imageUrl);
             urlconnection = (HttpURLConnection) url.openConnection();
             urlconnection.connect();
             bis = new BufferedInputStream(urlconnection.getInputStream());
//System.out.println("getBytesByConnection:Url=" + url);
//System.out.println("getBytesByConnection:urlconnection=" + urlconnection.getContentLength());
//System.out.println("getBytesByConnection:bis=" + bis.toString());
             for (int len = 0; (len = bis.read(buf)) != -1;){
                  baos.write(buf,0,len);
             }
             return baos.toByteArray();
         } catch(Exception ex){
        	 throw ex;
         } finally {
             try {
                  urlconnection.disconnect();
                  bis.close();
             } catch (IOException ex) {
            	 throw ex;
             }
         }
    }
    
    
    public static byte[] getBytesByStream(String imageUrl) throws Exception {
    	URL url = new URL(imageUrl);
    	DataInputStream dis = new DataInputStream(url.openStream());
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	byte[] buff = new byte[1024];
    	try {
	    	int len = -1;
	    	while((len = dis.read(buff))!=-1){
	    		baos.write(buff, 0, len);
		    }
		    
		    return baos.toByteArray();
    	} catch(Exception ex){
       		throw ex;
    	}finally{
    		buff = null;
    		baos.close();
    	    dis.close();
    	}
    }
}
