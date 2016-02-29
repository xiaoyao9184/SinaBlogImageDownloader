package model.core;

import java.io.Serializable;

import controller.core.ContextController;

import model.interfaces.IDataStatus;
import model.setting.Setting;

public class Photo implements Serializable, IDataStatus {

	/**
	 * 下载类型
	 * @author xiaoyao9184
	 *
	 */
	public static enum DownType{
		small,	//小图
		big,	//大图
		middle,	//中图
	}
	
	/**
	 * URL类型
	 * @author xiaoyao9184
	 *
	 */
	public static enum UrlType{
		small,		//小图
		big,		//大图
		middle,		//中图
		blogpic,	//预览图
		err			//错误
	}
	
	/**
	 * 保存命名规则
	 * @author xiaoyao9184
	 *
	 */
	public static enum ReNameStyle{
		number, 	//图片编号
		title, 		//图片标题
		id, 		//图片ID
		downType	//
	}

	private static final long serialVersionUID = 1L;
	private static final String TAG = "Photo";
	private static final Setting SETTING = ContextController.getSetting();
	
	//基本数据
	private String photoName = null;                        //名称
	private String photoUrl = null;                         //连接
	private String photoID = null;	                        //ID
	private int photoNO = 0;		                        //编号
	private String photoSmallUrl = null;                    //小图连接
	private String photoMiddleUrl = null;                   //中图连接
	private String photoBigUrl = null;                      //大图连接
	
	//标识
	private boolean isIgnore = false;	                    //是否忽略子集
	private DataStatus dataStatus = DataStatus.notresolve;  //状态
	
/*Getter&Setter*/
	
	public String getPhotoName() {
		return photoName;
	}
	public void setPhotoName(String photoName) {
		this.photoName = photoName;
	}
	public String getPhotoUrl() {
		return photoUrl;
	}
	public void setPhotoUrl(String photoUrl) {
		this.photoUrl = photoUrl;
	}
	public String getPhotoID() {
		return photoID;
	}
	public void setPhotoID(String photoID) {
		this.photoID = photoID;
	}
	public int getPhotoNO() {
		return photoNO;
	}
	public void setPhotoNO(int photoNO) {
		this.photoNO = photoNO;
	}
	public String getPhotoSmallUrl() {
		return photoSmallUrl;
	}
	public void setPhotoSmallUrl(String photoSmallUrl) {
		this.photoSmallUrl = photoSmallUrl;
	}
	public String getPhotoMiddleUrl() {
		return photoMiddleUrl;
	}
	public void setPhotoMiddleUrl(String photoMiddleUrl) {
		this.photoMiddleUrl = photoMiddleUrl;
	}
	public String getPhotoBigUrl() {
		return photoBigUrl;
	}
	public void setPhotoBigUrl(String photoBigUrl) {
		this.photoBigUrl = photoBigUrl;
	}
	public DataStatus getDataStatus() {
		return dataStatus;
	}
	public void setDataStatus(DataStatus dataStatus) {
		this.dataStatus = dataStatus;
	}
	
/*Ohter*/
	
	/**
	 * 设定URL（自动）
	 * @param strLink
	 * @return URL类型
	 */
	public UrlType setUrl(String strLink){
		if ( strLink.indexOf("small") != -1){
			this.photoSmallUrl = strLink;
System.out.println(TAG + ":setUnLink:small=" + strLink); 
			return UrlType.small;				
		}else if ( strLink.indexOf("middle") != -1){
			this.photoMiddleUrl = strLink;
System.out.println(TAG + ":setUnLink:middle=" + strLink); 
			return UrlType.middle;
		}else if ( strLink.indexOf("original") != -1){
			this.photoBigUrl = strLink;
System.out.println(TAG + ":setUnLink:big=" + strLink); 
			return UrlType.big;
		}else if (strLink.indexOf("blogpic") != -1){
			this.photoUrl = strLink;
System.out.println(TAG + ":setUnLink:url=" + strLink); 
			return UrlType.blogpic;
		}else{
System.out.println(TAG + ":setUnLink:ERR:" + strLink ); 
			return UrlType.err;
		}
	}
	
	/**
	 * 取得指定的URL连接
	 * @param ut URL类型
	 * @return
	 */
	public String getUrl(UrlType ut){
		String strResult = null;
		switch (ut){
		case big:
			strResult = this.photoBigUrl;
			break;
		case middle:
			strResult = this.photoMiddleUrl;
			break;
		case small:
			strResult = this.photoSmallUrl;
			break;
		default:
			strResult = this.photoUrl;
			break;
		}
		return strResult;
	}
	
	/**
	 * 返回指定的连接
	 * @param dt
	 * @return
	 */
	public String getDownUrl(DownType dt){
		String strResult = null;
		switch (dt){
		case big:
			strResult = this.photoBigUrl;
			break;
		case middle:
			strResult = this.photoMiddleUrl;
			break;
		case small:
			strResult = this.photoSmallUrl;
			break;
		default:
		
		}
		return strResult;
	}
	
	/**
	 * 名称
	 * @param rs 重命名方式
	 * @return
	 */
	public String getReName(ReNameStyle rs) {
		String strResult = null;
		switch (rs){
		case id:
			strResult = this.photoID + ".jpg";
			break;
		case title:
			if ( this.photoName.equals("") ){
				strResult = this.photoID + ".jpg";
			}else{
				strResult = this.photoName + ".jpg";
			}
			break;
		default:
		
		}
		return strResult;
	}
	
/*Object*/
	
	@Override
	public String toString() {
		return TAG + "{" + 
				"PhotoNO=" + photoNO +  "," +
				"PhotoUrl=" + photoUrl + "," +
				"PhotoID=" + photoID + "," +
				"PhotoName=" + photoName + "," +
				"PhotoSmallUrl=" + photoSmallUrl +
				"PhotoMiddleUrl=" + photoMiddleUrl +
				"PhotoBigUrl=" + photoBigUrl +
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
		return TAG + ":" + photoName; 
	}
}
