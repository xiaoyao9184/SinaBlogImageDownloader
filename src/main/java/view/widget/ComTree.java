package view.widget;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import controller.collections.ThreadQueue.QueueType;
import controller.core.ContextController;

import model.interfaces.IDataStatus.DataStatus;
import model.interfaces.IRefreshable;
import model.interfaces.ISelectSub;
import model.interfaces.IDataStatus;
import model.interfaces.IWidgetController;

/**
 * 树状页
 * @author xiaoyao9184
 * @version 2.0
 */
public class ComTree extends Composite implements IRefreshable, ISelectSub {
	//按钮命令
	private enum Commandos{
		select,
		resolve,
		resetResolve,
		resetDown
	} 
	
	private static final Logger LOG = Logger.getLogger(ComTree.class);
    
/*GUI*/
    private Tree tree;
    private ComInfo cinfo;
    private Menu menu;
    
/*DATA*/
    private IDataStatus data;
    private IWidgetController controller;
    
/*Widget*/
    
    /**
     * Create the composite.
     * @param parent
     * @param style
     */
    public ComTree(Composite parent, int style) {
        super(parent, style);
        this.setLayout(new GridLayout(1, false));
        
        GridData treeGridData = new GridData(GridData.FILL_BOTH);
        treeGridData.widthHint = 100;
        treeGridData.horizontalSpan = 1;
        
        tree = new Tree(this, SWT.BORDER | SWT.CHECK | SWT.VIRTUAL | SWT.MULTI);
        tree.addTreeListener(new ComTreeTreeListener());
        tree.addSelectionListener(new ComTreeSelectionAdapter());
        tree.setLayoutData(treeGridData);
        
        
        //下文菜单 
        MenuSelectAdapter msa = new MenuSelectAdapter();
        menu = new Menu(tree);
        
        MenuItem mResolve = new MenuItem(menu, SWT.NONE);
		mResolve.setText("解析");
		mResolve.setData("com", Commandos.resolve);
		mResolve.addSelectionListener(msa);
		
        MenuItem mSelect = new MenuItem(menu, SWT.NONE);
		mSelect.setText("选中");
		mSelect.setData("com", Commandos.select);
		mSelect.addSelectionListener(msa);
        
		MenuItem mResetResolve = new MenuItem(menu, SWT.NONE);
		mResetResolve.setText("重置状态为未解析");
		mResetResolve.setData("com", Commandos.resetResolve);
		mResetResolve.addSelectionListener(msa);
		
		MenuItem mResetDown = new MenuItem(menu, SWT.NONE);
		mResetDown.setText("重置状态为未下载");
		mResetDown.setData("com", Commandos.resetDown);
		mResetDown.addSelectionListener(msa);
		
		tree.setMenu(menu);
    }
    
    @Override  
    public void dispose() {
    	controller.removeRefreshMapping(data, this);
        LOG.debug("dispose");
        
        for (TreeItem ti : tree.getItems()){
            TreeBase t = (TreeBase) ti;
            t.dispose();
        }
        tree.dispose();
        super.dispose();
    }

/*Getter&Setter*/
    
    /**
     * 设置显示信息控件
     * @param cinfo
     */
    public void setInfo(ComInfo cominfo){
        this.cinfo = cominfo;
    }
    
/*Other*/
    
    /**
	 * 简单解析
	 */
    private void selectResolve() {
    	for (TreeItem t : tree.getSelection()) {
			TreeBase treeBase = (TreeBase)t;
            IDataStatus data = treeBase.getData();
            if (!DataStatus.isResolve(data)){
                ContextController.getDefaultIShowStatusable().showStatus(QueueType.NONE,"简单解析:" + data.getName());
                LOG.debug("selectResolve:简单解析:" + data.getName());
                data.setIgnore(false);
                ContextController.asyncResolve(data);
            }
		}
	}

	/**
	 * 勾选
	 */
	private void selectCheck(){
		for (TreeItem t : tree.getSelection()) {
			t.setChecked(true);
		}
	}

	/**
	 * 重置状态为未解析:需要删除子集数据
	 */
	private void selectResetResolve() {
		for (TreeItem t : tree.getSelection()) {
			TreeBase tb = (TreeBase)t;
	        IDataStatus data = tb.getData();
	        tb.getController().resetResolve(data);
		}
	}

