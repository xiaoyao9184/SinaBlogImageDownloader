package controller.collections;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import model.interfaces.IDataStatus.DataStatus;
import model.interfaces.IThreadInfo;

/**
 * 
 * 线程队列
 * @author xiaoyao9184
 * @version 2.0
 */
public class ThreadQueue extends RefreshProducer {
	/**
	 * 队列类型
	 * @author xiaoyao9184
	 * @version 2.0
	 */
	public enum QueueType{
		NONE,
		DOWN,
		RESOLVE;
		
		@Override
		public String toString(){
			String str = null;
			switch (this) {
			case DOWN:
				str = "下载队列";
				break;
			case RESOLVE:
				str = "解析队列";
				break;
			default:
				break;
			}
			return str;
		}
	}
	
	private static final String TAG = "ThreadQueue";
	private static final Logger LOG = Logger.getLogger(ThreadQueue.class);
	
/*DATA*/
	private QueueType type = QueueType.NONE;			//队列类型
	private int threadMaxCount = 0;						//队列执行数量
	private ThreadPoolExecutor executor = null;			//队列执行者
	private BlockingQueue<Runnable> queue = null;		//线程等待队列
	private ArrayList<IThreadInfo> threadList = null;	//线程列表
	
	private HashMap<DataStatus, ArrayList<IThreadInfo>> threadStatistics = null;	//统计信息
	private HashMap<IThreadInfo, DataStatus> threadStatistics2 = null;	            //统计信息
	private double percent = 0;				//完成百分比
	private int countOK = 0;				//完成数量
	private int countErr = 0;				//错误数量
	private int countAll = 0;				//线程数量
	
	public ThreadLocal<IThreadInfo> threadSession = new ThreadLocal<IThreadInfo>();  
	
	/**
	 * 构造
	 * @param ThreadMaxCount 线程最大数量
	 */
	public ThreadQueue(QueueType type, int ThreadMaxCount){
		this.type = type;
		this.threadMaxCount = ThreadMaxCount;
		this.threadStatistics = new HashMap<DataStatus, ArrayList<IThreadInfo>>();
		this.threadStatistics2 = new HashMap<>();
		this.threadList = new ArrayList<IThreadInfo>();
		this.queue = new LinkedBlockingQueue<Runnable>();  //无界
		this.executor = new ThreadPoolExecutor(
				this.threadMaxCount,
				this.threadMaxCount+10,
	    		1,
	    		TimeUnit.DAYS,
	    		this.queue,
	    		new ThreadQueueFactory(type.name()));
	}
	
/*Getter*/
	
	public synchronized QueueType getType() {
		return type;
	}
	public synchronized String getName() {
		return type.toString();
	}
	public synchronized int getMaxCount() {
		return threadMaxCount;
	}

	public synchronized BlockingQueue<Runnable> getWorkQueue() {
		return queue;
	}
	public synchronized int getWorkQueueSize() {
		return queue.size();
	}
	public synchronized int getWorkingSize() {
		return executor.getActiveCount();
	}
	public synchronized IThreadInfo getHistoryItem(int index){
		return threadList.get(index);
	}
	public synchronized int getHistorySize() {
		return threadList.size();
	}
	public synchronized int getCountSize() {
		return countAll;
	}
	public synchronized int getCompleteSize(DataStatus ds){
		if(threadStatistics.containsKey(ds)){
			return threadStatistics.get(ds).size();
		}
		return 0;
	}
	public synchronized boolean isWorking(){
		return executor.getActiveCount()>0;
	}
	public synchronized boolean isShutdown(){
		return executor.isShutdown();
	}
	public synchronized boolean isCompleteDown(){
		return getWorkQueueSize() == 0 && 
				!isWorking();
	}
	
/*Other*/
	
	/**
	 * 变更进度
	 * @return
	 */
	private boolean changeProgress(){
		int all = countAll;
		double not = getWorkQueueSize();
		double ing = getWorkingSize();
		double me = 1;
		
		//double percent = (executor.getCompletedTaskCount() + me) / all;
		double percent = (all - not - ing + me) / all;
		//TODO 设置百分比忽略阔值
		if(this.percent != percent){
			LOG.debug("changeProgress:percent is changed");
			this.percent = percent;
			switch (type) {
			case RESOLVE:
				countOK = getCompleteSize(DataStatus.resolve_ok);
				countErr = getCompleteSize(DataStatus.resolve_err);
				break;
			case DOWN:
				countOK = getCompleteSize(DataStatus.down_ok);
				countErr = getCompleteSize(DataStatus.down_err);
				break;
			default:
				break;
			}
			return true;
		}
		
		return false;
	}

	/**
	 * 最后一个任务
	 * @return
	 */
	private boolean isLastTask(){
		return (executor.getActiveCount() == 1 &&
				queue.size() == 0
				);
	}

	/** 
	 * 获取百分比字符串
	 * @param percent
	 * @return 
	  */ 
	private String toPercentString(double percent)  {
		String str;
	    NumberFormat nf = NumberFormat.getPercentInstance();
	    nf.setMinimumFractionDigits( 2 );
	    str = nf.format(percent);
	    return str;
	}

	/**
	 * 清空指定的历史线程
	 * @param list
	 */
	public synchronized void remove(List<IThreadInfo> list){
		queue.removeAll(list);
		threadList.removeAll(list);
	}
	
