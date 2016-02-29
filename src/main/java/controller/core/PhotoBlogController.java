package controller.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.xiaoyao.io.file.FolderNew;
import com.xiaoyao.io.file.PathName;

import controller.collections.RefreshProducer;
import controller.collections.ThreadQueue;
import controller.threads.DownLoadThread;
import controller.threads.ResolveThread;
import model.core.Photo;
import model.core.PhotoBlog;
import model.interfaces.IDataStatus.DataStatus;
import model.interfaces.IDataStatus;
import model.interfaces.IDownController;
import model.interfaces.IRefreshable;
import model.interfaces.IResolveController;
import model.interfaces.IShowImage;
import model.interfaces.IThreadInfo;
import model.interfaces.IWidgetController;
import model.setting.Setting;

/**
 * 博文配图控制器
 * @author xiaoyao9184
 * @version 1.0
 */
public class PhotoBlogController implements IResolveController<PhotoBlog>, IDownController<PhotoBlog>, IWidgetController {
	//常量
	private static final Setting SETTING = ContextController.getSetting();
	private static final Logger LOG = Logger.getLogger(PhotoBlogController.class);
	
	private Map<PhotoBlog, RefreshProducer> refreshMapping = Collections.synchronizedMap(
			new HashMap<PhotoBlog, RefreshProducer>());
	
	private Map<IDataStatus, String> cachePath = Collections.synchronizedMap(
			new HashMap<IDataStatus, String>());
	
	//单例
	private static PhotoBlogController instance;  
	private PhotoBlogController (){}
	public static synchronized PhotoBlogController getInstance() {  
		if (instance == null) {  
			instance = new PhotoBlogController();  
		}  
		return instance;  
	} 


