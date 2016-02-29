package model.interfaces;

import controller.collections.ThreadQueue;

/**
 * 下载控制器
 * @author xiaoyao9184
 * @version 2.0
 * @param <T>
 */
public interface IDownController<T> {
	
	/**
	 * 下载
	 * @param data
	 * @param tq
	 * @param strPath
	 */
	public abstract void download(T data, ThreadQueue tq, String strPath);

	/**
	 * 是否都下载完毕
	 * @param d
	 * @return
	 */
	public abstract boolean isAllDown(T d);
	
	/**
	 * 子集下载状态变更
	 * @param d
	 * @return 状态是否变更
	 */
	public abstract boolean subDownloadChange(T d);
	
}
