package model.core;

import java.io.Serializable;
import java.util.ArrayList;

import org.jsoup.nodes.Element;

import controller.core.ContextController;

import model.interfaces.IDataStatus;
import model.setting.Setting;

public class PhotoBlog implements Serializable, IDataStatus {
	

	private static final long serialVersionUID = 1L;
	private static final String TAG = "PhotoBlog";
	private static final Setting SETTING = ContextController.getSetting();
	
	//基本数据
	private String blogName = null;                         //博文名称
	private String blogUrl = null;                          //博文连接
	private String blogTime = "";	                        //博文时间
	private String blogID = null;                           //博文ID
	private int blogNO = 0;                                 //博文编号
	
	//缓存
	private transient Element blogPhotoElement = null;      
	
	//解析数据:getPhotoLists后得到
	private int blogPhotoCount = 0;                         //博文图片数
	private ArrayList<Photo> photoList = null;              //博文图片
	
	//标识
	private boolean isIgnore = false;					    //是否忽略子集
	private DataStatus dataStatus = DataStatus.notresolve;	//状态
	

/*Getter&Setter*/
	
	public String getBlogName() {
		return blogName;
	}
	public void setBlogName(String blogName) {
		this.blogName = blogName;
	}
	public String getBlogUrl() {
		return blogUrl;
	}
	public void setBlogUrl(String blogUrl) {
		this.blogUrl = blogUrl;
	}
	public String getBlogTime() {
		return blogTime;
	}
	public void setBlogTime(String blogTime) {
		this.blogTime = blogTime;
	}
	public String getBlogID() {
		return blogID;
	}
	public void setBlogID(String blogID) {
		this.blogID = blogID;
	}
	public int getBlogNO() {
		return blogNO;
	}
	public void setBlogNO(int blogNO) {
		this.blogNO = blogNO;
	}
	public Element getBlogPhotoElement() {
		return blogPhotoElement;
	}
	public void setBlogPhotoElement(Element blogPhotoElement) {
		this.blogPhotoElement = blogPhotoElement;
	}
	public int getBlogPhotoCount() {
		return blogPhotoCount;
	}
	public void setBlogPhotoCount(int blogPhotoCount) {
		this.blogPhotoCount = blogPhotoCount;
	}
	public ArrayList<Photo> getPhotoList() {
		return photoList;
	}
	public void setPhotoList(ArrayList<Photo> photoList) {
		this.photoList = photoList;
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
				"BlogNO=" + blogNO + "," +
				"BlogUrl=" + blogUrl + "," +
				"BlogID=" + blogID + "," +
				"BlogName=" + blogName + "," +
				"BlogTime=" + blogTime + "," +
				"BlogPhotoCount=" + blogPhotoCount +
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
