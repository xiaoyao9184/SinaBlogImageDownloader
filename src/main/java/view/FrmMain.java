package view;

import java.util.Arrays;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import com.xiaoyao.io.file.FolderNew;
import com.xy.swt.xShell;

import controller.collections.ThreadQueue;
import controller.collections.ThreadQueue.QueueType;
import controller.core.BlogController;
import controller.core.ContextController;

import model.core.Blog;
import model.core.Photo.DownType;
import model.core.Photo.ReNameStyle;
import model.interfaces.IShowStatusable;
import model.interfaces.IWidgetController;
import model.setting.Setting;

import view.widget.ComImageList;

/**
 * 主界面
 * @author xiaoyao9184
 * @version 2.0
 */
public class FrmMain implements IShowStatusable{

	//按钮命令
	private enum Commandos{
		getAlbum,
		startDown,
		stopDown,
		exit,
		goEdit,
		read,
		save,
		startResolve,
		stopResolve,
		all,
		anti,
		goListResolve,
		goListDown
	}
	
	private static final Setting SETTING = ContextController.getSetting();
	private static final Logger LOG = Logger.getLogger(FrmMain.class);
	
/*GUI*/
	protected Shell shell;
	private ComImageList cImageList;
	private CCombo cobName;
	private MenuItem mID;
	private MenuItem mTitle;
	private MenuItem mNO;
	private MenuItem mPhotoSizeB;
	private MenuItem mPhotoSizeM;
	private MenuItem mPhotoSizeS;
	private MenuItem mBlogPath;
	private ToolItem tltmStatus;
	private ToolItem tltmStatistics;
	private ToolItem tltmStatistics2;
	private FrmList frmList;
	private FrmList frmList2;
	
/*DATA*/
	//主要
	private Blog blog;							//博客
	private IWidgetController controller;		//
	private ThreadQueue resolveQueue;			//解析队列
	private ThreadQueue downQueue;				//下载队列
	//单例
	private static FrmMain instance;  
	private FrmMain (){
		controller = BlogController.getInstance();
		
		resolveQueue = new ThreadQueue(ThreadQueue.QueueType.RESOLVE,
				SETTING.resolveThreadCount);
		resolveQueue.addShowStatusable(this);
	
		downQueue = new ThreadQueue(ThreadQueue.QueueType.DOWN,
				SETTING.downThreadCount);
		downQueue.addShowStatusable(this);
		
		frmList = new FrmList();
		frmList2 = new FrmList();
	}
	public static synchronized FrmMain getInstance() {  
		if (instance == null) {  
			instance = new FrmMain();  
		}  
		return instance;
	}
		
/*Widget*/
	
	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			//ues this cant design in WindowsBuider
			//FrmMain.getInstance().open();
			instance = new FrmMain();
			instance.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			try {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			} catch (Exception e) {
				e.printStackTrace();
				LOG.error("open:Event loop Error", e);
			}
		}
	}
	
	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setSize(706, 598);
		shell.setText("新浪博客图片下载器");
		shell.setLayout(new GridLayout(1, false));
		xShell.centerShell(shell.getDisplay(), shell);
		shell.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent arg0) {
				dispose();
			}
		});
		
		Composite compositeUP = new Composite(shell, SWT.NONE);
		compositeUP.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		compositeUP.setLayout(new GridLayout(5, false));
		
		Label lblName = new Label(compositeUP, SWT.NONE);
		lblName.setText("博客名称");
		
		cobName = new CCombo(compositeUP, SWT.BORDER);
		cobName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		MenuSelectAdapter msa = new MenuSelectAdapter();
		
		Button btnGetAlbum = new Button(compositeUP, SWT.NONE);
		btnGetAlbum.setText("查看相册");
		btnGetAlbum.setData("com", Commandos.getAlbum);
		btnGetAlbum.addSelectionListener(msa);
		
		Button btnResolveInto = new Button(compositeUP, SWT.NONE);
		btnResolveInto.setText("解析所有图片链接");
		btnResolveInto.setData("com", Commandos.startResolve);
		btnResolveInto.addSelectionListener(msa);
		
		Button btnStart = new Button(compositeUP, SWT.NONE);
		btnStart.setText("开始下载");
		btnStart.setData("com", Commandos.startDown);
		btnStart.addSelectionListener(msa);
		
		
		Menu menu = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menu);
		
		MenuItem menuItemFile = new MenuItem(menu, SWT.CASCADE);
		menuItemFile.setText("文件");
		
		Menu mFile = new Menu(menuItemFile);
		menuItemFile.setMenu(mFile);
		
		MenuItem mSave = new MenuItem(mFile, SWT.NONE);
		mSave.setText("保存进度");
		mSave.setData("com", Commandos.save);
		mSave.addSelectionListener(msa);
		
		MenuItem mRead = new MenuItem(mFile, SWT.NONE);
		mRead.setText("读取进度");
		mRead.setData("com", Commandos.read);
		mRead.addSelectionListener(msa);
		
		MenuItem m1 = new MenuItem(mFile, SWT.SEPARATOR);
		m1.setText("-");
		
		MenuItem mExit = new MenuItem(mFile, SWT.NONE);
		mExit.setText("退出");
		mExit.setData("com", Commandos.exit);
		mExit.addSelectionListener(msa);
		
		MenuItem menuItemDo = new MenuItem(menu, SWT.CASCADE);
		menuItemDo.setText("操作");
		
		Menu mDo = new Menu(menuItemDo);
		menuItemDo.setMenu(mDo);
		
		MenuItem mGetA = new MenuItem(mDo, SWT.NONE);
		mGetA.setText("查看相册");
		mGetA.setData("com", Commandos.getAlbum);
		mGetA.addSelectionListener(msa);
		
		MenuItem m2 = new MenuItem(mDo, SWT.SEPARATOR);
		m2.setText("-");
		
		MenuItem mResolveList = new MenuItem(mDo, SWT.NONE);
		mResolveList.setText("解析列表");
		mResolveList.setData("com", Commandos.goListResolve);
		mResolveList.addSelectionListener(msa);
		
		MenuItem mResolveStart = new MenuItem(mDo, SWT.NONE);
		mResolveStart.setText("解析相册");
		mResolveStart.setData("com", Commandos.startResolve);
		mResolveStart.addSelectionListener(msa);
		
		MenuItem mResolveStop = new MenuItem(mDo, SWT.NONE);
		mResolveStop.setText("停止解析");
		mResolveStop.setData("com", Commandos.stopResolve);
		mResolveStop.addSelectionListener(msa);
		
		MenuItem menuItem = new MenuItem(mDo, SWT.SEPARATOR);
		menuItem.setText("-");
		
		MenuItem mDownList = new MenuItem(mDo, SWT.NONE);
		mDownList.setText("下载列表");
		mDownList.setData("com", Commandos.goListDown);
		mDownList.addSelectionListener(msa);
		
		MenuItem mDownStart = new MenuItem(mDo, SWT.NONE);
		mDownStart.setText("开始下载");
		mDownStart.setData("com", Commandos.startDown);
		mDownStart.addSelectionListener(msa);
		
		MenuItem mDownStop = new MenuItem(mDo, SWT.NONE);
		mDownStop.setText("停止下载");
		mDownStop.setData("com", Commandos.stopDown);
		mDownStop.addSelectionListener(msa);
		
		
		SettingSelectAdapter sListener = new SettingSelectAdapter();
		
		MenuItem menuItemEdit = new MenuItem(menu, SWT.CASCADE);
		menuItemEdit.setText("编辑");

		Menu mEdit = new Menu(menuItemEdit);
		menuItemEdit.setMenu(mEdit);
		
		MenuItem mAll = new MenuItem(mEdit, SWT.NONE);
		mAll.setText("全选");
		mAll.setData("com", Commandos.all);
		mAll.addSelectionListener(msa);
		
		MenuItem mAnti = new MenuItem(mEdit, SWT.NONE);
		mAnti.setText("反选");
		mAnti.setData("com", Commandos.anti);
		mAnti.addSelectionListener(msa);
		
		MenuItem m4 = new MenuItem(mEdit, SWT.SEPARATOR);
		m4.setText("-");
		
		MenuItem mBlog = new MenuItem(mEdit, SWT.NONE);
		mBlog.setText("编辑");
		mBlog.setData("com", Commandos.goEdit);
		mBlog.addSelectionListener(msa);
		
		MenuItem m5 = new MenuItem(mEdit, SWT.SEPARATOR);
		m5.setText("-");
		
		mID = new MenuItem(mEdit, SWT.RADIO);
		mID.setText("以图片ID命名");
		mID.addSelectionListener(sListener);
		
		mTitle = new MenuItem(mEdit, SWT.RADIO);
		mTitle.setText("以图片标题命名");
		mTitle.addSelectionListener(sListener);
		
		mNO = new MenuItem(mEdit, SWT.RADIO);
		mNO.setText("以图片编号命名");
		mNO.addSelectionListener(sListener);
		
		MenuItem m6 = new MenuItem(mEdit, SWT.SEPARATOR);
		m6.setText("-");
		
		mBlogPath = new MenuItem(mEdit, SWT.CHECK);
		mBlogPath.setText("博文配图以博文文件夹区分");
		
		MenuItem m3 = new MenuItem(mEdit, SWT.SEPARATOR);
		m3.setText("-");
		mPhotoSizeB = new MenuItem(mEdit, SWT.RADIO);
		mPhotoSizeB.setText("下载大图");
		
		mPhotoSizeM = new MenuItem(mEdit, SWT.RADIO);
		mPhotoSizeM.setText("下载中图");
		
		mPhotoSizeS = new MenuItem(mEdit, SWT.RADIO);
		mPhotoSizeS.setText("下载小图");
		mPhotoSizeS.addSelectionListener(sListener);
		mPhotoSizeM.addSelectionListener(sListener);
		mPhotoSizeB.addSelectionListener(sListener);
		mBlogPath.addSelectionListener(sListener);
		
		cImageList = new ComImageList(shell, SWT.BORDER | SWT.V_SCROLL);
		
		cImageList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		ToolBar toolBar = new ToolBar(shell, SWT.FLAT | SWT.RIGHT);
		toolBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		tltmStatus = new ToolItem(toolBar, SWT.NONE);
		tltmStatus.setText("状态");
		
		tltmStatistics = new ToolItem(toolBar, SWT.NONE);
		tltmStatistics.setText("解析统计");
		tltmStatistics.setData("com", Commandos.goListResolve);
		tltmStatistics.addSelectionListener(msa);
		
		tltmStatistics2 = new ToolItem(toolBar, SWT.NONE);
		tltmStatistics2.setText("下载统计");
		tltmStatistics2.setData("com", Commandos.goListDown);
		tltmStatistics2.addSelectionListener(msa);
		
		showSet();
	}
	
	/**
	 * Dispose
	 */
    public void dispose() {		
		frmList2.setVisible(false);
		frmList.setVisible(false);
		shell.setVisible(false);
		
		syncSet();
		ContextController.saveSetting();
		stopDown();
		stopResolve();
		
		frmList2.dispose();
		frmList.dispose();
		LOG.debug("dispose");
		System.exit(0);
    }
	
