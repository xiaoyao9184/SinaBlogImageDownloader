package controller.collections;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Widget;

import controller.collections.ThreadQueue.QueueType;
import model.interfaces.IRefreshable;
import model.interfaces.IShowStatusable;

/**
 * 刷新者
 * @author xiaoyao9184
 * @version 2.0
 */
public class RefreshProducer {
	private static final Logger LOG = Logger.getLogger(RefreshProducer.class);
	
	private transient ArrayList<IRefreshable> rs = new ArrayList<IRefreshable>();        //刷新集（控件）
	private transient ArrayList<IShowStatusable> ss = new ArrayList<IShowStatusable>();  //刷新集（控件）
	private transient RefreshProducer father = null;                                     //父级
	private transient byte[] lock = new byte[0];
	
	/**
	 * 设置父级
	 * @param FatherMakeRefresh
	 */
	public void setFather(RefreshProducer FatherMakeRefresh){
		this.father = FatherMakeRefresh;
	}
	
	/**
	 * 添加
	 * @param r
	 */
	public void addRefreshable(IRefreshable r){
		synchronized (lock) {
			if ( rs.indexOf(r) == -1 ){
				rs.add(r);
			}
		}
	}
	
	/**
	 * 删除
	 * @param r
	 */
	public void deleRefreshable(IRefreshable r){
		synchronized (lock) {
			rs.remove(r);
		}
	}
	
	/**
	 * 添加
	 * @param r
	 */
	public void addShowStatusable(IShowStatusable r){
		synchronized (lock) {
			if ( ss.indexOf(r) == -1 ){
				ss.add(r);
			}
		}
	}
	
	/**
	 * 删除
	 * @param r
	 */
	public void deleShowStatusable(IShowStatusable r){
		synchronized (lock) {
			ss.remove(r);
		}
	}
	
	/**
	 * 回调刷新
	 */
	public void refresh(){
		synchronized (lock) {
			for(final IRefreshable r :rs){
				final Widget w = (Widget) r;
				if (w != null && !w.isDisposed()){
					//TODO 可能这里控件会被disposed，应该互斥disposed方法与getDisplay方法
					w.getDisplay().asyncExec(new Runnable() {
						public void run() {
							if (w != null && !w.isDisposed()){
								LOG.debug("GUI:asyncExec: Will call refresh");
								r.refresh();
							}else {
								LOG.debug("GUI:asyncExec: Widget is disposed");
							}
						}
					});	
				}
			}
			if (father!=null){
				father.refresh();
			}
		}
	}
	
	/**
	 * 回调显示进度
	 * @param queueType
	 * @param msg
	 */
	public void showProgress(final QueueType queueType, final String msg){
		synchronized (lock) {
			for(final IShowStatusable s :ss){
				final Widget w = s.getWidget();
				if (w != null && !w.isDisposed()){
					w.getDisplay().asyncExec(new Runnable() {
						public void run() {
							if (w != null && !w.isDisposed()){
								LOG.debug("GUI:asyncExec: Will call showStatus");
								s.showStatus(queueType, msg);
							}else {
								LOG.debug("GUI:asyncExec: Widget is disposed");
							}
						}
					});	
				}
			}
		}
	}
	
	/**
	 * 回调完成
	 */
	public void showComplete(){
		synchronized (lock) {
			for(final IShowStatusable s :ss){
				final Widget w = s.getWidget();
				if (w != null && !w.isDisposed()){
					w.getDisplay().asyncExec(new Runnable() {
						public void run() {
							if (w != null && !w.isDisposed()){
								LOG.debug("GUI:asyncExec: Will call showStatistics");
								s.showStatistics();
							}else {
								LOG.debug("GUI:asyncExec: Widget is disposed");
							}
						}
					});	
				}
			}
		}
	}
	
}
