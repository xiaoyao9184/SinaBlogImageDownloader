package controller.core;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Image;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.xiaoyao.io.file.PathName;
import com.xiaoyao.web.http.ImageReaderUtil;

import controller.collections.RefreshProducer;
import controller.collections.ThreadQueue;
import controller.threads.ImageLoadThread;
import controller.threads.ResolveThread;
import model.core.Photo;
import model.core.Photo.DownType;
import model.interfaces.IDataStatus.DataStatus;
import model.interfaces.IDataStatus;
import model.interfaces.IDownController;
import model.interfaces.IShowImage;
import model.interfaces.IThreadInfo;
import model.interfaces.IRefreshable;
import model.interfaces.IResolveController;
import model.interfaces.IWidgetController;
import model.setting.Setting;

/**
 * 图片控制器
 * @author xiaoyao9184
 * @version 2.0
 */
public class PhotoController implements IResolveController<Photo>, IDownController<Photo>, IWidgetController {	
	//常量
	private static final Setting SETTING = ContextController.getSetting();
	private static final Logger LOG = Logger.getLogger(PhotoController.class);
	
	private Map<Photo, RefreshProducer> refreshMapping = Collections.synchronizedMap(
			new HashMap<Photo, RefreshProducer>());
	
	private Map<Photo, String> cachePhotoFile = Collections.synchronizedMap(
			new HashMap<Photo, String>());
	private Map<Long, Photo> cacheHash = Collections.synchronizedMap(
			new HashMap<Long, Photo>());
	
	//单例
	private static PhotoController instance;  
	private PhotoController(){
		cacheHash.put((long) 90434323, null);
	}
	public static synchronized PhotoController getInstance() {  
		if (instance == null) {  
			instance = new PhotoController();  
		}  
		return instance;  
	} 
	
	
	/**
	 * 缓存图片
	 * @param id
	 */
	private void addCacheImageFile(Photo photo, String strFile) {
		if(cachePhotoFile.containsKey(photo)){
			cachePhotoFile.remove(photo);
		}

		cachePhotoFile.put(photo, strFile);
	}
	
	/**
	 * 检验重复
	 * @param bytes
	 * @return
	 * @throws IOException 
	 */
	private boolean check(Photo photo, byte[] bytes) throws IOException {
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		CRC32 crc32 = new CRC32();
		CheckedInputStream checkedinputstream = new CheckedInputStream(is, crc32);  
        while (checkedinputstream.read() != -1) {  
        }
        Long v = crc32.getValue();
        LOG.debug("check:CRC32=" + Long.toHexString(v));
        if(cacheHash.containsKey(v)){
        	return false;
        }
        //TODO 对于同一张正常图片也会出现重复
        //cacheHash.put(v, photo);
        return true;
	}
	
/*IWidgetController*/
	
