package model.interfaces;

/**
 * 数据状态
 * @author xiaoyao9184
 * @version 2.0
 */
public interface IDataStatus {
	
	/**
	 * 数据状态
	 * @author xiaoyao9184
	 * @version 2.0
	 */
	public static enum DataStatus{
		notresolve,
		resolve_ok,
		resolve_err,
		down_ok,
		down_ing,
		down_err;
		
		@Override
		public String toString(){
			String str = null;
			switch(this){
			case notresolve:
				str = "队列中";
				break;
			case resolve_ok:
				str = "解析完毕";
				break;
			case resolve_err:
				str = "解析错误";
				break;
			case down_ok:
				str = "下载完毕";
				break;
			case down_ing:
				str = "下载中";
				break;
			case down_err:
				str = "下载错误";
				break;
			default:
				str = "未知";
				break;
			}
			return str;
		}
		
		/**
		 * 是否忽略
		 * @param data
		 * @return
		 */
		public static boolean isIgnore(IDataStatus data){
			return data.isIgnore();
		}
		
		/**
		 * 是否解析
		 * @param data
		 * @return
		 */
		public static boolean isResolve(IDataStatus data){
			if(data.getStatus() == DataStatus.resolve_ok ||
					data.getStatus() == DataStatus.down_ok ||
					data.getStatus() == DataStatus.down_err ||
					data.getStatus() == DataStatus.down_ing){
				return true;
			}
			return false;
		}
		
		/**
		 * 是否下载
		 * @param data
		 * @return
		 */
		public static boolean isDown(IDataStatus data){
			if(data.getStatus() == DataStatus.down_ok){
				return true;
			}
			return false;
		}
		
		/**
		 * 是否有错误
		 * @param data
		 * @return
		 */
		public static boolean isError(IDataStatus data){
			if(data.getStatus() == DataStatus.resolve_err ||
					data.getStatus() == DataStatus.down_err){
				return true;
			}
			return false;
		}
	}
	
	/**
	 * 获取数据名称
	 * @return
	 */
	public String getName();
	
	/**
	 * 设置状态
	 * @param dataStatus
	 */
	public void setStatus(DataStatus dataStatus);
	
	/**
	 * 获取数据状态
	 * @return
	 */
	public DataStatus getStatus();
	
	/**
	 * 获取是否忽略
	 * @return
	 */
	public boolean isIgnore();
	
	/**
	 * 设置是否忽略
	 * @param ignore
	 */
	public void setIgnore(boolean ignore);
	
}
