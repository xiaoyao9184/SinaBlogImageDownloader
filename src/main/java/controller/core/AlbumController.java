package controller.core;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Image;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.xiaoyao.io.file.FolderNew;
import com.xiaoyao.io.file.PathName;

import controller.collections.RefreshProducer;
import controller.collections.ThreadQueue;
import controller.threads.DownLoadThread;
import controller.threads.ImageLoadThread;
import controller.threads.ResolveThread;

import model.core.Album;
import model.core.Page;
import model.interfaces.IDataStatus;
import model.interfaces.IDataStatus.DataStatus;
import model.interfaces.IRefreshable;
import model.interfaces.IShowImage;
import model.interfaces.IThreadInfo;
import model.interfaces.IResolveController;
import model.interfaces.IDownController;
import model.interfaces.IWidgetController;
import model.setting.Setting;

/**
 * 专辑控制器
 * @author xiaoyao9184
 * @version 2.0
 */
public class AlbumController implements IResolveController<Album>, IDownController<Album>, IWidgetController {
	//常量
	private static final Setting SETTING = ContextController.getSetting();
	private static final Logger LOG = Logger.getLogger(AlbumController.class);
	
	private Map<Album, RefreshProducer> refreshMapping = Collections.synchronizedMap(
			new HashMap<Album, RefreshProducer>());
	
	private Map<IDataStatus, String> cachePath = Collections.synchronizedMap(
			new HashMap<IDataStatus, String>());
	