	@Override
	public boolean getChecked(IDataStatus data) {
		return !data.isIgnore();
	}
	@Override
	public void setChecked(IDataStatus data, boolean check) {
		data.setIgnore(!check);
		refresh(data);
	}
	@Override
	public String getText(IDataStatus data) {
		Photo photo = (Photo)data;
		if(photo.getPhotoName().length() > 0){
			return photo.getPhotoName();
		}else{
			return photo.getPhotoID();
		}
	}
	@Override
	public String getLink(IDataStatus data){
		Photo photo = (Photo)data;
		return photo.getPhotoUrl();
	}
	@Override
	public String getLinkText(IDataStatus data) {
		Photo photo = (Photo)data;
		return "图片名称：" + "<a href=\"" + photo.getPhotoUrl() + "\" >" + photo.getPhotoName() + "</a>";
	}
	@Override
	public String getSubCountText(IDataStatus data) {
		if(DataStatus.isResolve(data)){
			Photo photo = (Photo)data;
			return "编号：" + photo.getPhotoID();
		}else{
			return "还未解析";
		}
	}
	@Override
	public String getDataPlace(IDataStatus data) {
		if(cachePhotoFile.containsKey(data)){
			return cachePhotoFile.get(data);
		}else{
			Photo photo = (Photo)data;
			return photo.getPhotoUrl();
		}
	}
	@Override
	public String getDataPlaceText(IDataStatus data) {
		if(DataStatus.isIgnore(data)){
			return "不会占用位置";
		}else if(DataStatus.isDown(data)){
			if(cachePhotoFile.containsKey(data)){
				return cachePhotoFile.get(data);
			}
			return "";
		}else if(DataStatus.isResolve(data)){
			return "内存中";
		}else{
			return "未知";
		}
	}
	@Override
	public void asyncLoadImage(IDataStatus data, IShowImage showImage, boolean isUseCache) {
		Photo photo = (Photo)data;
		
		if(isUseCache){
			//获取已经下载的文件
			if(DataStatus.isDown(photo)){
				String filename = cachePhotoFile.get(photo);
				if(filename != null){
					Image img = new Image(showImage.getDisplay(), filename);
					LOG.debug("asyncLoadImage:Use download cache");
					showImage.showImage(img);
					return;
				}
			}
			//获取缓存的图像
			byte[] bytes = ContextController.getCacheUrl(photo.getPhotoSmallUrl());
			if(bytes != null){
				ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
				Image img = new Image(showImage.getDisplay(), bis);
				LOG.debug("asyncLoadImage:Use URL/image cache");
				showImage.showImage(img);
				return;
			}
		}
		//异步网络加载
		Thread thr = new ImageLoadThread(photo.getPhotoSmallUrl(), showImage);
		thr.start();
	}
	@Override
	public boolean isHaveSub(IDataStatus data) {
		return false;
	}
	@Override
	public ArrayList<IDataStatus> getSubList(IDataStatus data) {
		return null;
	}
	@Override
	public ArrayList<IDataStatus> getSubImageList(IDataStatus data) {
		ArrayList<IDataStatus> list = new ArrayList<>();
		list.add(data);
		return list;
	}
	@Override
	public void addRefreshMapping(IDataStatus data, IRefreshable refreshable) {
		RefreshProducer rp = null;
		if(refreshMapping.containsKey(data)){
			rp = refreshMapping.get(data);
		}else{
			rp = new RefreshProducer();
			refreshMapping.put((Photo)data, rp);
		}
		rp.addRefreshable(refreshable);
	}
	@Override
	public void removeRefreshMapping(IDataStatus data, IRefreshable refreshable) {
		RefreshProducer rp = null;
		if(refreshMapping.containsKey(data)){
			rp = refreshMapping.get(data);
			rp.deleRefreshable(refreshable);
		}
	}
	@Override
	public void refresh(IDataStatus data) {
		RefreshProducer rp = null;
		if(refreshMapping.containsKey(data)){
			rp = refreshMapping.get(data);
			rp.refresh();
		}
	}
	@Override
	public void resetResolve(IDataStatus data) {
		data.setStatus(DataStatus.notresolve);
		refresh(data);
	}
	@Override
	public void resetDown(IDataStatus data) {
		data.setStatus(DataStatus.resolve_ok);
		refresh(data);
	}
	
/*IDownController*/

	@Override
	public boolean isAllDown(Photo photo) {
		if(DataStatus.isIgnore(photo)){
			return true;
		}else if(!DataStatus.isResolve(photo) && SETTING.ignoreDownUnResolve){
			return true;
		}else if(DataStatus.isDown(photo)){
			return true;
		}
		return false;
	}
	