/*Getter&Setter*/
	
	public ThreadQueue getResolveQueue() {
		return resolveQueue;
	}
	
	public ThreadQueue getDownQueue() {
		return downQueue;
	}
	
/*Commandos*/
	
	/**
	 * 取得相册
	 */
	private void resolveBase(){
		if (!cobName.getText().equals("")){
			if ( cobName.indexOf(cobName.getText()) == -1 ){
				cobName.add(cobName.getText());
			}
			if (blog == null || !blog.getBlogName().equals(cobName.getText())){
				blog = new Blog(cobName.getText());
			}
			if(resolveQueue == null){
				resolveQueue = new ThreadQueue(ThreadQueue.QueueType.RESOLVE,
						SETTING.resolveThreadCount);
				resolveQueue.addShowStatusable(this);
			}
			cImageList.show(blog);
			showStatus(QueueType.NONE,"获取基本相册数据");
			ContextController.asyncResolve(blog);
		}
	}
	
	/**
	 * 开始解析
	 */
	private void startResolve() {
		if (!cobName.getText().equals("")){
			if ( cobName.indexOf(cobName.getText()) == -1 ){
				cobName.add(cobName.getText());
			}
			if (blog == null){
				blog = new Blog(cobName.getText());
			}
			if(resolveQueue == null){
				resolveQueue = new ThreadQueue(ThreadQueue.QueueType.RESOLVE,
						SETTING.resolveThreadCount);
				resolveQueue.addShowStatusable(this);
			}
			cImageList.show(blog);
			
			if(resolveQueue.isWorking()){
				showStatus(QueueType.NONE,"还在解析中");
			}else if(resolveQueue.isShutdown()){
				resolveQueue.init();
				showStatus(QueueType.NONE,"开始解析");
				ContextController.asyncResolveAll(blog);
			}else{
				showStatus(QueueType.NONE,"开始解析");
				ContextController.asyncResolveAll(blog);
			}
		}
	}
	
	/**
	 * 停止解析
	 */
	private void stopResolve(){
		resolveQueue.shutdown();
	}
	
	/**
	 * 开始下载
	 */
	private void startDown(){
		if(downQueue == null){
			downQueue = new ThreadQueue(ThreadQueue.QueueType.DOWN,
					SETTING.downThreadCount);
		}
		
		if (blog != null){
			if ( blog.getAlbumList() != null ){
				showStatus(QueueType.NONE,"开始下载");
				String basePath = SETTING.downPath;
				if(!FolderNew.createFolder(basePath)){
					showTip("文件路径错误");
					return;
				}
				ContextController.asyncDownAll(blog, basePath);
				return;
			}else{
				showTip("还没有获取到专辑数据，确认专辑已经在界面中显示");
			}
		}else{
			showTip("还没有获取到博客数据，请先输入博客名称点击‘查看相册’");
		}
	}
	
	/**
	 * 停止下载
	 */
	private void stopDown(){
		downQueue.shutdown();
	}
	
	/**
	 * 保存进度
	 */
	private void saveProject() {
		if (blog == null || blog.getAlbumList() == null ){
			showTip("还未获取相册，不能保存");
		}else{
			//文件选择
			FileDialog fDlg = new FileDialog(shell, SWT.SAVE);
			fDlg.setText("保存文件");
			fDlg.setFileName(blog.getBlogName() + ".json");
			fDlg.setFilterPath(SETTING.downPath);
			fDlg.setFilterExtensions(new String[]{"*.json","*.*"});
			String dir = fDlg.open();
			if (dir != null){
				BlogController.save(blog, dir);
				LOG.debug("saveProject:Blog=" + blog.getBlogName());
				showTip("保存成功");
			}
		}
		
	}
	
	/**
	 * 读取进度
	 */
	private void readProject() {
		//文件选择
		FileDialog fDlg = new FileDialog(shell, SWT.OPEN);
		fDlg.setText("选择文件");
		fDlg.setFilterPath(SETTING.downPath);
		fDlg.setFilterExtensions(new String[]{"*.json","*.*"});
		String dir = fDlg.open();

		if (dir != null){
			blog = BlogController.read(dir);
			LOG.debug("readProject:Blog=" + blog.getBlogName());
			cobName.setText( blog.getBlogName() );
			if ( cobName.indexOf(cobName.getText()) == -1 ){
				cobName.add(cobName.getText());
			}
			if ( blog.getAlbumList() != null){
				cImageList.show(blog);
			}
		}
	}
	
	/**
	 * 显示设置
	 */
	private void showSet(){
		LOG.debug("showSet");
		switch (SETTING.downType){
		case small:
			mPhotoSizeS.setSelection(true);
			break;
		case middle:
			mPhotoSizeM.setSelection(true);
			break;
		case big:
			mPhotoSizeB.setSelection(true);
			break;
		default:
			break;
		}
		
		switch (SETTING.reNameStyle){
		case id:
			mID.setSelection(true);
			break;
		case number:
			mNO.setSelection(true);
			break;
		case title:
			mTitle.setSelection(true);
			break;
		default:
			break;
		}
		mBlogPath.setSelection(SETTING.useBlogPath);
		
		cobName.removeAll();
		for (String name : SETTING.blogHistory) {
			cobName.add(name);
		}
		cobName.select(0);
	}
	
	/**
	 * 同步设置
	 */
	private void syncSet(){
		LOG.debug("syncSet");
		if ( mPhotoSizeB.getSelection() ){
			SETTING.downType = DownType.big;
		}else if ( mPhotoSizeM.getSelection() ){
			SETTING.downType = DownType.middle;
		}else if ( mPhotoSizeS.getSelection() ){
			SETTING.downType = DownType.small;
		}
		if ( mID.getSelection() ){
			SETTING.reNameStyle = ReNameStyle.id;
		}else if ( mNO.getSelection() ){
			SETTING.reNameStyle = ReNameStyle.number;
		}else if ( mTitle.getSelection() ){
			SETTING.reNameStyle = ReNameStyle.title;
		}
		SETTING.useBlogPath = mBlogPath.getSelection();
		SETTING.blogHistory = Arrays.asList(cobName.getItems());
	}
	
