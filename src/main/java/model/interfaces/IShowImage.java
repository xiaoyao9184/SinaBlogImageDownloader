package model.interfaces;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * 显示图像接口
 * (必须封装SWT Display)
 * @author xiaoyao9184
 * @version 2.0
 */
public interface IShowImage {
	
	/**
	 * 获取SWT Display
	 * @return
	 */
	public abstract Display getDisplay();
	
	/**
	 * 显示图像
	 * @param image
	 */
	public void showImage(Image image);
	
}
