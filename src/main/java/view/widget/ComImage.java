package view.widget;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;

import controller.core.ContextController;

import model.interfaces.IRefreshable;
import model.interfaces.IShowImage;
import model.interfaces.IDataStatus;
import model.interfaces.IWidgetController;

import view.FrmEdit;

/**
 * 图片预览
 * @author xiaoyao9184
 * @version 2.0
 */
public class ComImage extends Composite implements IRefreshable, IShowImage{
	//按钮命令
	public enum Commandos{
		goEdit,
		select,
		removeCache
	} 
	
	private static final Logger LOG = Logger.getLogger(ComImage.class);
    private final int IMAGE_SIZE = 200;//缩放后的图片最长的边长为定值。
    
/*GUI*/
    private CLabel clblImage;
    private Button btnChk;
    private Label lblName;
    private Image image;
    private Menu menu;
    private MenuItem mEdit;
    
/*DATA*/
    private String title;
    private IDataStatus data;
    private IWidgetController controller;
    
/*Widget*/
    
    /**
     * Create the composite.
     * @param parent
     * @param style
     */
    public ComImage(Composite parent, int style) {
        super(parent, style);
        createContents(); 
    }
    
    /**
     * Create Contents
     */
    protected void createContents() {
    	GridData data = new GridData ();
        data.widthHint = 200;
        data.heightHint = 200;
        this.setLayoutData(data);
        
        GridLayout thisLayout = new GridLayout();
        this.setLayout(thisLayout);
        {   
            GridData cLabelImageGridData = new GridData();
            cLabelImageGridData.horizontalAlignment = GridData.FILL;
            cLabelImageGridData.verticalAlignment = GridData.FILL;
            cLabelImageGridData.grabExcessVerticalSpace = true;
            cLabelImageGridData.grabExcessHorizontalSpace = true;
            clblImage = new CLabel(this, SWT.NONE);
            clblImage.setLayoutData(cLabelImageGridData);
            clblImage.setText("...");
            clblImage.addPaintListener(new PaintListener() {
                public void paintControl(PaintEvent evt) {
                    drawImage(evt);
                }
            });
        }
        {
            GridData gd_lblName = new GridData();
            gd_lblName.widthHint = 438;
            gd_lblName.horizontalAlignment = GridData.CENTER;
            lblName = new Label(this, SWT.NONE);
            lblName.setLayoutData(gd_lblName);
            lblName.setText("...");
        }
        {
            GridData gd_btnChk = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
            gd_btnChk.widthHint = 438;
            btnChk = new Button(this, SWT.CHECK);
            btnChk.setText("...");
            btnChk.setLayoutData(gd_btnChk);
            btnChk.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    setSelect(btnChk.getSelection());
                }
            });
        }
        this.layout();
        
        
        //下文菜单 
    	MenuSelectAdapter msa = new MenuSelectAdapter();
        menu = new Menu(this);
        menu.addMenuListener(new MenuListener() {
			
			@Override
			public void menuShown(MenuEvent e) {
				if(!ComImage.this.controller.isHaveSub(ComImage.this.data)){
					ComImage.this.mEdit.setEnabled(false);
	        	}
			}
			
			@Override
			public void menuHidden(MenuEvent e) {
			}
		});
        
        mEdit = new MenuItem(menu, SWT.NONE);
        mEdit.setText("编辑");
        mEdit.setData("com", Commandos.goEdit);
        mEdit.addSelectionListener(msa);
        
        MenuItem mSelect = new MenuItem(menu, SWT.NONE);
        mSelect.setText("选中");
        mSelect.setData("com", Commandos.select);
        mSelect.addSelectionListener(msa);
        
        MenuItem mReLoad = new MenuItem(menu, SWT.NONE);
		mReLoad.setText("重新加载图片");
		mReLoad.setData("com", Commandos.removeCache);
		mReLoad.addSelectionListener(msa);
		
		this.setMenu(menu);
		
    }   
    
    @Override
    protected void checkSubclass() {}
    
    @Override  
    public void dispose() {
        controller.removeRefreshMapping(data, this);
        LOG.debug("dispose");
        
        if(null !=image && !image.isDisposed()){
            image.dispose();
        }   
        if(null !=clblImage && !clblImage.isDisposed()){
            clblImage.dispose();
        }   
        if(null !=lblName && !lblName.isDisposed()){
            lblName.dispose();
        }   
        if(null !=btnChk && !btnChk.isDisposed()){
            btnChk.dispose();
        } 
        super.dispose();
    }
    
    @Override  
    public void addMouseListener(MouseListener listener) {
        super.addMouseListener(listener);
        //Event Delivery
        clblImage.addMouseListener(listener);
        lblName.addMouseListener(listener);
    }
    
	@Override
    public void setMenu(Menu menu) {
    	super.setMenu(menu);
        //Setter Delivery
        clblImage.setMenu(menu);
        lblName.setMenu(menu);
    }

    @Override
	public void setToolTipText(String string) {
		super.setToolTipText(string);
        //Setter Delivery
		clblImage.setToolTipText(string);
	}

