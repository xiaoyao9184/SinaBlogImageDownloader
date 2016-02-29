package model.core;

import java.io.Serializable;
import java.util.ArrayList;

import controller.core.ContextController;

import model.interfaces.IDataStatus;
import model.setting.Setting;

public class Page implements Serializable, IDataStatus {
	
	private static final long serialVersionUID = 1L;
	private static final String TAG = "Page";
	private static final Setting SETTING = ContextController.getSetting();
	
	//基本数据
	private String pageUrl = null; 						         //页链接
	private int pageNO = 0;       						         //页编号
	private int pagePhotoCount = 0;     				         //页面图片数
	private int pagePhotoBlogCount = 0;      			         //页面博文数
	private Album.AlbumType pageType = Album.AlbumType.ORDINARY; //页面类型
	
	//解析数据:getPagePhotos/getPageBlogs后得到
	private ArrayList<Photo> photoList = null; 			         //图片列表
	private ArrayList<PhotoBlog> photoBlogList = null;  	     //博文列表
	
	//标识
	private boolean isIgnore = false;					    //是否忽略子集
	private DataStatus dataStatus = DataStatus.notresolve;	//状态
	
	
/*Getter&Setter*/
	
	public String getPageUrl() {
		return pageUrl;
	}
	public void setPageUrl(String pageUrl) {
		this.pageUrl = pageUrl;
	}
	public long getPageNO() {
		return pageNO;
	}
	public void setPageNO(int pageNO) {
		this.pageNO = pageNO;
	}
	public int getPagePhotoCount() {
		return pagePhotoCount;
	}
	public void setPagePhotoCount(int pagePhotoCount) {
		this.pagePhotoCount = pagePhotoCount;
	}
	public int getPagePhotoBlogCount() {
		return pagePhotoBlogCount;
	}
	public void setPagePhotoBlogCount(int pagePhotoBlogCount) {
		this.pagePhotoBlogCount = pagePhotoBlogCount;
	}
	public ArrayList<Photo> getPhotoList() {
		return photoList;
	}
	public void setPhotoList(ArrayList<Photo> photoList) {
		this.photoList = photoList;
	}
	public ArrayList<PhotoBlog> getPhotoBlogList() {
		return photoBlogList;
	}
	public void setPhotoBlogList(ArrayList<PhotoBlog> photoBlogList) {
		this.photoBlogList = photoBlogList;
	}
	public Album.AlbumType getPageType() {
		return pageType;
	}
	public void setPageType(Album.AlbumType pageType) {
		this.pageType = pageType;
	}
	public DataStatus getDataStatus() {
		return dataStatus;
	}
	public void setDataStatus(DataStatus dataStatus) {
		this.dataStatus = dataStatus;
	}
	
/*Object*/
	
	@Override
	public String toString() {
		return TAG + "{" + 
				"PageNO=" + pageNO + "," +
				"PageUrl=" + pageUrl + "," +
				"PageType=" + pageType.name() +  "," +
				(pageType==Album.AlbumType.ORDINARY?
						"PagePhotoCount=" + pagePhotoCount:
						"PageBlogCount=" + pagePhotoBlogCount)+
				"}"
				;
	}
	
/*IDataStatus*/
	
	@Override
	public boolean isIgnore() {
		return isIgnore;
	}
	
	@Override
	public void setIgnore(boolean isIgnore) {
		this.isIgnore = isIgnore;
	}
	
	@Override
	public void setStatus(DataStatus dataStatus) {
		this.dataStatus = dataStatus;
	}
	
	@Override
	public DataStatus getStatus() {
		return this.dataStatus;
	}
	
	@Override
	public String getName() {
		if(SETTING.debug){
			return this.toString();
		}
		return TAG + ":" + pageNO; 
	}
}
