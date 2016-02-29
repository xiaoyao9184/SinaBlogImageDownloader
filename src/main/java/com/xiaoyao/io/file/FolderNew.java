package com.xiaoyao.io.file;

import java.io.File;

/**
 * 文件夹创建
 * @author xiaoyao9184
 * @version 1.0
 */
public class FolderNew {
	
	/**
	 * 创建文件夹
	 * @param strFolderPath
	 * @return
	 */
	public static boolean createFolder(String strFolderPath){
		File f = new File(strFolderPath);
		if ( !( f.exists() && f.isDirectory()) ){
			return f.mkdirs();
		}
		return true;
	}
	
}