	//单例
	private static AlbumController instance;
	private AlbumController (){}
	public static synchronized AlbumController getInstance() {
		if (instance == null) {
			instance = new AlbumController();
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
		Album album = (Album)data;
		return album.getAlbumName();
	}
	@Override
	public String getLink(IDataStatus data){
		Album album = (Album)data;
		return album.getAlbumUrl();
	}
	@Override
	public String getLinkText(IDataStatus data) {
		Album album = (Album)data;
		return "相册名称：" + "<a href=\"" + album.getAlbumUrl() + "\" >" + album.getAlbumName() + "</a>";
	}
	@Override
	public String getSubCountText(IDataStatus data) {
		if(DataStatus.isResolve(data)){
			Album album = (Album)data;
			return album.getAlbumPhotoCount() + "个图片，" + 
					album.getAlbumPageCount() + "页";
		}else{
			Album album = (Album)data;
			return album.getAlbumPhotoCount() + "个图片，" + 
				"还未解析";
		}
	}
	@Override
	public String getDataPlace(IDataStatus data) {
		if(cachePath.containsKey(data)){
			return cachePath.get(data);
		}else{
			Album album = (Album)data;
			return album.getAlbumUrl();
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
		Album album = (Album)data;
		if(isUseCache){
			//获取缓存的图像
			byte[] bytes = ContextController.getCacheUrl(album.getAlbumImageUrl());
			if(bytes != null){
				ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
				Image img = new Image(showImage.getDisplay(), bis);
				showImage.showImage(img);
				return;
			}
		}
		//异步网络加载
		Thread thr = new ImageLoadThread(album.getAlbumImageUrl(), showImage);
		thr.start();
	}
	@Override
	public boolean isHaveSub(IDataStatus data) {
		Album album = (Album)data;
		return album.getAlbumPhotoCount() > 0;
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ArrayList<IDataStatus> getSubList(IDataStatus data) {
		Album album = (Album)data;
		return (ArrayList)album.getAlbumPageList();
	}
	@Override
	public ArrayList<IDataStatus> getSubImageList(IDataStatus data) {
		//return getSubList(data);
		//不显示页
		return null;
	}
	@Override
	public void addRefreshMapping(IDataStatus data, IRefreshable refreshable) {
		RefreshProducer rp = null;
		if(refreshMapping.containsKey(data)){
			rp = refreshMapping.get(data);
		}else{
			rp = new RefreshProducer();
			refreshMapping.put((Album)data, rp);
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
		Album album = (Album)data;
		album.setAlbumPageList(null);
		refresh(data);
	}
	@Override
	public void resetDown(IDataStatus data) {
		data.setStatus(DataStatus.resolve_ok);
		refresh(data);
	}
	
/*IDownController*/
	
	@Override
	public boolean isAllDown(Album album) {
		if(DataStatus.isIgnore(album)){
			return true;
		}else if(!DataStatus.isResolve(album) && SETTING.ignoreDownUnResolve){
			return true;
		}else if(DataStatus.isDown(album)){
			return true;
		}else if(album.getAlbumPageList() == null){
			return false;
		}
		for (IDataStatus subdata : album.getAlbumPageList()) {
			if(!ContextController.getDownController(subdata).isAllDown(subdata)){
				return false;
			}
		}
		return true;
	}
	
	@Override
	public void download(Album album, ThreadQueue tq, String strBlogPath) {
		if (album.isIgnore()){
			//忽略
			LOG.debug("download:Ignore " + album.getAlbumName());
			return;
		}else if (!DataStatus.isResolve(album) && SETTING.ignoreDownUnResolve){
			//忽略未解析
			LOG.debug("download:Ignore unResolved " + album.getAlbumName());
			return;
		}else{
			resolve(album);
		}
				
		LOG.debug("download:" + album.getAlbumName());
		String strAlbumPath = PathName.getName(strBlogPath + 
				(SETTING.useAlubmPath?
						PathName.removeIllegal(album.getAlbumName(), true) + "/":
						"")
				);
		FolderNew.createFolder(strAlbumPath);
		for (Page page : album.getAlbumPageList()) {
			DownLoadThread<Page> thr = new DownLoadThread<Page>(
					PageController.getInstance(), 
					page, 
					strAlbumPath, 
					tq);
			tq.in(thr);
			//PageController.getInstance().download(page, tq, strAlbumPath);
		}
		album.setDataStatus(DataStatus.down_ing);
		addCachePathMapping(album, strAlbumPath);
	}
	
	@Override
	public boolean subDownloadChange(Album album) {
		DataStatus ds = album.getDataStatus();
		if(isAllDown(album)){
			if(ds == DataStatus.down_ok){
				return false;
			}else{
				album.setStatus(DataStatus.down_ok);
				return true;
			}
		}
		return false;
	}
	
/*IResolveController*/
	
	@Override
	public void asyncResolve(
			ThreadQueue tqResolve,
			Album data, 
			boolean into) {
		IThreadInfo thr = new ResolveThread<Album>(
				AlbumController.getInstance(), 
				data, 
				into, 
				tqResolve);
		if(!into &&
				tqResolve.isShutdown()){
			tqResolve.init();
		}
		tqResolve.in(thr);
	}
	
	@Override
	public void resolve(Album album){
		//忽略
		if (album.isIgnore()){
			LOG.debug("resolve:Ignore " + album.getAlbumName());
		}else if (DataStatus.isResolve(album) && SETTING.ignoreResolved){
			LOG.debug("resolve:Ignore Resolved " + album.getAlbumName());
		}else{
			album.setAlbumPageList(AlbumController.getAlbumPages(album));
			album.setDataStatus(DataStatus.resolve_ok);
		}
	}

	@Override
	public void resolveInto(Album album, ThreadQueue tqResolve){
		//忽略（仅忽略自动解析/手动解析不忽略）
		if (album.isIgnore()){
			LOG.debug("resolveInto:Ignore " + album.getAlbumName());
			return;
		}
		for(Page page : album.getAlbumPageList()){
			IThreadInfo thr = new ResolveThread<Page>(
					PageController.getInstance(), 
					page, 
					true, 
					tqResolve);
			tqResolve.in(thr);
		}
	}
	
/*Self Use*/
	
	/**
	 * 取得此专辑页列表
	 * @param album
	 * @return 页列表
	 */
	private static ArrayList<Page> getAlbumPages(Album album) {
		LOG.debug("getAlbumPages:START");
		LOG.debug("getAlbumPages:" + album);
		ArrayList<Page> resultPageList = new ArrayList<Page>();
		Document doc = BlogController.openHtml(album.getAlbumUrl(), 10000);
		//获得页数
		Elements AlbumPageLists = doc.select("a[href][title=跳转至最后一页]");
		for (Element AlbumPageLink : AlbumPageLists) {
			String strTemp = AlbumPageLink.text();
			int i = new Integer(strTemp);
			if ( i > 0 ){
				album.setAlbumPageCount(i);
			}
		}
		if (AlbumPageLists.size() == 0){
			album.setAlbumPageCount(1);
		}
		for (int l = 1; l <= album.getAlbumPageCount(); l++){
			//页面信息
			Page pl = new Page();
			pl.setPageNO(l);
			pl.setPageType(album.getAlbumType());
			
			if (l==1){
				pl.setPageUrl(album.getAlbumUrl());
			}else {
				pl.setPageUrl(album.getAlbumUrl() + "/page" + l);
			}
			resultPageList.add(pl);
		}
		LOG.debug("getAlbumPages:专辑名=" + album.getAlbumName() + "-专辑内页数=" + album.getAlbumPageCount());
		LOG.debug("getAlbumPages:END");
		return resultPageList;
	}

}