package controller.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.xiaoyao.io.file.FolderNew;
import com.xiaoyao.io.file.PathName;

import controller.collections.RefreshProducer;
import controller.collections.ThreadQueue;
import controller.threads.DownLoadThread;
import controller.threads.ResolveThread;

import model.core.Album.AlbumType;
import model.core.Page;
import model.core.Photo;
import model.core.PhotoBlog;
import model.interfaces.IDataStatus;
import model.interfaces.IDownController;
import model.interfaces.IRefreshable;
import model.interfaces.IResolveController;
import model.interfaces.IShowImage;
import model.interfaces.IThreadInfo;
import model.interfaces.IWidgetController;
import model.interfaces.IDataStatus.DataStatus;
import model.setting.Setting;

/**
 * 页控制器
 * @author xiaoyao9184
 * @version 2.0
 */
public class PageController implements IResolveController<Page>, IDownController<Page>, IWidgetController {
	//常量
	private static final Setting SETTING = ContextController.getSetting();
	private static final Logger LOG = Logger.getLogger(PageController.class);
	
	private Map<Page, RefreshProducer> refreshMapping = Collections.synchronizedMap(
			new HashMap<Page, RefreshProducer>());
	
	private Map<IDataStatus, String> cachePath = Collections.synchronizedMap(
			new HashMap<IDataStatus, String>());
	
	//单例
	private static PageController instance;
	private PageController (){}
	public static synchronized PageController getInstance() {
		if (instance == null) {
			instance = new PageController();
		}
		return instance;
	} 
	

