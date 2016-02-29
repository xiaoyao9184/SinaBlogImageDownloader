package view;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import com.xy.swt.xShell;

import model.interfaces.IDataStatus;

import view.widget.ComInfo;
import view.widget.ComTree;

/**
 * 编辑界面
 * @author xiaoyao9184
 * @version 2.0
 */
public class FrmEdit {

	private static final Logger LOG = Logger.getLogger(FrmEdit.class);

/*GUI*/
	protected Shell shell;
	private ComTree tree;
	private ComInfo info;
	private SashForm sashForm;
	
/*Widget*/
	
	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			FrmEdit window = new FrmEdit();
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
		shell.setSize(840, 463);
		shell.setText("编辑");
		shell.setLayout(new GridLayout(1, false));
		xShell.centerShell(shell.getDisplay(), shell);
		shell.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent arg0) {
				dispose();
			}
		});
		shell.addShellListener(new ShellAdapter() {
			//监听关闭窗口事件
            public void shellClosed(ShellEvent arg0) {
            	shell.setVisible(false);
            	//缓存控件 不关闭
            	//tree.dispose();
            	//info.dispose();
            }
        });
		
		sashForm = new SashForm(shell, SWT.NONE);
			//顺序不能变
			tree = new ComTree(sashForm, 0);
			info = new ComInfo(sashForm, 0);
			tree.setInfo(info);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		sashForm.setSashWidth(2);
		sashForm.setWeights(new int[] {1, 3});
	}
	
	/**
	 * Dispose
	 */
    public void dispose() {
		LOG.debug("dispose");
		
    	tree.dispose();
    	info.dispose();
    	sashForm.dispose();
    }

/*Other*/
	
	/**
	 * 显示专辑内容
	 * @param data
	 */
	public void show(IDataStatus data){
		LOG.debug("show");
		
		open();
		tree.show(data);
		closeWait();
	}
	
}
