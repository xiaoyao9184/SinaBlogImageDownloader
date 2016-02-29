package view;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;

import com.xy.swt.xShell;

import controller.collections.ThreadQueue;
import controller.collections.ThreadQueue.QueueType;

import model.interfaces.IShowStatusable;

import view.widget.ComTable;

/**
 * 列表界面
 * @author xiaoyao9184
 * @version 2.0
 */
public class FrmList implements IShowStatusable{
	
	private static final Logger LOG = Logger.getLogger(FrmList.class);
	
/*GUI*/
	protected Shell shell;
	private Composite composite;
	private Composite compositeStatus;
	private Link likAll;
	private Link likSnti;
	private Link likRemove;
	private Link likRemoveComplete;
	private Link likRetryError;
	private ComTable cTable;
	private Label lblStatus;
	
/*DATA*/
	private ThreadQueue queue;
	
/*Widget*/
	
	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			FrmList window = new FrmList();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		createContents();
		shell.open();
		shell.layout();
	}
	
	/**
	 * Event loop
	 */
	public void closeWait(){
		Display display = Display.getDefault();
		while( !shell.isDisposed() ){
			if( !display.readAndDispatch()){
				display.sleep();
			}
		}
	}
	
	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setSize(600, 375);
		shell.setText("列表");
		shell.setLayout(new GridLayout(1, false));
		xShell.centerShell(shell.getDisplay(), shell);
		shell.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent arg0) {
				//dispose();
				//System.out.println();
			}
		});
		shell.addShellListener(new ShellAdapter() {
			//监听关闭窗口事件
            public void shellClosed(ShellEvent arg0) {
            	shell.setVisible(false);
            	arg0.doit = false;
            	//缓存控件 不关闭
            	//tree.dispose();
            	//info.dispose();
            }
        });
		
		composite = new Composite(shell, SWT.NONE);
		composite.setLayout(new RowLayout(SWT.HORIZONTAL));
		
		likAll = new Link(composite, SWT.NONE);
		likAll.setText("<a>全选</a>");
		likAll.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				cTable.selectAll(true);
			}
		});
		
		likSnti = new Link(composite, SWT.NONE);
		likSnti.setText("<a>反选</a>");
		likSnti.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				cTable.selectSnti();
			}
		});
		
		likRemove = new Link(composite, SWT.NONE);
		likRemove.setText("<a>清空</a>");
		likRemove.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				cTable.removeChecked();
			}
		});
		
		likRemoveComplete = new Link(composite, SWT.NONE);
		likRemoveComplete.setText("<a>清空已完成</a>");
		likRemoveComplete.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				cTable.removeComplete();
			}
		});
		
		likRetryError = new Link(composite, SWT.NONE);
		likRetryError.setText("<a>重试错误</a>");
		likRetryError.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				cTable.retryError();
			}
		});
		
		cTable = new ComTable(shell, 0);
		cTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		compositeStatus = new Composite(shell, SWT.NONE);
		compositeStatus.setLayout(new FillLayout(SWT.HORIZONTAL));
		GridData gd_composite_1 = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		gd_composite_1.heightHint = 21;
		compositeStatus.setLayoutData(gd_composite_1);
		
		lblStatus = new Label(compositeStatus, SWT.SHADOW_NONE);
		lblStatus.setText("状态");
	}
	
	/**
	 * Dispose
	 */
    public void dispose() {
		LOG.debug("dispose");
    	if(shell == null){
    		return;
    	}
    	queue.deleShowStatusable(this);
		LOG.debug("dispose:unBind:" + queue.getName());

		likRetryError.dispose();
		likRemoveComplete.dispose();
    	likRemove.dispose();
    	likSnti.dispose();
    	likAll.dispose();
    	lblStatus.dispose();
    	compositeStatus.dispose();
    	composite.dispose();
    	cTable.dispose();
    }
	
/*Other*/

    /**
     * 设置显示隐藏
     * @param b
     */
	public void setVisible(boolean b) {
		if(shell != null && !shell.isDisposed()){
			shell.setVisible(b);
		}
	}
	
	/**
	 * 显示队列
	 */
	public void show(ThreadQueue threadQueue){
		LOG.debug("show");
		if(shell != null){
			shell.setVisible(true);
			shell.setActive();
			return;
		}
		open();
		switch(threadQueue.getType()){
			case RESOLVE:
				shell.setText("解析列表");
				break;
			case DOWN:
				shell.setText("下载列表");
				break;
			default:
				break;
		}
		
		this.queue = threadQueue;
		this.queue.addShowStatusable(this);
		this.cTable.show(queue);
		showStatistics();
		closeWait();
	}
	
/*IShowStatusable*/

	@Override
	public Widget getWidget() {
		return shell;
	}

	@Override
	public void showStatistics() {
		if(queue.getWorkQueueSize() == 0){
			lblStatus.setText("完成!");
		}else{
			lblStatus.setText("还在继续...");
		}
	}

	@Override
	public void showStatus(QueueType queueType, String msg) {
		lblStatus.setText(msg);
	}

}
