package com.xiaoyao.io.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.Date;

/**
 * 文件复制
 * @author xiaoyao9184
 * @version 1.0
 */
public class FileCopy {

	/**
	 * 复制文件(纯JAVA)
	 * @param f1
	 * @param f2
	 * @return
	 * @throws Exception
	 */
	public static long forJava(File f1,File f2) throws Exception{
		long time=new Date().getTime();
		int length=2097152;
		FileInputStream in=new FileInputStream(f1);
		FileOutputStream out=new FileOutputStream(f2);
		byte[] buffer=new byte[length];
		while(true){
			int ins=in.read(buffer);
			if(ins==-1){
			in.close();
			out.flush();
			out.close();
			return new Date().getTime()-time;
	   }else
		   out.write(buffer,0,ins);
	   }
	}
	
	/**
	 * 复制文件()
	 * @param f1
	 * @param f2
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("resource")
	public static long forTransfer(File f1,File f2) throws Exception{
		long time=new Date().getTime();
		int length=2097152;
		FileInputStream in=new FileInputStream(f1);
		FileOutputStream out=new FileOutputStream(f2);
		FileChannel inC=in.getChannel();
		FileChannel outC=out.getChannel();
		//int i=0;
		while(true){
		    if(inC.position()==inC.size()){
		        inC.close();
		        outC.close();
		        return new Date().getTime()-time;
		    }
		    if((inC.size()-inC.position())<20971520)
		        length=(int)(inC.size()-inC.position());
		    else
		        length=20971520;
		    inC.transferTo(inC.position(),length,outC);
		    inC.position(inC.position()+length);
		    //i++;
		}
	}
	/**
	 * 复制文件()
	 * @param f1
	 * @param f2
	 * @return
	 * @throws Exception
	 */
    @SuppressWarnings("resource")
	public static long forImage(File f1,File f2) throws Exception{
        long time=new Date().getTime();
        int length=2097152;
        FileInputStream in=new FileInputStream(f1);
        RandomAccessFile out=new RandomAccessFile(f2,"rw");
        FileChannel inC=in.getChannel();
        MappedByteBuffer outC=null;
        MappedByteBuffer inbuffer=null;
        byte[] b=new byte[length];
        while(true){
            if(inC.position()==inC.size()){
                inC.close();
                outC.force();
                out.close();
                return new Date().getTime()-time;
            }
            if((inC.size()-inC.position())<length){
                length=(int)(inC.size()-inC.position());
            }else{
                length=20971520;
            }
            b=new byte[length];
            inbuffer=inC.map(MapMode.READ_ONLY,inC.position(),length);
            inbuffer.load();
            inbuffer.get(b);
            outC=out.getChannel().map(MapMode.READ_WRITE,inC.position(),length);
            inC.position(b.length+inC.position());
            outC.put(b);
            outC.force();
        }
    }
	/**
	 * 复制文件()
	 * @param f1
	 * @param f2
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("resource")
	public static long forChannel(File f1,File f2) throws Exception{
        long time=new Date().getTime();
        int length=2097152;
        FileInputStream in=new FileInputStream(f1);
        FileOutputStream out=new FileOutputStream(f2);
        FileChannel inC=in.getChannel();
        FileChannel outC=out.getChannel();
        ByteBuffer b=null;
        while(true){
            if(inC.position()==inC.size()){
                inC.close();
                outC.close();
                return new Date().getTime()-time;
            }
            if((inC.size()-inC.position())<length){
                length=(int)(inC.size()-inC.position());
            }else
                length=2097152;
            b=ByteBuffer.allocateDirect(length);
            inC.read(b);
            b.flip();
            outC.write(b);
            outC.force(false);
        }
    }
}
