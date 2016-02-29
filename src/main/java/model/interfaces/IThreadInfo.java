package model.interfaces;

/**
 * 线程信息
 * @author xiaoyao9184
 * @version 2.0
 */
public interface IThreadInfo {
	
	/**
	 * 获取数据
	 * @return
	 */
	public <T> T getDataObject();
	
	/**
	 * 获取数据状态
	 * @return
	 */
	public IDataStatus getDataStatus();
	
}
