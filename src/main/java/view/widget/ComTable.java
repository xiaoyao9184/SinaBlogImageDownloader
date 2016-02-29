package view.widget;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import controller.collections.ThreadQueue;
import controller.collections.ThreadQueue.QueueType;
import controller.core.ContextController;

import model.interfaces.IDataStatus.DataStatus;
import model.interfaces.IRefreshable;
import model.interfaces.ISelectSub;
import model.interfaces.IThreadInfo;

/**
 * 表页
 * @author xiaoyao9184
 * @version 2.0
 */
public class ComTable extends Composite implements IRefreshable, ISelectSub {
	//按钮命令
	private enum Commandos{
		select,
		remove,
		open, 
		retry
	} 
	
	private static final Logger LOG = Logger.getLogger(ComTable.class);
    
/*GUI*/
    private Table table;
    private TableColumn tblclmnNo;        //NO
    private TableColumn tblclmnStatus;    //Status
    private TableColumn tblclmnName;      //Name
    private TableColumn tblclmnData;      //Data Status
    private Menu menu;
    
/*DATA*/
    private ThreadQueue queue;
    
/*Widget*/
    
    /**
     * 标准构造
     * @param parent
     * @param style
     */
    public ComTable(Composite parent, int style) {
        super(parent, style);
        this.setLayout(new GridLayout(1, false));
        
        GridData treeGridData = new GridData(GridData.FILL_BOTH);
        treeGridData.widthHint = 100;
        treeGridData.horizontalSpan = 1;
        
        table = new Table(this, SWT.BORDER | SWT.CHECK | SWT.FULL_SELECTION | SWT.MULTI);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        
        MenuSelectAdapter msa = new MenuSelectAdapter();
        
        //下文菜单
        //TODO 打开编辑页面
        menu = new Menu(table);
        
        MenuItem mSelect = new MenuItem(menu, SWT.NONE);
		mSelect.setText("选中");
		mSelect.setData("com", Commandos.select);
		mSelect.addSelectionListener(msa);
        
		MenuItem mRemove = new MenuItem(menu, SWT.NONE);
		mRemove.setText("移除");
		mRemove.setData("com", Commandos.remove);
		mRemove.addSelectionListener(msa);
		
		MenuItem mOpen = new MenuItem(menu, SWT.NONE);
		mOpen.setText("打开");
		mOpen.setData("com", Commandos.open);
		mOpen.addSelectionListener(msa);
		
        table.setMenu(menu);
        
        tblclmnNo = new TableColumn(table, SWT.NONE);
        tblclmnNo.setWidth(100);
        tblclmnNo.setText("序号");
        
        tblclmnStatus = new TableColumn(table, SWT.NONE);
        tblclmnStatus.setWidth(100);
        tblclmnStatus.setText("状态");
                
        tblclmnName = new TableColumn(table, SWT.NONE);
        tblclmnName.setWidth(200);
        tblclmnName.setText("名称");
        
        tblclmnData = new TableColumn(table, SWT.NONE);
        tblclmnData.setWidth(400);
        tblclmnData.setText("数据");
    }
    
    @Override  
    public void dispose() {
    	this.queue.deleRefreshable(this);
    	LOG.debug("dispose:tableItems length:" + table.getItems().length);
        
        for (TableItem ti : table.getItems()){
            ti.dispose();
        }
        table.dispose();
        super.dispose();
    }
    
/*Other*/
    
    /**
	 * 重试
	 */
	private void retrySelect() {
		List<IThreadInfo> dataList = new ArrayList<>();
	    for (TableItem ti : table.getSelection()){
	    	if(ti instanceof TableBase){
				TableBase tb = (TableBase)ti;
				dataList.add(tb.getData());
	        }
	    }
	    queue.retry(dataList);
	}

	/**
	 * 勾选
	 */
	private void checkSelect(){
		for (TableItem t : table.getSelection()) {
			t.setChecked(true);
		}
	}

