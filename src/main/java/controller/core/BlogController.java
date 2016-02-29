package controller.core;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;
import com.xiaoyao.io.file.FolderNew;
import com.xiaoyao.io.file.PathName;

import controller.collections.RefreshProducer;
import controller.collections.ThreadQueue;
import controller.threads.DownLoadThread;
import controller.threads.ResolveThread;

import model.core.Blog;
import model.core.Album;
import model.core.Album.AlbumType;
import model.interfaces.IDataStatus;
import model.interfaces.IDataStatus.DataStatus;
import model.interfaces.IDownController;
import model.interfaces.IRefreshable;
import model.interfaces.IResolveController;
import model.interfaces.IShowImage;
import model.interfaces.IThreadInfo;
import model.interfaces.IWidgetController;
import model.setting.Setting;

/**
 * Blog控制器
 * @author xiaoyao9184
 * @version 2.0
 */
public class BlogController implements IResolveController<Blog>, IDownController<Blog>, IWidgetController {
	//常量
	private static final Setting SETTING = ContextController.getSetting();
	private static final Logger LOG = Logger.getLogger(BlogController.class);
	
	private Map<Blog, RefreshProducer> refreshMapping = Collections.synchronizedMap(
			new HashMap<Blog, RefreshProducer>());
	
	private Map<IDataStatus, String> cachePath = Collections.synchronizedMap(
			new HashMap<IDataStatus, String>());
	