	@Override
	public void download(Photo photo, ThreadQueue tq, String strSavePath) {
		if (photo.isIgnore()){
			//忽略
			LOG.debug("download:Ignore " + photo.getPhotoID());
			return;
		}else if (!DataStatus.isResolve(photo) && SETTING.ignoreDownUnResolve){
			//忽略未解析
			LOG.debug("Down:Ignore unResolved " + photo.getPhotoID());
			return;
		}else{
			resolve(photo);
		}
		
		//忽略已下载
		if (DataStatus.isDown(photo) && SETTING.ignoreDowned){
			LOG.debug("download:Ignore Downed " + photo.getPhotoID());
			return;
		}

		String strUrl = photo.getDownUrl(SETTING.downType);
		if (strUrl==""){
			LOG.debug("download:Ignore URL is Empty by this DownType=" + SETTING.downType.toString());
			return;
		}
		
		LOG.debug("download:" + photo.getPhotoName() + "-" + photo.getPhotoID());
		String strSaveFileName = PathName.removeIllegal(photo.getReName(SETTING.reNameStyle),true);
		String strSavePathFile = strSavePath + strSaveFileName;
		
		OutputStream fos = null;
		try {
			byte[] bytes = ImageReaderUtil.getBytesByConnection(strUrl);
			
			//校验
			if(!check(photo, bytes)){
				LOG.debug("download:下载文件校验失败:"+ photo.getPhotoName() + "-" + photo.getPhotoID() );            
				photo.setDataStatus(DataStatus.down_err);
				return;
			}
			
			String strFile = PathName.getName(strSavePathFile);
			fos = new FileOutputStream(new File(strFile));
			fos.write(bytes);
			LOG.debug("download:下载文件完成:"+ photo.getPhotoName() + "-" + photo.getPhotoID() );            
			photo.setDataStatus(DataStatus.down_ok);
			
			addCacheImageFile(photo, strFile);
		} catch (Exception e) {
			e.printStackTrace();
			photo.setDataStatus(DataStatus.down_err);	
			LOG.error("download:下载失败:" + photo.getPhotoName() + "-" + photo.getPhotoID() + "-" + strUrl);
		}finally{
			try {
				if(fos != null){
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
//		tq.in(new DownLoadThread(
//				photo.getDownUrl(setting.DownType), 
//				strPageBlogPath,
//				photo.getReName(setting.ReNameStyle),
//				photo));
	}

	@Override
	public boolean subDownloadChange(Photo photo) {
		return true;
	}
	
/*IResolveController*/
	
	@Override
	public void asyncResolve(
			ThreadQueue tqResolve, 
			Photo photo, 
			boolean into) {
		IThreadInfo thr = new ResolveThread<Photo>(
				PhotoController.getInstance(), 
				photo, 
				into, 
				tqResolve);
		if(!into &&
				tqResolve.isShutdown()){
			tqResolve.init();
		}
		tqResolve.in(thr);
	}
	
	@Override
	public void resolve(Photo photo){
		if (photo.isIgnore()){
			LOG.debug("resolve:Ignore " + photo.getPhotoID());
		}else if (DataStatus.isResolve(photo) && SETTING.ignoreResolved){
			LOG.debug("resolve:Ignore Resolved " + photo.getPhotoID());
		}else{
			PhotoController.getMiddleBigUrl(photo);
			photo.setDataStatus(DataStatus.resolve_ok);
		}
	}
	
	@Override
	public void resolveInto(Photo photo, ThreadQueue tqResolve){
		LOG.debug("resolveInto:Ignore no sub");
		return;
	}
	
/*Self Use*/
	
	/**
	 * 取得图片的大小图片链接
	 * @param photo
	 */
	private static void getMiddleBigUrl(Photo photo) {
		LOG.debug("getMiddleBigUrl:START");
		LOG.debug("getMiddleBigUrl:" + photo);
		String url = photo.getPhotoUrl();
		if (url == null || url == ""){
			if (photo.getPhotoBigUrl() != null && SETTING.downType == DownType.big ){
				return;
			}else if (photo.getPhotoMiddleUrl() != null && SETTING.downType == DownType.middle){
				return;
			}
			LOG.debug("getMiddleBigUrl:图片URL为空，无法获取大、中、小图片URL");
			photo.setIgnore(true);
			return;
		}
		Document doc = BlogController.openHtml(photo.getPhotoUrl(), 15000);
		//大图:超连接文本
		Elements PhotoOrignalLinkLists = doc.select("a[onclick][href][target=_blank]:contains(原图)"); //带有src属性的img元素
		for (Element PhotoOrignallink : PhotoOrignalLinkLists) {
			photo.setPhotoBigUrl(PhotoOrignallink.attr("href"));
		}
		//中图
		Elements PhotoLinkLists = doc.select("a[href] > img[src][alt]");//[title]
		for (Element Photolink : PhotoLinkLists) {
			if ( Photolink.attr("alt").indexOf("翻页") !=-1 ){
				photo.setPhotoMiddleUrl(Photolink.attr("src"));
			}
		}
		LOG.debug("getMiddleBigUrl:END");
	}
	
}
