package model.interfaces;

import controller.collections.ThreadQueue;

/**
 * 解析控制器
 * @author xiaoyao9184
 * @version 2.0
 * @param <T>
 */
public interface IResolveController<T> {
	
	/**
	 * 异步解析
	 * @param data
	 */
	public abstract void asyncResolve(
			ThreadQueue tqResolve,
			T data, 
			boolean into);
	
	/**
	 * 解析
	 * @param <T>
	 * @param data
	 */
	public abstract void resolve(T data);
	
	/**
	 * 解析子集
	 * @param <T>
	 * @param data
	 * @param tqResolve
	 */
	public abstract void resolveInto(T data, ThreadQueue tqResolve);
	
}
