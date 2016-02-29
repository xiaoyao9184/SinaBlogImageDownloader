package com.xiaoyao.io.file;

import java.io.File;
import java.io.IOException;

/**
 * 路径相同
 * @author xiaoyao9184
 * @version 1.2 2014-05-05
 * 			Fix:修复1.1 2次replace都是对name进行操作
 * @version 1.3 2014-05-18
 * 			+:
 */
public class PathName {
	
	/**
	 * 返回绝对路径
	 * @param name
	 * @return
	 * @throws IOException
	 */
	public static String getRealName(String name) throws IOException{
		return new File(name).getCanonicalPath();
	}
	
	/**
	 * 统一路径分隔符为系统分隔符
	 * @param name
	 * @return
	 */
	public static String getName(String name){
		String n = name;
		if (File.separator.equals("/")){
			n = name.replace("\\", "/");	//
			n = n.replace("//", "/");		//1.1去掉重复的;1.2替换name为n
		}else if(File.separator.equals("\\")){
			n = name.replace("/", "\\");	
			n = n.replace("\\\\", "\\");	//1.1去掉重复的;1.2替换name为n
		}
		return n;
	}
	
	/**
	 * 判断是否相同
	 * @param path1
	 * @param path2
	 * @return
	 */
	public static boolean isSame(String path1, String path2){
		String n1 = getName(path1);
		String n2 = getName(path2);
		
		return n1.equals(n2);
	}
	
	/**
	 * 剔除文件名不支持的字符(文件名)
	 * @version 1.4
	 * @param name
	 * @param useUnderline
	 * @return
	 */
	public static String removeIllegal(String name,boolean useUnderline){
		String replace = useUnderline?"_":"";
		String n = name;
		
		//Windows cannot support \/:*?"<>|
		//And end by . will ignore
		n = n.replaceAll("\\\\", replace);
		n = n.replaceAll("/", replace);
		n = n.replaceAll("\\:", replace);
		n = n.replaceAll("\\*", replace);
		n = n.replaceAll("\\?", replace);
		n = n.replaceAll("\"", replace);
		n = n.replaceAll("<", replace);
		n = n.replaceAll(">", replace);
		n = n.replaceAll("\\|", replace);
		
		n = n.replaceAll("\\.+$", "");//\.+$
		return n;
	}
}
