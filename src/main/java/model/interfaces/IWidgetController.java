package model.interfaces;

import java.util.ArrayList;

/**
 * 界面控制器
 * @author xiaoyao9184
 * @version 2.0
 */
public interface IWidgetController {
	
	/**
	 * 获取选中状态
	 * @param data
	 * @return 
	 */
	public abstract boolean getChecked(IDataStatus data);
	
	/**
	 * 设置选中状态
	 * @param data
	 * @param check
	 * @return
	 */
	public abstract void setChecked(IDataStatus data, boolean check);
	
	/**
	 * 获取显示文本
	 * @param data
	 * @return
	 */
	public abstract String getText(IDataStatus data);
	
	/**
	 * 超链接
	 * @param data
	 * @return
	 */
	public abstract String getLink(IDataStatus data);
	
	/**
	 * 超链接文本
	 * @param data
	 * @return
	 */
	public abstract String getLinkText(IDataStatus data);
	
	/**
	 * 获取超链接
	 * @param data
	 * @return
	 */
	public abstract String getDataPlace(IDataStatus data);

	/**
	 * 获取数据位置
	 * @return
	 */
	public abstract String getDataPlaceText(IDataStatus data);
	
	/**
	 * 包含子集
	 * @param data
	 * @return
	 */
	public abstract boolean isHaveSub(IDataStatus data);
	
	/**
	 * 子集数量文本
	 * @param data
	 * @return
	 */
	public abstract String getSubCountText(IDataStatus data);

	/**
	 * 获取子集
	 * @param data
	 * @return 允许NULL
	 */
	public abstract ArrayList<IDataStatus> getSubList(IDataStatus data);
	
	/**
	 * 获取预览子集(用于预览图片)
	 * @param data
	 * @return 允许NULL
	 */
	public abstract ArrayList<IDataStatus> getSubImageList(IDataStatus data);

	/**
	 * (异步)加载图片
	 * @param data
	 * @param refreshable 可以为NULL
	 * @param isUseCache
	 */
	public abstract void asyncLoadImage(IDataStatus data, IShowImage showImage, boolean isUseCache);

	/**
	 * 增加刷新映射
	 * @param data
	 * @param refreshable
	 */
	public abstract void addRefreshMapping(IDataStatus data, IRefreshable refreshable);
	
	/**
	 * 删除刷新映射
	 * @param data
	 * @param refreshable
	 */
	public abstract void removeRefreshMapping(IDataStatus data, IRefreshable refreshable);
	
	/**
	 * 刷新
	 * @param data
	 */
	public abstract void refresh(IDataStatus data);

	/**
	 * 重置解析状态
	 * @param data
	 */
	public abstract void resetResolve(IDataStatus data);

	/**
	 * 重置下载状态
	 * @param data
	 */
	public abstract void resetDown(IDataStatus data);
	
}
