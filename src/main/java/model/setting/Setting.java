package model.setting;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

import com.xiaoyao.io.file.PathName;

/**
 * 设置
 * (更改字段命名同时更新cfg.json资源文件)
 * @author xiaoyao9184
 * @version 2.0
 */
public class Setting {
	public boolean debug = true;               //是否是DEBUG模式
	public String version = "";                //版本
	
	public List<String> blogHistory = new ArrayList<>();  //历史纪录
	
	public String downPath = null;             //默认下载位置(JAR目录)
	public int downThreadCount = 10 ;          //下载线程数量
	public int resolveThreadCount = 10;        //解析线程数量
	
	public int maxTimeOutCount = 5;            //超时重试次数
	public int retrySleep = 3000;              //超时重试间隔
		
	public boolean useAlubmPath = true;        //使用专辑文件夹分组
	public boolean usePagePath = false;        //使用页文件夹分组
	public boolean useBlogPath = true;         //使用博文文件夹分组
	public boolean usePageNO = false;          //使用页编号分组(不使用页文件夹分组时有效)
	public boolean useBlogNO = false;          //使用博文编号分组(不使用博文文件夹分组有效)
	
	public model.core.Photo.DownType downType = model.core.Photo.DownType.big;          //图片下载类型
	public model.core.Photo.ReNameStyle reNameStyle = model.core.Photo.ReNameStyle.id;  //图片命名
	
	public boolean ignore0CountAlubm = true;	//忽略没有图片的专辑
	public boolean ignoreResolved = true;		//忽略已经解析
	public boolean ignoreDowned = true;			//忽略已经下载
	public boolean ignoreDownUnResolve = false;	//忽略没有解析的下载
	
	public Setting(){
		try {
			downPath = Setting.class.
					getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
			downPath = downPath.substring(0, downPath.lastIndexOf('/')+1);
			downPath = PathName.getRealName(downPath) + File.separator;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getDownPath() {
		return downPath;
	}

	public void setDownPath(String downPath) {
		this.downPath = downPath;
	}

	public int getDownThreadCount() {
		return downThreadCount;
	}

	public void setDownThreadCount(int downThreadCount) {
		this.downThreadCount = downThreadCount;
	}

	public int getResolveThreadCount() {
		return resolveThreadCount;
	}

	public void setResolveThreadCount(int resolveThreadCount) {
		this.resolveThreadCount = resolveThreadCount;
	}

	public int getMaxTimeOutCount() {
		return maxTimeOutCount;
	}

	public void setMaxTimeOutCount(int maxTimeOutCount) {
		this.maxTimeOutCount = maxTimeOutCount;
	}

	public int getRetrySleep() {
		return retrySleep;
	}

	public void setRetrySleep(int retrySleep) {
		this.retrySleep = retrySleep;
	}

	public boolean isUseAlubmPath() {
		return useAlubmPath;
	}

	public void setUseAlubmPath(boolean useAlubmPath) {
		this.useAlubmPath = useAlubmPath;
	}

	public boolean isUseBlogPath() {
		return useBlogPath;
	}

	public void setUseBlogPath(boolean useBlogPath) {
		this.useBlogPath = useBlogPath;
	}

	public boolean isUsePagePath() {
		return usePagePath;
	}

	public void setUsePagePath(boolean usePagePath) {
		this.usePagePath = usePagePath;
	}

	public boolean isUseBlogNO() {
		return useBlogNO;
	}

	public void setUseBlogNO(boolean useBlogNO) {
		this.useBlogNO = useBlogNO;
	}

	public boolean isUsePageNO() {
		return usePageNO;
	}

	public void setUsePageNO(boolean usePageNO) {
		this.usePageNO = usePageNO;
	}

	public model.core.Photo.DownType getDownType() {
		return downType;
	}

	public void setDownType(model.core.Photo.DownType downType) {
		this.downType = downType;
	}

	public model.core.Photo.ReNameStyle getReNameStyle() {
		return reNameStyle;
	}

	public void setReNameStyle(model.core.Photo.ReNameStyle reNameStyle) {
		this.reNameStyle = reNameStyle;
	}

	public boolean isIgnore0CountAlubm() {
		return ignore0CountAlubm;
	}

	public void setIgnore0CountAlubm(boolean ignore0CountAlubm) {
		this.ignore0CountAlubm = ignore0CountAlubm;
	}

	public boolean isIgnoreResolved() {
		return ignoreResolved;
	}

	public void setIgnoreResolved(boolean ignoreResolved) {
		this.ignoreResolved = ignoreResolved;
	}

	public boolean isIgnoreDowned() {
		return ignoreDowned;
	}

	public void setIgnoreDowned(boolean ignoreDowned) {
		this.ignoreDowned = ignoreDowned;
	}
}