	/**
	 * 缓存
	 * @param id
	 */
	public void addCachePathMapping(IDataStatus data, String strFile) {
		if(cachePath.containsKey(data)){
			cachePath.remove(data);
		}

		cachePath.put(data, strFile);
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
		PhotoBlog blog = (PhotoBlog)data;
		return blog.getBlogName();
	}
	@Override
	public String getLink(IDataStatus data){
		PhotoBlog blog = (PhotoBlog)data;
		return blog.getBlogUrl();
	}
	@Override
	public String getLinkText(IDataStatus data) {
		PhotoBlog blog = (PhotoBlog)data;
		return "博客名称：" + "<a href=\"" + blog.getBlogUrl() + "\" >" + blog.getBlogName() + "</a>";
	}
	@Override
	public String getSubCountText(IDataStatus data) {
		if(DataStatus.isResolve(data)){
			PhotoBlog blog = (PhotoBlog)data;
			return blog.getBlogTime();
			//return blog.getBlogPhotoCount() + "个图片，博客编号：" + blog.getBlogID() + "，描述：" + blog.getBlogTime();
		}else{
			return "还未解析";
		}
	}
	@Override
	public String getDataPlace(IDataStatus data) {
		if(cachePath.containsKey(data)){
			return cachePath.get(data);
		}else{
			PhotoBlog blog = (PhotoBlog)data;
			return blog.getBlogUrl();
		}
	}
	@Override
	public String getDataPlaceText(IDataStatus data) {
		if(DataStatus.isIgnore(data)){
			return "不会占用位置";
		}else if(DataStatus.isDown(data) || data.getStatus() == DataStatus.down_ing){
			if(cachePath.containsKey(data)){
				return cachePath.get(data);
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
		return;
	}
	@Override
	public boolean isHaveSub(IDataStatus data) {
		return true;
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ArrayList<IDataStatus> getSubList(IDataStatus data) {
		PhotoBlog blog = (PhotoBlog)data;
		return (ArrayList)blog.getPhotoList();
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ArrayList<IDataStatus> getSubImageList(IDataStatus data) {
		PhotoBlog blog = (PhotoBlog)data;
		return (ArrayList)blog.getPhotoList();
	}
	@Override
	public void addRefreshMapping(IDataStatus data, IRefreshable refreshable) {
		RefreshProducer rp = null;
		if(refreshMapping.containsKey(data)){
			rp = refreshMapping.get(data);
		}else{
			rp = new RefreshProducer();
			refreshMapping.put((PhotoBlog)data, rp);
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
		PhotoBlog photoBlog = (PhotoBlog)data;
		photoBlog.setPhotoList(null);
		refresh(data);
	}
	@Override
	public void resetDown(IDataStatus data) {
		data.setStatus(DataStatus.resolve_ok);
		refresh(data);
	}
	
/*IDownController*/
	
	@Override
	public boolean isAllDown(PhotoBlog photoBlog) {
		if(DataStatus.isIgnore(photoBlog)){
			return true;
		}else if(!DataStatus.isResolve(photoBlog) && SETTING.ignoreDownUnResolve){
			return true;
		}else if(DataStatus.isDown(photoBlog)){
			return true;
		}else if(photoBlog.getPhotoList() == null){
			return false;
		}
		for (IDataStatus subdata : photoBlog.getPhotoList()) {
			if(!ContextController.getDownController(subdata).isAllDown(subdata)){
				return false;
			}
		}
		return true;
	}
	
	@Override
	public void download(PhotoBlog photoBlog, ThreadQueue tq, String strPagePath) {
		if (photoBlog.isIgnore()){
			//忽略
			LOG.debug("download:Ignore " + photoBlog.getBlogName());
			return;
		}else if (!DataStatus.isResolve(photoBlog) && SETTING.ignoreDownUnResolve){
			//忽略未解析
			LOG.debug("download:Ignore unResolved " + photoBlog.getBlogName());
			return;
		}else{
			resolve(photoBlog);
		}
		
		LOG.debug("download:" + photoBlog.getBlogName());
		String strBlogPath = PathName.getName(strPagePath +
				(SETTING.useBlogNO?photoBlog.getBlogNO() + "-":"") +
				(SETTING.useBlogPath?
						(photoBlog.getBlogName().length()==0?
								"[" + photoBlog.getBlogID() + "]":
								PathName.removeIllegal(photoBlog.getBlogName(), true) 
						)+ "/":
						""));
		FolderNew.createFolder(strBlogPath);
		for (Photo photo : photoBlog.getPhotoList()) {
			IThreadInfo thr = new DownLoadThread<Photo>(
					PhotoController.getInstance(), 
					photo, 
					strBlogPath, 
					tq);
			tq.in(thr);
			//PhotoController.getInstance().download(photo, tq, strBlogPath);
		}
		photoBlog.setDataStatus(DataStatus.down_ing);
		addCachePathMapping(photoBlog, strBlogPath);
	}
	
	@Override
	public boolean subDownloadChange(PhotoBlog photoBlog) {
		DataStatus ds = photoBlog.getDataStatus();
		if(isAllDown(photoBlog)){
			if(ds == DataStatus.down_ok){
				return false;
			}else{
				photoBlog.setStatus(DataStatus.down_ok);
				return true;
			}
		}
		return false;
	}

/*IResolveController*/
	
	@Override
	public void asyncResolve(
			ThreadQueue tqResolve,
			PhotoBlog photoBlog,
			boolean into){
		IThreadInfo thr = new ResolveThread<PhotoBlog>(
				PhotoBlogController.getInstance(), 
				photoBlog, 
				into, 
				tqResolve);
		if(!into &&
				tqResolve.isShutdown()){
			tqResolve.init();
		}
		tqResolve.in(thr);
	}
	
	@Override
	public void resolve(PhotoBlog photoBlog){
		if (photoBlog.isIgnore()){
			LOG.debug("resolve:Ignore " + photoBlog.getBlogName());
		}else if (DataStatus.isResolve(photoBlog) && SETTING.ignoreResolved){
			LOG.debug("resolve:Ignore Resolved " + photoBlog.getBlogName());
		}else{
			photoBlog.setPhotoList(PhotoBlogController.getBlogPhotos(photoBlog));
			photoBlog.setDataStatus(DataStatus.resolve_ok);
		}
	}

	@Override
	public void resolveInto(PhotoBlog photoBlog, ThreadQueue tqResolve){
		//忽略（仅忽略自动解析/手动解析不忽略）
		if (photoBlog.isIgnore()){
			LOG.debug("resolveInto:Ignore " + photoBlog.getBlogName());
			return;
		}
		for(Photo photo : photoBlog.getPhotoList()){
			IThreadInfo thr = new ResolveThread<Photo>(PhotoController.getInstance(), photo, 
					true, tqResolve);
			tqResolve.in(thr);
		}
	}
	
/*Self Use*/
	
	/**
	 * 解析
	 * 取得此博文图片列表
	 * 通过小图img找
	 * @param photoBlog
	 * @return 图片列表
	 */
	private static ArrayList<Photo> getBlogPhotos(PhotoBlog photoBlog) {
		LOG.debug("getBlogPhotos:START");
		LOG.debug("getBlogPhotos:" + photoBlog);
		Photo p = null;
		ArrayList<Photo> result = new ArrayList<Photo>();
		
		Elements PhotoLinkLists = photoBlog.getBlogPhotoElement().select("a[href][target=_blank] > img"); 
		for (Element Photolink : PhotoLinkLists) {
			photoBlog.setBlogPhotoCount(photoBlog.getBlogPhotoCount() + 1);
			LOG.debug("getBlogPhotos:博文名=" + photoBlog.getBlogName() + "-博文编号=" + photoBlog.getBlogNO() + "-博文内图片数=" + photoBlog.getBlogPhotoCount());
			p = new Photo();
			p.setPhotoSmallUrl(Photolink.attr("src"));
			p.setPhotoID(
					p.getPhotoSmallUrl().substring(
							p.getPhotoSmallUrl().lastIndexOf("/")+1,
							p.getPhotoSmallUrl().lastIndexOf("&"))
					);
			p.setPhotoNO(photoBlog.getBlogPhotoCount());
			Photolink = Photolink.parent();
			p.setPhotoName(Photolink.text());
			//判读a元素链接是否为图片链接
			String UrlPhotoIDTemp = Photolink.attr("abs:href");
			UrlPhotoIDTemp = UrlPhotoIDTemp.substring( UrlPhotoIDTemp.lastIndexOf("/")+1 );
			String SmallPhotoIDTemp = p.getPhotoSmallUrl().substring( p.getPhotoSmallUrl().lastIndexOf("/")+1);
			if ( UrlPhotoIDTemp.equals(SmallPhotoIDTemp) ){
				//链接是图片
				LOG.debug("getBlogPhotos:未知Url=" + Photolink.attr("abs:href")); 
				p.setUrl(Photolink.attr("abs:href"));
			}else{
				//链接是图片预览
				p.setPhotoUrl(Photolink.attr("abs:href"));	
			}
			result.add(p);
		}
		LOG.debug("getBlogPhotos:博文名=" + photoBlog.getBlogName() + "-博文编号=" + photoBlog.getBlogNO() + "-博文内图片数=" + photoBlog.getBlogPhotoCount());
		LOG.debug("getBlogPhotos:END");
		return result;
	}
	
}