	/**
	 * 移除
	 */
	private void removeSelect(){
		List<IThreadInfo> dataList = new ArrayList<>();
	    List<Integer> indexList = new ArrayList<>();
	    int index = 0;
	    for (TableItem ti : table.getSelection()){
	    	indexList.add(index);
	        dataList.add((IThreadInfo)ti.getData());
	        index++;
	    }
	    int[] ret = new int[indexList.size()];
	    Iterator<Integer> iter = indexList.iterator();
	    for (int i=0; iter.hasNext(); i++) {
	        ret[i] = iter.next();
	    }
	    table.remove(ret);
	    queue.remove(dataList);
	}

	/**
	 * 打开
	 */
	private void openSelect(){
		for (TableItem t : table.getSelection()) {
			if(t instanceof TableBase){
				TableBase tb = (TableBase)t;
				ContextController.open(tb.getData().getDataStatus());
			}
		}
	}

    /**
     * Bind data
     * @param data
     */
    private void bind(ThreadQueue data){
        if(data.equals(this.queue)){
            return;
        }

		LOG.debug("bind:ReBind");
		if(this.queue != null){
		    this.queue.deleRefreshable(this);
		}
        
        this.queue = data;
        this.queue.addRefreshable(this);
        
        table.clearAll();
    }

	/**
	 * Show queue
	 * @param album 专辑
	 */
	public void show(ThreadQueue queue){
    	LOG.debug("show:START");
        bind(queue);
        refresh();
		LOG.debug("show:END");
	}

	/**
     * 移除勾选
     */
    public void removeChecked(){
    	List<IThreadInfo> dataList = new ArrayList<>();
        List<Integer> indexList = new ArrayList<>();
        int index = 0;
        for (TableItem ti : table.getItems()){
            if(ti.getChecked()){
                indexList.add(index);
                dataList.add((IThreadInfo)ti.getData());
            }
            index++;
        }
        int[] ret = new int[indexList.size()];
        Iterator<Integer> iter = indexList.iterator();
        for (int i=0; iter.hasNext(); i++) {
            ret[i] = iter.next();
        }
        table.remove(ret);
        queue.remove(dataList);
    }
    
    /**
     * 移除完成
     */
    public void removeComplete(){
        List<Integer> indexList = new ArrayList<>();
        int index = 0;
        for (TableItem ti : table.getItems()){
            if(ti instanceof TableBase){
                TableBase tb = (TableBase)ti;
                if(queue.getType() == QueueType.RESOLVE 
                		&& DataStatus.isResolve(tb.getData().getDataStatus())){
                		indexList.add(index);
                }else if(queue.getType() == QueueType.DOWN 
                		&& DataStatus.isDown(tb.getData().getDataStatus())){
                	indexList.add(index);
                }
            }
            index++;
        }
        int[] ret = new int[indexList.size()];
        Iterator<Integer> iter = indexList.iterator();
        for (int i=0; iter.hasNext(); i++) {
            ret[i] = iter.next();
        }
        table.remove(ret);
        queue.removeComplete();
    }
    
    /**
     * 重试错误
     */
    public void retryError(){
    	queue.retryError();
    }
    
/*IRefreshable*/
    
    @Override
    public void refresh() {
    	if(queue == null){
            return;
        }
        int tc = table.getItemCount();
        int qc = queue.getHistorySize();
        int j = qc - tc;//队列变化数量
        if(j > 0){
            for (int i = tc; i < qc; i++){
                IThreadInfo thread = queue.getHistoryItem(i);
                new TableBase(table, SWT.CHECK).show(thread, i);
            }
        }else if(j < 0){
        	//无需
        }
    }
    
/*ISelectSub*/
    
    @Override
    public void selectAll(boolean b) {
        for (TableItem ti : table.getItems()){
            ti.setChecked(b);
        }
    }

    @Override
    public void selectSnti() {
        for (TableItem ti : table.getItems()){
            ti.setChecked(!ti.getChecked());
        }
    }
    
/*Adapter*/
    
    /**
	 * 菜单/按钮
	 * @author xiaoyao9184
	 * @version 2.0
	 */
	private class MenuSelectAdapter extends SelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent e) {
			Widget w = (Widget) e.getSource();
			Commandos c =(Commandos) w.getData("com");
			
			switch (c){
			case select:
				checkSelect();
				break;
			case remove:
				removeSelect();
				break;
			case open:
				openSelect();
				break;
			case retry:
				retrySelect();
				break;
			}
		}
	}

}
