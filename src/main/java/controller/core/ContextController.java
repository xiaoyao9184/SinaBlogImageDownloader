package controller.core;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.eclipse.swt.program.Program;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.xiaoyao.io.file.FileCopy;

import controller.collections.ThreadQueue;
import controller.threads.DownLoadThread;
import controller.threads.ResolveThread;
import model.core.Album;
import model.core.Blog;
import model.core.Page;
import model.core.Photo;
import model.core.PhotoBlog;
import model.interfaces.IDataStatus;
import model.interfaces.IDownController;
import model.interfaces.IResolveController;
import model.interfaces.IShowStatusable;
import model.interfaces.IThreadInfo;
import model.interfaces.IWidgetController;
import model.setting.Setting;
import view.FrmMain;

public class ContextController {
	//常量
	@SuppressWarnings("unused")
	private static final String TAG = "ContextController";
	private static final String RES_SETTING = "setting/cfg.json";
	private static final String LOG_NAME = "run.log";
	private static final String LOG_PATTERN = "%d{yyyy-MM-dd HH:mm:ss} | [%t] {%c} %p - %m %n";
	
	private static Logger logger = Logger.getRootLogger();//.getLogger(ContextController.class);
	
	private static Map<Class<?>, IWidgetController> map = null;

	private static Map<String, byte[]> cacheUrl = null;
	
	private static Setting setting = null;
	
	static{
		//cfg
		setting = readSetting();
		
		//data
		map = new HashMap<>();
		map.put(Blog.class, BlogController.getInstance());
		map.put(Album.class, AlbumController.getInstance());
		map.put(Page.class, PageController.getInstance());
		map.put(PhotoBlog.class, PhotoBlogController.getInstance());
		map.put(Photo.class, PhotoController.getInstance());
		
		cacheUrl = Collections.synchronizedMap(new HashMap<String, byte[]>());
		
		//log
        PatternLayout layout = new PatternLayout(LOG_PATTERN);  
        
        ConsoleAppender consoleAppender = new ConsoleAppender(layout);
        FileAppender fileAppender = null;
        try {
        	String strFile = ContextController.class.
					getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
			strFile = strFile.substring(0, strFile.lastIndexOf('/')+1) + LOG_NAME;
			fileAppender = new FileAppender(layout, strFile, true);
			fileAppender.setEncoding("UTF-8");
			logger.addAppender(fileAppender);
		} catch (Exception e) {
			e.printStackTrace();
		}
        logger.addAppender(consoleAppender);
        if(setting.debug){
            logger.setLevel(Level.DEBUG);
        }else{
        	logger.setLevel(Level.ERROR);
        }
	}
	
	/**
	 * 获取设置
	 * @return
	 */
	public static Setting getSetting(){
		return setting;
	}

	/**
	 * 获取解析队列
	 * @return
	 */
	public static ThreadQueue getResolveQueue() {
		return FrmMain.getInstance().getResolveQueue();
	}
	
	/**
	 * 获取下载队列
	 * @return
	 */
	public static ThreadQueue getDownQueue() {
		return FrmMain.getInstance().getDownQueue();
	}
	
	/**
	 * 获取默认的状态显示控件
	 * @return
	 */
	public static IShowStatusable getDefaultIShowStatusable(){
		return FrmMain.getInstance();
	}
	
	/**
	 * 获取缓存
	 * @param url
	 * @return
	 */
	public static byte[] getCacheUrl(String url){
		if(cacheUrl.containsKey(url)){
			return cacheUrl.get(url);
		}
		return null;
	}

	/**
	 * 设置缓存
	 * @param url
	 * @param data 可以为NULL
	 */
	public static void setCacheUrl(String url, byte[] data){
		if(cacheUrl.containsKey(url)){
			cacheUrl.remove(url);
		}
		if(data != null){
			cacheUrl.put(url, data);
		}
	}

	/**
	 * 显示提示
	 * @param msg
	 */
	public static void showTip(String msg){
		FrmMain.getInstance().showTip(msg);
	}
	