	/**
	 * 重置状态为未下载:需要删除路径文件缓存
	 */
	private void selectResetDown() {
		for (TreeItem t : tree.getSelection()) {
			TreeBase tb = (TreeBase)t;
	        IDataStatus data = tb.getData();
	        tb.getController().resetDown(data);
		}
	}

	/**
     * Bind data
     * @param data
     */
    private void bind(IDataStatus data){
        if(data.equals(this.data)){
            return;
        }

		LOG.debug("bind:ReBind");
        if(this.controller != null){
            this.controller.removeRefreshMapping(data, this);
        }
        
        this.data = data;
        this.controller = ContextController.getWidgetController(data);
        this.controller.addRefreshMapping(data, this);
        
        if(!DataStatus.isResolve(data)){
        	data.setIgnore(false);
            ContextController.asyncResolve(data);
            return;
        }else{
            refresh();
        }
    }

	/**
     * Show data
     * @param data
     */
    public void show(IDataStatus data){
    	LOG.debug("show:START");
        bind(data);
        //refresh();
		LOG.debug("show:END");
    }

/*IRefreshable*/

	@Override
	public void refresh() {
		ArrayList<IDataStatus> list = controller.getSubList(data);
        if (list != null){
        	if(tree.getItemCount() != list.size()){
        		tree.clearAll(true);
	    		for (IDataStatus subdata : list){
	    			new TreeBase(tree, SWT.NONE).show(subdata);
	            }
        	}
        }
	}

/*ISelectSub*/
	
	@Override
    public void selectAll(boolean b) {
        for (TreeItem ti : tree.getItems()) {
            TreeBase t = (TreeBase) ti;
            t.selectAll(b);
        }
    }
    
    @Override
    public void selectSnti() {
        for (TreeItem ti : tree.getItems()) {
            TreeBase t = (TreeBase) ti;
            t.selectSnti();
        }
    }
    
/*Adapter*/
    
    /**
     * 单击/双击
     * @author xiaoyao9184
     * @version 2.0
     */
    private class ComTreeSelectionAdapter extends SelectionAdapter {
        
        @Override
        public void widgetDefaultSelected(SelectionEvent e) {    
            TreeBase treeBase = (TreeBase)e.item;
            
            LOG.debug("widgetDefaultSelected:显示:" + data.getName());
            IDataStatus data = treeBase.getData();
            cinfo.show(data);
            if (!DataStatus.isResolve(data)){
                ContextController.getDefaultIShowStatusable().showStatus(QueueType.NONE,"简单解析:" + data.getName());
                LOG.debug("widgetDefaultSelected:简单解析:" + data.getName());
                data.setIgnore(false);
                ContextController.asyncResolve(data);
            }
        }
        
        @Override
        public void widgetSelected(SelectionEvent e) {
            TreeBase tree = (TreeBase)e.item;

            //点击复选框处理
            if(e.detail == SWT.CHECK){
            	LOG.debug("widgetSelected:选中");
            	tree.setChecked(tree.getChecked());
                return;
            }

            LOG.debug("widgetSelected:显示:" + data.getName());
            IDataStatus data = tree.getData();
            cinfo.show(data);
        }
    }
    
    /**
     * 展开/合闭
     * @author xiaoyao9184
     * @version 2.0
     */
    private class ComTreeTreeListener implements TreeListener { 
        
        @Override
        public void treeCollapsed(TreeEvent e) {
            TreeItem item = (TreeItem) e.item;
            LOG.debug("treeCollapsed:闭合:" + item);
            // 将该节点的图标设置为关闭状态  
            // item.setImage(ImageFactory.loadImage(tree.getDisplay(),  
            // ImageFactory.TOC_CLOSED));  
        }
        
        @Override
        public void treeExpanded(TreeEvent e) {
            TreeItem item = (TreeItem) e.item;
            LOG.debug("treeExpanded:展开:" + item);
            // item.setImage(ImageFactory.loadImage(tree.getDisplay(),  
            // ImageFactory.TOC_OPEN));     
        }  
    }
       
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
				selectCheck();
				break;
			case resolve:
				selectResolve();
				break;
			case resetResolve:
				selectResetResolve();
				break;
			case resetDown:
				selectResetDown();
				break;
			}
		}
	}
	
}
