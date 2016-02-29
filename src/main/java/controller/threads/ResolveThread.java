package controller.threads;

import java.util.concurrent.RejectedExecutionException;

import org.apache.log4j.Logger;

import controller.collections.ThreadQueue;

import model.interfaces.IDataStatus;
import model.interfaces.IDataStatus.DataStatus;
import model.interfaces.IThreadInfo;
import model.interfaces.IWidgetController;
import model.interfaces.IResolveController;

/**
 * 解析 线程
 * @author xiaoyao9184
 * @version 2.0
 * @param <T>
 */
public class ResolveThread<T> extends Thread implements IThreadInfo{
	
	private static final String TAG = "ResolveThread";
	private static final Logger LOG = Logger.getLogger(ResolveThread.class);
	
/*DATA*/
	private IWidgetController wcontroller = null;
	private IResolveController<T> controller = null;
	private T data = null;
	private Boolean into = false;
	private ThreadQueue tqResolve = null;

	/**
	 * 
	 * @param <T>
	 * @param controller 控制器
	 * @param data 数据
	 * @param into 是否深入解析
	 * @param tqResolve 解析队列
	 */
	public ResolveThread(
			IResolveController<T> controller, 
			T data, 
			Boolean into, 
			ThreadQueue tqResolve){
		setName(TAG);
		this.controller = controller;
		this.data = data;
		this.into = into;
		this.tqResolve = tqResolve;
		
		if(controller instanceof IWidgetController){
			wcontroller = (IWidgetController)controller;
		}
	}
	
/*IThreadInfo*/
	
	@SuppressWarnings("unchecked")
	@Override
	public T getDataObject(){
		return data;
	}
	
	@Override
	public IDataStatus getDataStatus(){
		if (data instanceof IDataStatus) {
			return (IDataStatus)data;
		}
		return null;
	}
	
/*Runnable*/
	
	@Override
	public void run() {
		LOG.debug("run:START");
		//解析
		try {
			controller.resolve(data);
			
			//解析子集
			if(into){
				controller.resolveInto(data, tqResolve);
			}
		} catch (RejectedExecutionException ex){
			LOG.debug("run:强制结束", ex);
		} catch (Exception ex){
			ex.printStackTrace();
			LOG.error("run:错误", ex);
			if(data instanceof IDataStatus){
				//变更数据状态
				IDataStatus d = (IDataStatus)data;
				d.setStatus(DataStatus.down_err);
			}
		} finally {
			LOG.debug("run:成功");
			//刷新控件
			wcontroller.refresh((IDataStatus)data);
			
			//通知队列
			tqResolve.complete(this);
		}
		LOG.debug("run:END");
	}
}