	/**
	 * 读取设置反序列化
	 * @return 
	 */
	public static Setting readSetting(){
		Setting setting = null;
		try {
			String strFile = ContextController.class.
					getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
			strFile = strFile.substring(0, strFile.lastIndexOf('/')+1) + "cfg.json";
			File f = new File(strFile);
			if(!f.exists()){
				ClassLoader classLoader = ContextController.class.getClassLoader();
				Path temp = Files.createTempFile("cfg-", ".json");
				Files.copy(classLoader.getResourceAsStream(RES_SETTING), temp, StandardCopyOption.REPLACE_EXISTING);
				FileCopy.forTransfer(temp.toFile(), f);
			}
			
			FileReader fr = new FileReader(f);
			Gson gson = new Gson();
			setting = gson.fromJson(fr, Setting.class);
			fr.close();
			return setting;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return setting;
	}
	
	/**
	 * 保存设置
	 * @return
	 */
	public static void saveSetting(){
		try {
			String strFile = ContextController.class.
					getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
			strFile = strFile.substring(0, strFile.lastIndexOf('/')+1) + "cfg.json";
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			String json = gson.toJson(setting);
			FileWriter fw = new FileWriter(strFile);
			fw.write(json);
			fw.close();
        } catch (Exception ex){   
            ex.printStackTrace();
			logger.error("保存设置时出错！", ex);
        }  
	}
	
	/**
	 * 打开URL
	 * @param url
	 */
	public static void open(String url) {
		Program.launch(url);
	}

	/**
	 * 打开数据(下载的打开文件)
	 * @param data
	 */
	public static void open(IDataStatus data) {
		String url = getWidgetController(data).getDataPlace(data);
		Program.launch(url);
	}

	/**
	 * 根据数据获取对应的界面控制器
	 * @param data
	 * @return
	 */
	public static IWidgetController getWidgetController(IDataStatus data){
		return map.get(data.getClass());
	}
	
	/**
	 * 根据数据获取对应的解析控制器
	 * @param data
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static IResolveController<IDataStatus> getResolveController(IDataStatus data){
		IWidgetController con = getWidgetController(data);
		if(con instanceof IResolveController<?>){
			return (IResolveController<IDataStatus>)con;
		}
		return null;
	}
	
	/**
	 * 根据数据获取对应的下载控制器
	 * @param data
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static IDownController<IDataStatus> getDownController(IDataStatus data){
		IWidgetController con = getWidgetController(data);
		if(con instanceof IDownController<?>){
			return (IDownController<IDataStatus>)con;
		}
		return null;
	}
	
	/**
	 * 异步解析
	 * @param data
	 */
	public static void asyncResolve(IDataStatus data){
		boolean intoFlag = false;
		ThreadQueue resolveThreadQueue = getResolveQueue();
		IThreadInfo thr = new ResolveThread<IDataStatus>(
				getResolveController(data), 
				data, 
				intoFlag, 
				resolveThreadQueue);
		if(!intoFlag &&
				resolveThreadQueue.isShutdown()){
			resolveThreadQueue.init();
		}
		resolveThreadQueue.in(thr);
	}
	
	/**
	 * 异步解析(全部)
	 * @param data
	 */
	public static void asyncResolveAll(IDataStatus data){
		boolean intoFlag = true;
		ThreadQueue resolveThreadQueue = getResolveQueue();
		IThreadInfo thr = new ResolveThread<IDataStatus>(
				getResolveController(data), 
				data, 
				intoFlag, 
				resolveThreadQueue);
		if(!intoFlag &&
				resolveThreadQueue.isShutdown()){
			resolveThreadQueue.init();
		}
		resolveThreadQueue.in(thr);
	}

	/**
	 * 异步下载(全部)
	 * @param data
	 * @param basePath
	 */
	public static void asyncDownAll(IDataStatus data, String basePath) {
		boolean intoFlag = true;
		ThreadQueue downThreadQueue = getDownQueue();
		IThreadInfo thr = new DownLoadThread<IDataStatus>(
				getDownController(data), 
				data, 
				basePath, 
				downThreadQueue);
		if(!intoFlag &&
				downThreadQueue.isShutdown()){
			downThreadQueue.init();
		}
		downThreadQueue.in(thr);
	}

}
