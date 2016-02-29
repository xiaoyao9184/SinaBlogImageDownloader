package controller.threads;

import java.util.concurrent.RejectedExecutionException;

import org.apache.log4j.Logger;

import controller.collections.ThreadQueue;

import model.interfaces.IDataStatus;
import model.interfaces.IDataStatus.DataStatus;
import model.interfaces.IThreadInfo;
import model.interfaces.IWidgetController;
import model.interfaces.IDownController;

/**
 * 下载 线程
 * @author xiaoyao9184
 * @version 2.0
 * @param <T>
 */
@SuppressWarnings("rawtypes")
public class DownLoadThread<T> extends Thread implements IThreadInfo{
	
	private static final String TAG = "DownLoadThread";
	private static final Logger LOG = Logger.getLogger(DownLoadThread.class);

/*DATA*/
	private IWidgetController controllerWidget = null;
    private IDownController<T> controllerDown = null;
	private T data = null;
	private String basePath = null;
	private ThreadQueue tqDown = null;
	
	private DownLoadThread<?> fatherThread = null;
    
    /**
	 * 下载线程
	 * @param controller 控制器
	 * @param data 数据
	 * @param basePath 基本路径
	 * @param tqResolve 下载队列
	 */
	public DownLoadThread(
			IDownController<T> controller, 
			T data, 
			String basePath, 
			ThreadQueue tqResolve){
		setName(TAG);
		this.controllerDown = controller;
		this.data = data;
		this.basePath = basePath;
		this.tqDown = tqResolve;
		
		if(controller instanceof IWidgetController){
			controllerWidget = (IWidgetController)controller;
		}
		
		//获取当前线程（创建者/父级）
		IThreadInfo thread = this.tqDown.threadSession.get();
		if(thread instanceof DownLoadThread<?>){
			fatherThread = (DownLoadThread)thread;
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
    
/*Other*/
	
	/**
	 * 通知父级线程
	 */
	private synchronized void complete(){
		if(controllerDown.subDownloadChange(data)){
			//刷新控件
			controllerWidget.refresh((IDataStatus)data);
			
			//通知队列
			tqDown.complete(this);
			
			//刷新父级
			if(fatherThread != null){
				fatherThread.complete();
			}
		}
//		if(controllerDown.isAllDown(data)){
//			getDataStatus().setStatus(DataStatus.down_ok);
//			
//			//刷新控件
//			controllerWidget.refresh((IDataStatus)data);
//			
//			//通知队列
//			tqDown.complete(this);
//			
//			//刷新父级
//			if(fatherThread != null){
//				fatherThread.complete();
//			}
//		}
	}
	
/*Runnable*/

    @Override
	public void run() {
		LOG.debug("run:START");
    	tqDown.threadSession.set(this);
    	//下载
		try {
			controllerDown.download(data, tqDown, basePath);
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
			//this.complete();
			//刷新控件
			controllerWidget.refresh((IDataStatus)data);
			
			//通知队列
			tqDown.complete(this);
			
			//刷新父级
			if(fatherThread != null){
				fatherThread.complete();
			}
		}
		LOG.debug("run:END");
	}
}
