package model.core;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;

import controller.core.ContextController;

import model.interfaces.IDataStatus;
import model.setting.Setting;

public class Blog implements Serializable, IDataStatus {
	
	private static final long serialVersionUID = 1L;
	private static final String TAG = "Blog";
	private static final Setting SETTING = ContextController.getSetting();
	
	//基本数据
	private String blogName = null;						    //博客名
	private URL photoUrl = null;						    //图片链接
	private int albumCount = 0;							    //相册数量
	private URL albumUrl = null;						    //相册链接
	private ArrayList<Album> albumList = null;			    //相册列表

	//标识
	private boolean isIgnore = false;					    //是否忽略子集
	private DataStatus dataStatus = DataStatus.notresolve;	//状态
	
	public Blog(String name){
		blogName = name;
	}
	
/*Getter&Setter*/
	
	public String getBlogName() {
		return blogName;
	}
	public void setBlogName(String blogName) {
		this.blogName = blogName;
	}
	public URL getPhotoUrl() {
		return photoUrl;
	}
	public void setPhotoUrl(URL photoUrl) {
		this.photoUrl = photoUrl;
	}
	public int getAlbumCount() {
		return albumCount;
	}
	public void setAlbumCount(int albumCount) {
		this.albumCount = albumCount;
	}
	public URL getAlbumUrl() {
		return albumUrl;
	}
	public void setAlbumUrl(URL albumUrl) {
		this.albumUrl = albumUrl;
	}
	public ArrayList<Album> getAlbumList() {
		return albumList;
	}
	public void setAlbumList(ArrayList<Album> albumList) {
		this.albumList = albumList;
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
				"BlogName=" + blogName + "," +
				"PhotoUrl=" + photoUrl + "," +
				"AlbumCount=" + albumCount +  "," +
				"AlbumUrl=" + albumUrl +
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
		return TAG + ":" + blogName; 
	}
}
