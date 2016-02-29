package model.interfaces;

import org.eclipse.swt.widgets.Widget;

import controller.collections.ThreadQueue.QueueType;

/**
 * 可显示状态的
 * (必须封装SWT Widget)
 * @author xiaoyao9184
 * @version 2.1
 */
public interface IShowStatusable {
	
	/**
	 * 获取SWT Widget
	 * @return
	 */
	public abstract Widget getWidget();
	
	/**
	 * 显示状态信息
	 * @param queueType
	 * @param msg
	 */
	public abstract void showStatus(QueueType queueType, String msg);
	
	/**
	 * 显示统计
	 */
	public abstract void showStatistics();
	
}
