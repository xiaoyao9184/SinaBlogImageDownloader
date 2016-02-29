package view.widget;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import controller.core.ContextController;

import model.interfaces.IRefreshable;
import model.interfaces.IThreadInfo;
import model.interfaces.IDataStatus;
import model.interfaces.IWidgetController;

/**
 * 表元素
 * @author xiaoyao9184
 * @version 2.0
 */
public class TableBase extends TableItem implements IRefreshable{
	
	private static final Logger LOG = Logger.getLogger(TableBase.class);
    
/*DATA*/
    private IThreadInfo thread;
    private int index = 0;
    private IDataStatus data;
    private IWidgetController controller;
    
/*Widget*/
    
    public TableBase(Table parent, int style) {
        super(parent, style);
    }
    
    @Override
    protected void checkSubclass() {}
    
    @Override  
    public void dispose() {
    	controller.removeRefreshMapping(data, this);
    	LOG.debug("dispose:index:" + index);
        
        super.dispose();
    }
    
    @Override
    public IThreadInfo getData(){
        return thread;
    }
    
/*Other*/
    
    /**
     * Show data
     * @param data
     */
    public void show(IThreadInfo thread, int index){
        this.index = index;
        this.thread = thread;
        this.data = this.thread.getDataStatus();
        this.controller = ContextController.getWidgetController(data);
        this.controller.addRefreshMapping(data,this);
        
        refresh();
    }
    
/*IRefreshable*/
    
    @Override
    public void refresh() {
        this.setText(0, String.valueOf(index + 1));           //NO
        if(data.isIgnore()){
        	this.setText(1, "人为忽略");                      //Status
        }else{
        	this.setText(1, data.getStatus().toString());     //Status
        }
        this.setText(2, data.getName());                      //Name
        this.setText(3, controller.getDataPlaceText(data));   //Data Status
    }
    
}