	/**
	 * 清空指定状态的历史线程
	 * @param ds
	 */
	public synchronized void remove(DataStatus ds){
		List<IThreadInfo> list = threadStatistics.get(ds);
		threadList.removeAll(list);
	}
	
	/**
	 * 清空完成的历史线程
	 */
	public synchronized void removeComplete(){
		List<IThreadInfo> list = null;
		if(type == QueueType.RESOLVE){
			list = threadStatistics.get(DataStatus.resolve_ok);
		}else if(type == QueueType.DOWN){
			list = threadStatistics.get(DataStatus.down_ok);
		}else{
			return;
		}
		
		threadList.removeAll(list);
	}
	
	/**
	 * 重试指定的历史线程
	 * @param list
	 */
	public synchronized void retry(List<IThreadInfo> list) {
		if(list != null){
			//利用历史列表已经存在的，不直接调用入队列方法
			for (IThreadInfo thread : list) {
				executor.execute((Runnable)thread);
			}
			
			refresh();
		}
	}

	/**
	 * 重试失败的历史线程
	 */
	public synchronized void retryError() {
		List<IThreadInfo> list = null;
		if(type == QueueType.RESOLVE){
			list = threadStatistics.get(DataStatus.resolve_err);
			threadStatistics.remove(DataStatus.resolve_err);
		}else if(type == QueueType.DOWN){
			list = threadStatistics.get(DataStatus.down_err);
			threadStatistics.remove(DataStatus.down_err);
		}else{
			return;
		}
		
		if(list != null){
			//利用历史列表已经存在的，不直接调用入队列方法
			for (IThreadInfo thread : list) {
				executor.execute((Runnable)thread);
			}
			
			refresh();
		}
	}
		
	/**
	 * 初始化队列
	 */
	public synchronized void init(){
		executor = new ThreadPoolExecutor(
				threadMaxCount,
				threadMaxCount+10,
	    		1,
	    		TimeUnit.DAYS,
	    		queue,
	    		new ThreadQueueFactory(type.name()));

		LOG.debug("init:" + this);
	}

	/**
	 * 关闭队列
	 */
	public synchronized void shutdown(){
		//使用 shutdown阻止添加新的，保证已经运行的线程不会再添加新线程
		//此时的线程历史列表可能并不是完整的
		executor.shutdown();
		queue.clear();

		LOG.debug("shutdown:" + this);
	}

	/**
	 * 入队列
	 * @param r 线程
	 */
	public synchronized void in(IThreadInfo thread){
		countAll++;
		threadList.add(thread);
		
		LOG.debug("in:" + this);
		
        executor.execute((Runnable)thread);
		refresh();
	}

	/**
	 * 统计(出队列)
	 * @param ti 线程
	 */
	public synchronized void complete(IThreadInfo ti){
		DataStatus dsnew = ti.getDataStatus().getStatus();
		DataStatus dsold = threadStatistics2.get(ti);
		if(dsold != null){
			LOG.debug("complete:already contains:" + dsold + "->" +  dsnew);
			threadStatistics2.remove(ti);
			if (threadStatistics.containsKey(dsold) && threadStatistics.get(dsold).contains(ti)){
				threadStatistics.get(dsold).remove(ti);
			}
		}
		
		if(threadStatistics.containsKey(dsnew)){
			threadStatistics.get(dsnew).add(ti);
			threadStatistics2.put(ti, dsnew);
		}else{
			ArrayList<IThreadInfo> list = new ArrayList<IThreadInfo>();
			list.add(ti);
			threadStatistics.put(dsnew, list);
			threadStatistics2.put(ti, dsnew);
		}

		if(changeProgress()){
			LOG.debug("complete:" + this);
			showProgress(
					type,
					toPercentString(percent)
					+ "\t(" + countOK + "+"+ countErr + ")"
					+ ":" + countAll);
		}
		if(isLastTask() &&
				percent == 1){
			showComplete();
		}
	}
	
	@Override
	public synchronized String toString() {
		return type.toString() +
				"\n\t状态:" + toPercentString(percent) +
				"\n\t统计:" + countOK + "+"+ countErr + "=" + countAll +
				"\n\t已结束:" + (threadList.size() - queue.size()) + 
				"\n\t未开始:" + queue.size() + 
				"\n\t总数:" + threadList.size();
	}
	

	/**
	 * 线程构造者
	 * @author xiaoyao9184
	 * @version 2.0
	 */
	private static class ThreadQueueFactory implements ThreadFactory{
//        static final AtomicInteger poolNumber = new AtomicInteger(1);  
//        final AtomicInteger threadNumber = new AtomicInteger(1);  
//        final String namePrefix;  
        
        private final ThreadGroup group;        //线程分组
        private String threadname = TAG;		//队列名称
        private String name = null;             //线程名称
        private int index = 0;                  //编号
        
        public ThreadQueueFactory(String name) {  
            SecurityManager s = System.getSecurityManager();  
            this.group = (s != null)? s.getThreadGroup() :  
                                 Thread.currentThread().getThreadGroup();
            this.name = name;
        }
        
		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(group, r, threadname + "-" + index + "-" + name, 0);
	    	if (t.isDaemon())//守护进程
	    		t.setDaemon(false);  
	    	if (t.getPriority() != Thread.NORM_PRIORITY)//进程优先级
	    		t.setPriority(Thread.NORM_PRIORITY);  
//	    	threadQueue.out(r);
	    	index++;
	    	return t;
		}
	}

}