	//单例
	private static BlogController instance;  
	private BlogController (){}
	public static synchronized BlogController getInstance() {  
		if (instance == null) {  
			instance = new BlogController();  
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
		Blog blog = (Blog)data;
		return blog.getBlogName();
	}
	@Override
	public String getLink(IDataStatus data){
		Blog blog = (Blog)data;
		return blog.getAlbumUrl().toString();
	}
	@Override
	public String getLinkText(IDataStatus data) {
		Blog blog = (Blog)data;
		return "博客：" + "<a href=\"" + blog.getAlbumUrl() + "\" >" + blog.getBlogName() + "</a>";
	}
	@Override
	public String getDataPlace(IDataStatus data) {
		if(cachePath.containsKey(data)){
			return cachePath.get(data);
		}else{
			Blog blog = (Blog)data;
			return blog.getAlbumUrl().toString();
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
			Blog blog = (Blog)data;
			return blog.getAlbumCount() + "个专辑";
		}else{
			return "还未解析";
		}
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ArrayList<IDataStatus> getSubList(IDataStatus data) {
		if(data == null){
			return null;
		}
		Blog blog = (Blog)data;
		return (ArrayList)blog.getAlbumList();
	}
	@Override
	public ArrayList<IDataStatus> getSubImageList(IDataStatus data) {
		return getSubList(data);
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
			refreshMapping.put((Blog)data, rp);
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
		Blog blog = (Blog)data;
		blog.setAlbumList(null);
		refresh(data);
	}
	@Override
	public void resetDown(IDataStatus data) {
		data.setStatus(DataStatus.resolve_ok);
		refresh(data);
	}
	
/*IDownController*/
	
	@Override
	public boolean isAllDown(Blog blog) {
		if(DataStatus.isIgnore(blog)){
			return true;
		}else if(!DataStatus.isResolve(blog) && SETTING.ignoreDownUnResolve){
			return true;
		}else if(DataStatus.isDown(blog)){
			return true;
		}else if(blog.getAlbumList() == null){
			return false;
		}
		for (IDataStatus subdata : blog.getAlbumList()) {
			if(!ContextController.getDownController(subdata).isAllDown(subdata)){
				return false;
			}
		}
		return true;
	}
	
	@Override
	public void download(Blog blog, ThreadQueue tq, String strBasePath) {
		if (blog.isIgnore()){
			//忽略
			LOG.debug("download:Ignore " + blog.getBlogName());
			return;
		}else if (!DataStatus.isResolve(blog) && SETTING.ignoreDownUnResolve){
			//忽略未解析
			LOG.debug("download:Ignore unResolved " + blog.getBlogName());
			return;
		}else{
			resolve(blog);
		}
		
		LOG.debug("download:" + blog.getBlogName());
		String strBlogPath = PathName.getName(strBasePath + blog.getBlogName() + "/");
		FolderNew.createFolder(strBlogPath);
		for (Album album : blog.getAlbumList()) {
			IThreadInfo thr = new DownLoadThread<Album>(
					AlbumController.getInstance(), 
					album, 
					strBlogPath, 
					tq);
			tq.in(thr);
			//AlbumController.getInstance().download(album, tq, strBlogPath);
		}
		blog.setDataStatus(DataStatus.down_ing);
		addCachePathMapping(blog, strBlogPath);
	}
	
	@Override
	public boolean subDownloadChange(Blog blog) {
		DataStatus ds = blog.getDataStatus();
		if(isAllDown(blog)){
			if(ds == DataStatus.down_ok){
				return false;
			}else{
				blog.setStatus(DataStatus.down_ok);
				return true;
			}
		}
		return false;
	}
	
/*IResolveController*/

	@Override
	public void asyncResolve(
			ThreadQueue tqResolve, 
			Blog blog, 
			boolean into) {
		IThreadInfo thr = new ResolveThread<Blog>(
				BlogController.getInstance(), 
				blog, 
				into, 
				tqResolve);
		if(!into &&
				tqResolve.isShutdown()){
			tqResolve.init();
		}
		tqResolve.in(thr);
	}
	
	@Override
	public void resolve(Blog blog){
		if (blog.isIgnore()){
			LOG.debug("resolve:Ignore " + blog.getBlogName());
		}else if (DataStatus.isResolve(blog) && SETTING.ignoreResolved){
			LOG.debug("resolve:Ignore Resolved " + blog.getBlogName());
		}else{
			blog.setPhotoUrl(BlogController.getBlogPhotoUrl(blog.getBlogName()));//sisimengmeng//1963zhangxinmin//1743189850
			blog.setAlbumUrl(BlogController.getBlogAlbumListUrl(blog.getPhotoUrl()));
			blog.setAlbumList(BlogController.getBlogAlbumListPages(blog.getAlbumUrl()));
			blog.setAlbumCount(blog.getAlbumList().size());
			blog.setDataStatus(DataStatus.resolve_ok);
		}
	}

	@Override
	public void resolveInto(Blog blog, ThreadQueue tqResolve){
		//忽略（仅忽略自动解析/手动解析不忽略）
		if (blog.isIgnore()){
			LOG.debug("resolveInto:Ignore " + blog.getBlogName());
			return;
		}
		for (Album album : blog.getAlbumList()) {	
			IThreadInfo thr = new ResolveThread<Album>(
					AlbumController.getInstance(), 
					album, 
					true, 
					tqResolve);
			tqResolve.in(thr);
			//AlbumController.getInstance().resolve(album, true, tqResolve);
		}
	}
	
/*Self Use*/
	
	/**
	 * 解析DOM中的专辑集合
	 * @param albumPageDocument DOM
	 * @return 
	 */
	private static ArrayList<Album> getBlogAlbum(Document albumPageDocument){
		LOG.debug("getBlogAlbum:START");
		ArrayList<Album> alsReturn = new ArrayList<Album>();
		
		Album al = null;
		Element FatherA = null;
		Element NextEM = null;
		
		//取得专辑
		Elements PhotoLinkLists = albumPageDocument.select("a[href] > img[src][title]"); //带有src,title属性的img元素
		for (Element Photolink : PhotoLinkLists) {
			al = new Album();
			al.setAlbumName(Photolink.attr("title"));
			al.setAlbumImageUrl(Photolink.attr("src"));					
			LOG.debug("getBlogAlbum:AlbumName=" + Photolink.attr("title"));
			LOG.debug("getBlogAlbum:AlbumImageUrl=" + Photolink.attr("src")); 
			FatherA = Photolink.parent();                    //取得父级，应该是个a元素
			al.setAlbumUrl(FatherA.attr("abs:href"));
			if ( al.getAlbumUrl().indexOf("blogpiclist") != -1){
				al.setAlbumType(AlbumType.BLOG_PIC_LIST);
			}
			LOG.debug("getBlogAlbum:AlbumUrl=" + FatherA.attr("abs:href")); 
			//取得专辑图片数量
			Elements PhotoTitleLinkLists = albumPageDocument.select("a[href=" + FatherA.attr("abs:href") + "][isvalue=1]"); //isvalue属性=1，href属性相同的a元素
			for (Element PhotoTitlelink : PhotoTitleLinkLists) {
				LOG.debug("getBlogAlbum:PhotoTitlelink=" + PhotoTitlelink.text()); 
				if ( al.getAlbumName().equals(PhotoTitlelink.text())){
					LOG.debug("getBlogAlbum:AlbumName与PhotoTitlelink不同" ); 					
				}
				NextEM = PhotoTitlelink.nextElementSibling();				
				if ( NextEM.nodeName().equals("em")  && NextEM.hasClass("SG_txtc")){//元素：有SG_txtc属性
					String strTemp =NextEM.text().replace("(", "").replace(")", "");
					al.setAlbumPhotoCount(new Integer( strTemp ));
					LOG.debug("getBlogAlbum:AlbumSubCount=" + al.getAlbumPhotoCount());
					if(al.getAlbumPhotoCount()==0 && SETTING.ignore0CountAlubm){
						al.setIgnore(true);//自动忽略为0的专辑
					}
				}
			}
			alsReturn.add(al);
			LOG.debug("getBlogAlbum:NEXT");
		}
		LOG.debug("getBlogAlbum:END");
		return alsReturn;
	}
	
	/**
	 * 获取专辑集合
	 * @param albumListUrl
	 * @return 所有的专辑列表
	 */
	private static ArrayList<Album> getBlogAlbumListPages(URL albumListUrl){
		LOG.debug("getBlogAlbumListPages:START");
		int PageCount = 1;
		Document doc = BlogController.openHtml(albumListUrl, 10000);
		//获得页数
		Elements PhotoPageLists = doc.select("a[href][title=跳转至最后一页]");
		for (Element PhotoPageLink : PhotoPageLists) {
			String strTemp = PhotoPageLink.text();
			int i = new Integer(strTemp);
			if ( i > 0 ){
				PageCount = i;
			}
		}
		if (PhotoPageLists.size() == 0){
			PageCount = 1;
		}
		LOG.debug("getBlogAlbumListPages:PhotoPage=" + PageCount);

		ArrayList<Album> result = BlogController.getBlogAlbum(doc);
		for (long l = 2; l <= PageCount ; l++){
			Document dom = BlogController.openHtml(albumListUrl.toString() + "/page" + l, 10000);
			ArrayList<Album> temp = BlogController.getBlogAlbum(dom);
			result.addAll(temp);
		}
		LOG.debug("getBlogAlbumListPages:END");
		return result;
	}
	
	/**
	 * 取得公开专辑网址
	 * 取得专辑数目
	 * @param blogPhotoUrl
	 * @return
	 */
	private static URL getBlogAlbumListUrl(URL blogPhotoUrl){
		LOG.debug("getBlogAlbumListUrl:START");
		Document doc = BlogController.openHtml(blogPhotoUrl, 10000);

		Elements links = doc.select("a[href]"); //带有href属性的a元素
		for (Element link : links) {
            if (link.children().size()!=0){		//有子元素
            	if (link.child(0).nodeName() == "em"){//第一个子元素节点名为em
            		LOG.debug("getBlogAlbumListUrl:AlbumListUrl=" + link.attr("abs:href"));
					if(SETTING.debug){
						LOG.debug("getAlbumListUrl:AlbumListUrl" + link.child(0).text()); 
						LOG.debug("getAlbumListUrl:AlbumListUrl" + link.text().substring(0,  link.text().indexOf("("))); 
					}
					String strTemp = link.text().substring(0,link.text().indexOf("("));	//"("前面的部分
					if ( !strTemp.equals("公开专辑") ){									//是公开专辑
						continue;
					}
					strTemp = link.child(0).text().replace("(", "").replace(")", "");
					//this.albumCount = new Integer(strTemp);
					LOG.debug("getBlogAlbumListUrl:AlbumListCount=" + strTemp); 
                	try {
                		LOG.debug("getBlogAlbumListUrl:END");
						return new URL(link.attr("abs:href"));
					} catch (MalformedURLException e) {
						e.printStackTrace();
						LOG.error("错误的URL测试",e);
					}
                }
            }
        }
		LOG.debug("getBlogAlbumListUrl:END");
		return null;
	}

	/**
	 * 得到博客图片网址
	 * @param blogName
	 * @return
	 */
	private static URL getBlogPhotoUrl(String blogName){
		LOG.debug("getBlogPhotoUrl:START");
		String strblogPhotourl = "http://photo.blog.sina.com.cn/";
		
		Pattern pattern = Pattern.compile("[0-9]*"); 
		if ( pattern.matcher(blogName).matches() ){
			strblogPhotourl = strblogPhotourl + "u/";
		}
		
		try {
			LOG.debug("getBlogPhotoUrl:PhotoUrl=" + strblogPhotourl + blogName);
			URL url = new URL(strblogPhotourl + blogName);
			LOG.debug("getBlogPhotoUrl:END");
			return url;
		} catch (MalformedURLException e) {
			e.printStackTrace();
			LOG.error("错误的URL格式", e);
			LOG.debug("getBlogPhotoUrl:END");
			return null;
		}
	}

	
/*Util*/
	
	/**
	 * 打开页面
	 * @param Url
	 * @param OutTime
	 * @return
	 */
	public static Document openHtml(String Url, int OutTime){
		try {
			return BlogController.openHtml(new URL(Url), OutTime);
		} catch (MalformedURLException e) {
			LOG.error("错误的URL格式", e);
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 打开页面
	 * @param Url
	 * @param OutTime
	 * @return
	 */
	public static Document openHtml(URL Url, int OutTime){
		Document doc = null;
		int timeOutCount = 0;
		while ( doc == null || timeOutCount > SETTING.maxTimeOutCount){
				
			try {
				doc = Jsoup.parse(Url, OutTime);
			} catch (IOException e) {
				timeOutCount++;
				LOG.debug("openHtml:Read timed out:" + timeOutCount + "--" +  Url.toString());
				try {
					Thread.sleep(SETTING.retrySleep);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			} 
			
		}
		return doc;
	}
	
	
	/**
	 * 读取
	 * @param strFile 文件路径
	 * @return 
	 */
	public static Blog read(String strFile){
		LOG.debug("read:Read sina pregress on disk:" + strFile);
		Blog blog = null;
		try {
			FileReader fr = new FileReader(strFile);
			Gson gson = new Gson();
			blog = gson.fromJson(fr, Blog.class);
			fr.close();
			return blog;
		} catch (Exception ex) {
			ex.printStackTrace();
            LOG.error("读取进度时出错！", ex);
			ContextController.showTip("读取进度时出错！");
		}
		return blog;
	}
	
	/**
	 * 保存
	 * @param blog
	 * @param strFilePath 保存路径
	 */
	public static void save(Blog blog, String strFilePath){
		LOG.debug("save:Save sina pregress on disk:" + strFilePath);
		try {
			Gson gson = new Gson();
			String json = gson.toJson(blog);
			FileWriter fw = new FileWriter(strFilePath);
			fw.write(json);
			fw.close();
        } catch (Exception ex){   
            ex.printStackTrace();
            LOG.error("保存进度时出错！", ex);
            ContextController.showTip("保存进度时出错！");
        }  
	}

}
