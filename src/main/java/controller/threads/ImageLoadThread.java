package controller.threads;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;

import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;

import com.xiaoyao.io.stream.StreamUtil;

import controller.core.ContextController;

import model.interfaces.IShowImage;

/**
 * 图片加载 线程
 * @author xiaoyao9184
 * @version 2.0
 */
public class ImageLoadThread extends Thread{
	
	private static final String TAG = "ImageThread";
	private static final Logger LOG = Logger.getLogger(ImageLoadThread.class);
	
/*DATA*/
	private String url = null;
	private IShowImage widget = null;
	
	/**
	 * 图片加载线程
	 * @param url URL
	 * @param refreshable 绑定的界面，会调用show，刷新数据
	 */
	public ImageLoadThread(String url, IShowImage showImage){
		setName(TAG);
		this.url = url;
		this.widget = showImage;
	}
	
	/**
	 * 取得Url中的图片
	 * @param display
	 * @param strUrl
	 * @return
	 * @throws Exception 
	 */
	private Image getImage(Display display, String strUrl) throws Exception{
		URL url = null;
		Image urlImage= null;

		url = new URL(strUrl);
		InputStream is = url.openStream();
		byte[] bytes = StreamUtil.toByte(is);
		
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		urlImage = new Image(display, bis);
		
		ContextController.setCacheUrl(this.url, bytes);

		return urlImage;
	}
	
/*Runnable*/
	
	@Override
	public void run() {
		LOG.debug("run:START");
		//获取Image
		try{
			if(widget != null){
				final Widget w = (Widget)widget;
				if (w != null && !w.isDisposed()){
					final Display display = w.getDisplay();
					final Image image = getImage(display, url);
					display.asyncExec(new Runnable() {
						public void run() {
							if (w != null && !w.isDisposed()){
								IShowImage showImage = (IShowImage) w;
								showImage.showImage(image);
							}
						}
					});
				}
			}
		} catch (Exception ex){
			ex.printStackTrace();
			LOG.error("run:错误", ex);
		}
		LOG.debug("run:END");
	}
}
