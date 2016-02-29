package view.widget;

import java.util.List;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;

import controller.core.ContextController;

import model.interfaces.IRefreshable;
import model.interfaces.ISelectSub;
import model.interfaces.IDataStatus;
import model.interfaces.IWidgetController;

/**
 * 专辑/图片集合
 * @author xiaoyao9184
 * @version 2.0
 */
public class ComImageList extends ScrolledComposite implements IRefreshable, ISelectSub{
	
	private static final Logger LOG = Logger.getLogger(ComImageList.class);
    
/*GUI*/
    private Composite c;
    private List<Composite> cs;
    private List<ComImage> as;
    private RowLayout rowLayout;
    
/*DATA*/
    private IDataStatus data;
    private IWidgetController controller;
    
/*Widget*/
    
    /**
     * Create the composite.
     * @param parent
     * @param style
     */
    public ComImageList(Composite parent, int style) {
        super(parent,  SWT.BORDER | SWT.V_SCROLL);
        
        rowLayout = new RowLayout();
        rowLayout.pack = false;
        rowLayout.wrap = true;
        rowLayout.justify = true;
        rowLayout.type = SWT.HORIZONTAL;
        rowLayout.marginLeft = 0;
        rowLayout.marginRight = 0;
        rowLayout.spacing = 0;
        
        c = new Composite(this, SWT.NONE);
        c.setLayout(rowLayout);
        
        cs = new ArrayList<Composite>();
        as = new ArrayList<ComImage>();
        
        c.addControlListener(new ControlListener() {
            @Override
            public void controlResized(ControlEvent e) {
            	setSize();
            }
            
            @Override
            public void controlMoved(ControlEvent e) {
                
            }
        } );
        this.setAlwaysShowScrollBars(true);
        this.setSize(620, 420);
        this.setExpandHorizontal(true);
        this.setExpandVertical(true);
        this.setMinWidth(350);  
        this.setMinHeight(400); 
        this.setContent(c);
        this.setLayout(new FillLayout());
    }

    @Override  
    public void dispose() {
    	controller.removeRefreshMapping(data, this);
        LOG.debug("dispose");
        
        for (ComImage a : as) {
            a.dispose();
        }
        for (Composite c : cs) {
            c.dispose();
        }
        as.clear();
        cs.clear();
        super.dispose();
    }

	@Override
	public void layout() {
		super.layout();
		c.layout();
	}
    
/*Other*/
    
    /**
     * 添加图像预览
     * @param comImage
     * @return
     */
    private int add(ComImage comImage){
        Composite composite = new Composite(c, SWT.NONE);        
        composite.setLayoutData(new RowData(200, 200));
        composite.setToolTipText("预览边界");
        composite.setVisible(true);
        
        comImage.setParent(composite);
        comImage.setToolTipText("双击编辑");
        comImage.addMouseListener(new ComImage.ComImageMouseAdapter(comImage));
        comImage.setVisible(true);
        comImage.setSize(200,200);
        
        cs.add(composite);
        as.add(comImage);        
        return as.size();
    }

    /**
     * 调整大小
     */
    private void setSize(){
        LOG.debug("setSize:START");
    	
        int row = c.getBounds().width / 200;
        if (row==0){return;}
        int col = as.size() / row;
        if ( (as.size() % row) != 0){col++;}
        int height = col * 200 + 20;
        
    	LOG.debug("setSize:c.getBounds=" + c.getBounds());
        LOG.debug("setSize:this.getBounds=" + this.getBounds());
        LOG.debug("setSize:row=" + row + "-col=" + col + "-height=" + height);
        
        ScrollBar bar = this.getVerticalBar();
        
        double d = (double)bar.getSelection() / bar.getMaximum();
        int index = (int) (d * height);
        bar.setSelection(index);
        bar.setMaximum(height);
        if(this.getBounds().height >= c.getBounds().height){
        	bar.setSelection(0);
        }

        if(this.getMinHeight() != height){
        	this.setMinHeight(height);
        }
        
        LOG.debug("setSize:c.getBounds=" + c.getBounds());
        LOG.debug("setSize:this.getBounds=" + this.getBounds());
        LOG.debug("setSize:this.getMinHeight()=" + this.getMinHeight());

        //使用layout()解决调整大小后不显示；
        //使用asyncExec()解决最大化、还原后显示不正确
        getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				ComImageList.this.layout();
			}
		});

        LOG.debug("setSize:END");
    }
        
    /**
     * Bind data
     * @param data
     * @return
     */
    private boolean bind(IDataStatus data){
        if(data.equals(this.data)){
            return false;
        }
        
        LOG.debug("bind:ReBind");
        if(controller != null){
            controller.removeRefreshMapping(data, this);
        }
        
        this.data = data;
        this.controller = ContextController.getWidgetController(data);
        this.controller.addRefreshMapping(data,this);

        return true;
    }

    /**
     * Show data
     * @param data
     */
    public void show(IDataStatus data){
    	LOG.debug("show:START");
        if(bind(data)){
            refresh();
        }
        LOG.debug("show:END");
    }
    
/*IRefreshable*/

    @Override
    public void refresh() {
        List<IDataStatus> list = controller.getSubImageList(data);
        
        int dc = 0;
        if (list != null){
        	dc = list.size();
        }
        
        int wc = as.size();
        int j = dc - wc;
        
        if(j > 0){
        	for(int i = 0; i < dc; i++){
        		if((i+1) > wc){
        			ComImage a = new ComImage(c, SWT.NONE);
        			a.show(list.get(i));
        			add(a);
        		}else{
        			as.get(i).show(list.get(i));
        		}
        	}
        	setSize();
        }else if(j < 0){
        	for(int i = 0; i < wc; i++){
        		if((i+1) > dc){
        			as.get(i).dispose();
        			cs.get(i).dispose();
        		}else{
        			as.get(i).show(list.get(i));
        		}
        	}
        	as = as.subList(0, dc);
        	cs = cs.subList(0, dc);
        	setSize();
        }else{
        	for(int i = 0; i < wc; i++){
    			as.get(i).show(list.get(i));
        	}
        }
        //this.layout();
    }
    
/*ISelectSub*/
    
    @Override
    public void selectAll(boolean b) {
        for (ComImage a : as) {
            a.setSelect(b);
        }
    }
    
    @Override
    public void selectSnti() {
        for (ComImage a : as) {
            a.setSelect(!a.getSelect());
        }
    }
    
}
