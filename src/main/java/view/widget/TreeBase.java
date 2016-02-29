package view.widget;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.wb.swt.SWTResourceManager;

import controller.core.ContextController;

import model.interfaces.IRefreshable;
import model.interfaces.ISelectSub;
import model.interfaces.IDataStatus;
import model.interfaces.IDataStatus.DataStatus;
import model.interfaces.IWidgetController;

/**
 * 树
 * @author xiaoyao9184
 * @version 2.0
 */
public class TreeBase extends TreeItem implements IRefreshable, ISelectSub{
	
	private static final Logger LOG = Logger.getLogger(TreeBase.class);
	
/*DATA*/
    private IDataStatus data;
    private IWidgetController controller;
    
/*Widget*/
    
    public TreeBase(Tree parent, int style) {
        super(parent, style);
    }
    public TreeBase(TreeItem parentItem, int style) {
        super(parentItem, style);
    }
    
    @Override
    protected void checkSubclass() {}
    
    @Override  
    public void dispose() {
        controller.removeRefreshMapping(data, this);
    	LOG.debug("dispose:index:" + this.getText());
        
        for (TreeItem t : this.getItems()) {
            t.dispose();
        }
        super.dispose();
    }
    
    @Override
    public IDataStatus getData() {
        return data;
    }
    
    @Override
    public void setChecked(boolean checked) {
    	super.setChecked(checked);
        this.setGrayed(false);
        for (TreeItem ti : this.getItems()) {
            ti.setChecked(checked);
        }
        controller.setChecked(data, checked);
    }
    
/*Getter*/
    
    /**
     * Get Controller
     * @return
     */
    public IWidgetController getController() {
		return controller;
	}
    
/*Other*/
    
    /**
     * 设置选中样式
     * @param checked 选中
     * @param grayed 灰色
     */
    public void checkPath(boolean checked, boolean grayed) {
        if (grayed) {
            checked = true;
        } else {
            TreeItem[] items = this.getItems();
            for (TreeItem ti : items) {
                TreeBase child = (TreeBase) ti;
                if (child.getGrayed() || checked != child.getChecked()) {
                    grayed = checked = true;
                    break;
                }
            }
        }
        super.setChecked(checked);
        this.setGrayed(grayed);
        
        TreeBase father = (TreeBase)this.getParentItem();
        if (father == null) return;
        father.checkPath(checked, grayed);
    }
    
    /**
     * Show data
     * @param data
     */
    public void show(IDataStatus data){
        this.data = data;
        this.controller = ContextController.getWidgetController(data);
        this.controller.addRefreshMapping(data, this);
        
        refresh();
    }
    
/*IRefreshable*/
        
    @Override
    public void refresh(){
    	super.setChecked(controller.getChecked(data));
    	checkPath(this.getChecked(), this.getGrayed());
        this.setText(controller.getText(data));
        
        if(DataStatus.isDown(data)){
            this.setForeground(SWTResourceManager.getColor(SWT.COLOR_GREEN));
        }else if(DataStatus.isError(data)){
            this.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
        }else {
            this.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
        }
        
        List<IDataStatus> list = controller.getSubList(data);
        if (list != null){
        	if(this.getItemCount() != list.size()){
        	    this.clearAll(true);
	    		for (IDataStatus subdata : list){
	                new TreeBase(this, SWT.NONE).show(subdata);
	            }
        	}
        }
    }
    
/*ISelectSub*/
    
    @Override
    public void selectAll(boolean b) {
    	this.setChecked(b);
    }

    @Override
    public void selectSnti() {
        throw new UnsupportedOperationException();
    }
    
}