/*Other*/
	
	/**
	 * 显示提示
	 * @param msg
	 */
	public void showTip(String msg){
		MessageBox mb = new MessageBox(shell,SWT.OK);
		mb.setMessage(msg);
		mb.setText("提示！");
		mb.open();
	}
	
/*IShowStatusable*/
	
	@Override
	public Widget getWidget() {
		return shell;
	}
	
	@Override
	public void showStatistics() {
		if(resolveQueue.isCompleteDown()){
			tltmStatus.setText("解析完成!");
		}else{
			tltmStatus.setText("解析中...");
		}
		if(downQueue.isCompleteDown()){
			tltmStatus.setText("完成!");
		}else{
			tltmStatus.setText("解析中...");
		}
	}

	@Override
	public void showStatus(QueueType queueType, String msg) {
		ToolItem ti = null;
		switch (queueType) {
		case RESOLVE:
			ti = tltmStatistics;
			break;
		case DOWN:
			ti = tltmStatistics2;
			break;
		default:
			ti = tltmStatus;
			break;
		}
		ti.setText(msg);
		ti.setToolTipText(msg);
	}
		
/*Adapter*/
	
	/**
	 * 设置/按钮
	 * @author xiaoyao9184
	 * @version 2.0
	 */
	private class SettingSelectAdapter extends SelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent e) {
			syncSet();
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
			case exit:
				System.exit(0);
				break;
			case save:
				saveProject();
				break;
			case read:
				readProject();
				break;
			case getAlbum:
				resolveBase();
				break;
			case startResolve:
				startResolve();
				break;
			case stopResolve:
				stopResolve();
				break;
			case startDown:
				startDown();
				break;
			case stopDown:
				stopDown();
				break;
			case goEdit:
				if(FrmMain.this.controller.getSubList(FrmMain.this.blog) == null){
					FrmMain.this.showTip("还没有获取到专辑数据，确认专辑已经在界面中显示");
	        		return;
	        	}
	            FrmEdit edit = new FrmEdit();
	            edit.show(FrmMain.this.blog);
				break;
			case goListResolve:
				//显示解析队列
				if(resolveQueue == null){
					resolveQueue = new ThreadQueue(ThreadQueue.QueueType.RESOLVE,
							SETTING.resolveThreadCount);
				}
				frmList.show(resolveQueue);
				//new FrmList().show(resolveQueue);
				break;
			case goListDown:
				//显示下载队列
				if(downQueue == null){
					downQueue = new ThreadQueue(ThreadQueue.QueueType.DOWN,
							SETTING.downThreadCount);
				}
				frmList2.show(downQueue);
				//new FrmList().show(downQueue);
				break;
			case all:
				cImageList.selectAll(true);
				break;
			case anti:
				cImageList.selectSnti();
				break;
			
			}
		}
	}
	
}
