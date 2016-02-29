package com.xiaoyao.io.stream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * StreamUtil
 * @author xiaoyao9184
 * @version 2.0
 * @version 1.0 2014-06-16
 */
public class StreamUtil {
	/**
     * 流转字节数组
     * @param is
     * @return
     * @throws IOException
     */
	public static byte[] toByte(InputStream is) throws IOException {
		ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
		int ch;
		while ((ch = is.read()) != -1) {
			bytestream.write(ch);
		}
		byte imgdata[] = bytestream.toByteArray();
		bytestream.close();
		return imgdata;
	}
}
