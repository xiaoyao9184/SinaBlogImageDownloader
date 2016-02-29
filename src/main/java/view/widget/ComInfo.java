package view.widget;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import controller.core.ContextController;

import model.interfaces.IRefreshable;
import model.interfaces.IDataStatus;
import model.interfaces.IWidgetController;

/**
 * 详细信息
 * @author xiaoyao9184
 * @version 2.0
 */
public class ComInfo extends Composite implements IRefreshable {
	
	private static final Logger LOG = Logger.getLogger(ComInfo.class);
	
/*GUI*/
	private Link link;
	private Label lblpb;
	private ComImageList cImageList;
	
/*DATA*/
	private IDataStatus data;
	private IWidgetController controller;
	
/*Widget*/
	
	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public ComInfo(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new GridLayout(1, false));
		
		GridData casGridData = new GridData(GridData.FILL_BOTH);
		casGridData.widthHint = 100;
		casGridData.horizontalSpan = 1;
		
		link = new Link(this, SWT.NONE);
		link.setText("左侧选择显示的信息");
		link.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		link.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ContextController.open(link.getToolTipText());
			}
		});
		
		lblpb = new Label(this, SWT.NONE);
		lblpb.setText("信息详情");
		lblpb.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		
		cImageList = new ComImageList(this, SWT.NONE);
		cImageList.setLayoutData(casGridData);
	}

	@Override
	protected void checkSubclass() {}

/*Other*/
	
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
		cImageList.show(data);
		LOG.debug("show:END");
	}

/*IRefreshable*/

	@Override
	public void refresh() {
		link.setText(controller.getLinkText(data));
		link.setToolTipText(controller.getLink(data));
		lblpb.setText(controller.getSubCountText(data));
	}
	
}