	/**
	 * 缓存
	 * @param id
	 */
	private void addCachePathMapping(IDataStatus data, String strFile) {
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
		Page page = (Page)data;
		return "" + page.getPageNO();
	}
	@Override
	public String getLink(IDataStatus data){
		Page page = (Page)data;
		return page.getPageUrl();
	}
	@Override
	public String getLinkText(IDataStatus data) {
		Page page = (Page)data;
		return "页编号：" + "<a href=\"" + page.getPageUrl() + "\" >" + page.getPageNO() + "</a>";
	}
	@Override
	public String getDataPlace(IDataStatus data) {
		if(cachePath.containsKey(data)){
			return cachePath.get(data);
		}else{
			Page page = (Page)data;
			return page.getPageUrl();
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
	public boolean isHaveSub(IDataStatus data) {
		return true;
	}
	@Override
	public String getSubCountText(IDataStatus data) {
		if(DataStatus.isResolve(data)){
			Page page = (Page)data;
			if (page.getPageType() == AlbumType.ORDINARY ){
				return page.getPagePhotoCount() + "个图片";
			}else{
				return page.getPagePhotoBlogCount() + "个博客";
			}
		}else{
			return "还未解析";
		}
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ArrayList<IDataStatus> getSubList(IDataStatus data) {
		Page page = (Page)data;
		if (page.getPageType() == AlbumType.ORDINARY ){
			return (ArrayList)page.getPhotoList();
		}else{
			return (ArrayList)page.getPhotoBlogList();
		}
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ArrayList<IDataStatus> getSubImageList(IDataStatus data) {
		Page page = (Page)data;
		if (page.getPageType() == AlbumType.ORDINARY ){
			return (ArrayList)page.getPhotoList();
		}else{
			//不显示博文
			return null;
		}
	}
	@Override
	public void asyncLoadImage(IDataStatus data, IShowImage showImage, boolean isUseCache) {
		return;
	}
	@Override
	public void addRefreshMapping(IDataStatus data, IRefreshable refreshable) {
		RefreshProducer rp = null;
		if(refreshMapping.containsKey(data)){
			rp = refreshMapping.get(data);
		}else{
			rp = new RefreshProducer();
			refreshMapping.put((Page)data, rp);
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
		Page page = (Page)data;
		page.setPhotoList(null);
		page.setPhotoBlogList(null);
		refresh(data);
	}
	@Override
	public void resetDown(IDataStatus data) {
		data.setStatus(DataStatus.resolve_ok);
		refresh(data);
	}
	
/*IDownController*/
	
	@Override
	public boolean isAllDown(Page page) {
		if(DataStatus.isIgnore(page)){
			return true;
		}else if(!DataStatus.isResolve(page) && SETTING.ignoreDownUnResolve){
			return true;
		}else if(DataStatus.isDown(page)){
			return true;
		}
		if (page.getPageType() == AlbumType.ORDINARY){
			if(page.getPhotoList() == null){
				return false;
			}
			for (IDataStatus subdata : page.getPhotoList()) {
				if(DataStatus.isIgnore(subdata)){
					
				}else if(!DataStatus.isResolve(subdata) && SETTING.ignoreDownUnResolve){
					
				}else if(!ContextController.getDownController(subdata).isAllDown(subdata)){
					return false;
				}
			}
			return true;
		}else{
			if(page.getPhotoBlogList() == null){
				return false;
			}
			for (IDataStatus subdata : page.getPhotoBlogList()) {
				if(DataStatus.isIgnore(subdata)){
					
				}else if(!DataStatus.isResolve(subdata) && SETTING.ignoreDownUnResolve){
					
				}else if(!ContextController.getDownController(subdata).isAllDown(subdata)){
					return false;
				}
			}
			return true;
		}
	}
	
	@Override
	public void download(Page page, ThreadQueue tq, String strAlbumPath) {
		if (page.isIgnore()){
			//忽略
			LOG.debug("download:Ignore " + page.getPageNO());
			return;
		}else if (!DataStatus.isResolve(page) && SETTING.ignoreDownUnResolve){
			//忽略未解析
			LOG.debug("download:Ignore unResolved " + page.getPageNO());
			return;
		}else{
			resolve(page);
		}
		
		LOG.debug("download:" + page.getPageNO());
		String strPagePath = PathName.getName(strAlbumPath +
				(SETTING.usePagePath?
						page.getPageNO() +"/":
						(SETTING.usePageNO?
								page.getPageNO() + "-":
								"")
				));
		FolderNew.createFolder(strPagePath);
		if (page.getPageType() == AlbumType.BLOG_PIC_LIST){
			for (PhotoBlog blog : page.getPhotoBlogList()) {
				IThreadInfo thr = new DownLoadThread<PhotoBlog>(
						PhotoBlogController.getInstance(), 
						blog, 
						strPagePath, 
						tq);
				tq.in(thr);
				//PhotoBlogController.getInstance().download(blog, tq, strPagePath);
			}
		}else{
			for (Photo photo : page.getPhotoList()) {
				IThreadInfo thr = new DownLoadThread<Photo>(
						PhotoController.getInstance(), 
						photo, 
						strPagePath, 
						tq);
				tq.in(thr);
				//PhotoController.getInstance().download(photo, tq, strPagePath);
			}
		}
		page.setDataStatus(DataStatus.down_ing);
		addCachePathMapping(page, strPagePath);
	}
	
	@Override
	public boolean subDownloadChange(Page page) {
		DataStatus ds = page.getDataStatus();
		if(isAllDown(page)){
			if(ds == DataStatus.down_ok){
				return false;
			}else{
				page.setStatus(DataStatus.down_ok);
				return true;
			}
		}
		return false;
	}
	
/*IResolveController*/

	@Override
	public void asyncResolve(
			ThreadQueue tqResolve,
			Page page,
			boolean into){
		IThreadInfo thr = new ResolveThread<Page>(
				PageController.getInstance(), 
				page, 
				into, 
				tqResolve);
		if(!into &&
				tqResolve.isShutdown()){
			tqResolve.init();
		}
		tqResolve.in(thr);
	}
	@Override	
	public void resolve(Page page) {
		if (page.isIgnore()){
			LOG.debug("resolve:Ignore " + page.getPageNO());
		}else if (DataStatus.isResolve(page) && SETTING.ignoreResolved){
			LOG.debug("resolve:Ignore Resolved " + page.getPageNO());		
		}else{
			Document doc = BlogController.openHtml(page.getPageUrl(), 10000);
			if ( page.getPageType() == AlbumType.BLOG_PIC_LIST){
				page.setPhotoBlogList(PageController.getPageBlogs(page, doc));
			}else{
				page.setPhotoList(PageController.getPagePhotos(page, doc));
			}
			page.setDataStatus(DataStatus.resolve_ok);
		}
	}
	
	@Override
	public void resolveInto(Page page, ThreadQueue tqResolve){
		//忽略（仅忽略自动解析/手动解析不忽略）
		if (page.isIgnore()){
			LOG.debug("resolveInto:Ignore " + page.getPageNO());
			return;
		}
		if (page.getPageType() == AlbumType.ORDINARY){
			for(Photo photo : page.getPhotoList()){
				IThreadInfo thr = new ResolveThread<Photo>(
						PhotoController.getInstance(), 
						photo, 
						true, 
						tqResolve);
				tqResolve.in(thr);
			}
		}else{
			for(PhotoBlog photoBlog : page.getPhotoBlogList()){
				IThreadInfo thr = new ResolveThread<PhotoBlog>(
						PhotoBlogController.getInstance(), 
						photoBlog, 
						true, 
						tqResolve);
				tqResolve.in(thr);
			}
		}
	}
	
/*Self Use*/
	
	/**
	 * 解析
	 * 解析页面中的图片
	 * @param doc 页面HTML
	 */
	private static ArrayList<Photo> getPagePhotos(Page page, Document d){
		LOG.debug("getPagePhotos:START");
		LOG.debug("getPagePhotos:" + page);
		Document doc = d;
		
		Photo photoTemp = null;
		ArrayList<Photo> result = new ArrayList<Photo>();
		
		Elements PhotoTitleLinkLists = doc.select("a[href][title][target][isvalue=1]"); //带有...属性的a元素
		for (Element PhotoTitlelink : PhotoTitleLinkLists) {
			page.setPagePhotoCount(page.getPagePhotoCount() + 1);
			LOG.debug("getPagePhotos:页编号=" + page.getPageNO() + "-页内图片数=" + page.getPagePhotoCount());	
			//图片信息
			photoTemp = new Photo();
			photoTemp.setPhotoName(PhotoTitlelink.attr("title"));
			photoTemp.setPhotoUrl(PhotoTitlelink.attr("abs:href"));			
			Elements es = doc.select("a[href=" + PhotoTitlelink.attr("abs:href") + "][target=_blank] > img");
			for (Element e : es) {
				photoTemp.setPhotoSmallUrl(e.attr("src"));
			}
			photoTemp.setPhotoID(
					photoTemp.getPhotoSmallUrl().substring(
							photoTemp.getPhotoSmallUrl().lastIndexOf("/")+1, 
							photoTemp.getPhotoSmallUrl().lastIndexOf("&"))
					);
			photoTemp.setPhotoNO(page.getPagePhotoCount());
			result.add(photoTemp);
		}
		LOG.debug("getPagePhotos:页编号=" + page.getPageNO() + "-页内图片数=" + page.getPagePhotoCount());
		LOG.debug("getPagePhotos:END");
		return result;
	}
	
	/**
	 * 解析
	 * @param doc 页面HTML
	 * @return 博文列表
	 */
	private static ArrayList<PhotoBlog> getPageBlogs(Page page, Document d){
		LOG.debug("getPageBlogs:START");
		LOG.debug("getPagePhotos:" + page);
		Document doc = d;

		PhotoBlog blog = null;
		ArrayList<PhotoBlog> result = new ArrayList<PhotoBlog>();
		
		Element NextEM = null;
		Elements BlogLinkLists = doc.select("div[class=pt_title_sub SG_txta] > a[href][target=_blank]"); 
		for (Element Bloglink : BlogLinkLists) {
			page.setPagePhotoBlogCount(page.getPagePhotoBlogCount() + 1);
			LOG.debug("getPageBlogs:页编号=" + page.getPageNO() + "-页内博客数=" + page.getPagePhotoBlogCount());
			//Blog信息
			blog = new PhotoBlog();
			blog.setBlogName(Bloglink.text());
			blog.setBlogUrl(Bloglink.attr("abs:href"));
			blog.setBlogID(
					blog.getBlogUrl().substring(
							blog.getBlogUrl().lastIndexOf("blog_") + 1 ,
							blog.getBlogUrl().lastIndexOf("."))
					);
			blog.setBlogNO(page.getPagePhotoBlogCount());
			//图片数量和日期
			NextEM = Bloglink.nextElementSibling();
			blog.setBlogTime(NextEM.text());
			//博文下图片html
			blog.setBlogPhotoElement(Bloglink.parent().nextElementSibling());
			result.add(blog);
		}
		LOG.debug("getPageBlogs:页编号=" + page.getPageNO() + "-页内博客数=" + page.getPagePhotoBlogCount());
		LOG.debug("getPageBlogs:END");
		return result;
	}
	
}