/*Getter&Setter*/
    
    /**
     * 
     * @return
     */
    public boolean getSelect() {
        return controller.getChecked(data);
    }

    /**
     * 
     * @param b
     */
    public void setSelect(boolean b) {
        btnChk.setSelection(b);
        controller.setChecked(data, b);
    }
    
/*Other*/

    /**
     * 
     * @param evt
     */
    private void drawImage(PaintEvent evt) {
        if(image==null){
            return;
        }
        //实际尺寸
        Rectangle bounds = image.getBounds();
        int imageWidth = bounds.width;
        int imageHeight = bounds.height;
    
        //缩放尺寸
        int thumbWidth = IMAGE_SIZE;
        int thumbHeight = IMAGE_SIZE;
    
        //实际尺寸 < 缩放尺寸
        if (imageWidth < IMAGE_SIZE && 
                imageHeight < IMAGE_SIZE) {
            thumbWidth = imageWidth;
            thumbHeight = imageHeight;
        } else{
            //实际尺寸比例
            double ratio = (double)imageWidth/(double)imageHeight;
        
            //根据比例调整缩放比例
            if(ratio > 1){   
                thumbHeight = (int)(thumbWidth/ratio);
            }else {   
                thumbWidth = (int)(thumbHeight*ratio);
            }
        }

        evt.gc.drawImage(
                image, 
                0, 0, 
                bounds.width, bounds.height, 
                0, 0, 
                thumbWidth, thumbHeight);  
    }
    
    
    /**
     * Open Edit
     */
    private void goEdit(){
    	if(controller.isHaveSub(data)){
    		FrmEdit edit = new FrmEdit();
            edit.show(data);
    	}
    }
    
    /**
     * Remove cache image
     */
    private void removeCache() {
        //重新加载预览：需要清楚url图片缓存
    	controller.asyncLoadImage(data, this, false);
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
        this.title = controller.getText(data);
        this.controller.addRefreshMapping(data, this);
        this.controller.asyncLoadImage(data, this, true);
        
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

/*IShowImage*/
    
    @Override
    public Display getDisplay() {
        return super.getDisplay();
    }

    @Override
    public void showImage(Image image){
        this.image = image;
        if(image == null){
            clblImage.setText("Image error");
            return;
        }
        refresh();
    }
    
/*IRefreshable*/
    
    @Override
    public void refresh() {
        btnChk.setText(title);
        btnChk.setSelection(controller.getChecked(data));
        lblName.setText(controller.getSubCountText(data));
        lblName.setToolTipText(lblName.getText());
        clblImage.redraw();
    }

/*Adapter*/
    
    /**
     * 双击弹出编辑
     * @author xiaoyao9184
     * @version 2.0
     */
    public static class ComImageMouseAdapter extends MouseAdapter {
        private ComImage ci = null;
        public ComImageMouseAdapter(ComImage ci){
            this.ci = ci;
        }
        @Override
        public void mouseDoubleClick(MouseEvent e) {
        	ci.goEdit();
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
	   			case goEdit:
	   				goEdit();
	   				break;
	   			case select:
	   				setSelect(true);
	   				break;
	   			case removeCache:
	   				removeCache();
	   				break;
   			}
   		}
   		
   	}

}
