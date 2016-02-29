package model.core;

import java.io.Serializable;
import java.util.ArrayList;

import controller.core.ContextController;

import model.interfaces.IDataStatus;
import model.setting.Setting;

public class Album implements Serializable, IDataStatus {
	
	/**
	 * Album类型
	 * @author xiaoyao9184
	 * @version 2.0
	 */
	public static enum AlbumType{
		ORDINARY,		//普通
		BLOG_PIC_LIST	//博文配图
	}

	private static final long serialVersionUID = 1L;
	private static final String TAG = "Album";
	private static final Setting SETTING = ContextController.getSetting();
	
	//基本数据
	private String albumImageUrl = null; 				    //专辑图片
	private String albumName = null;     				    //专辑名称
	private String albumUrl = null;      				    //专辑连接
	private int albumPhotoCount = 0;    				    //专辑图片数
	private AlbumType albumType = AlbumType.ORDINARY;	    //专辑类型
	
	//解析数据:getPhotoLists后获取
	private int albumPageCount = 0;      				    //页数
	private ArrayList<Page> albumPageList = null;		    //页集合
	
	//标识
	private boolean isIgnore = false;					    //是否忽略子集
	private DataStatus dataStatus = DataStatus.notresolve;	//状态


/*Getter&Setter*/
	
	public String getAlbumImageUrl() {
		return albumImageUrl;
	}
	public void setAlbumImageUrl(String albumImageUrl) {
		this.albumImageUrl = albumImageUrl;
	}
	public String getAlbumName() {
		return albumName;
	}
	public void setAlbumName(String albumName) {
		this.albumName = albumName;
	}
	public String getAlbumUrl() {
		return albumUrl;
	}
	public void setAlbumUrl(String albumUrl) {
		this.albumUrl = albumUrl;
	}
	public int getAlbumPhotoCount() {
		return albumPhotoCount;
	}
	public void setAlbumPhotoCount(int albumPhotoCount) {
		this.albumPhotoCount = albumPhotoCount;
	}
	public AlbumType getAlbumType() {
		return albumType;
	}
	public void setAlbumType(AlbumType albumType) {
		this.albumType = albumType;
	}
	public int getAlbumPageCount() {
		return albumPageCount;
	}
	public void setAlbumPageCount(int albumPageCount) {
		this.albumPageCount = albumPageCount;
	}
	public ArrayList<Page> getAlbumPageList() {
		return albumPageList;
	}
	public void setAlbumPageList(ArrayList<Page> albumPageList) {
		this.albumPageList = albumPageList;
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
				"AlbumName=" + albumName + "," +
				"AlbumUrl=" + albumUrl +  "," +
				"AlbumImageUrl=" + albumImageUrl + "," +
				"AlbumPageCount=" + albumPageCount + "," +
				"AlbumPhotoCount=" + albumPhotoCount +
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
		return TAG + ":" + albumName; 
	}
	
}
