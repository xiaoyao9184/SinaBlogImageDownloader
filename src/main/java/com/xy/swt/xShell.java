package com.xy.swt;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * shell操作
 * @author xiaoyao9184
 * @version 1.0
 */
public class xShell {
	/**
	 * 居中
	 * @param display
	 * @param shell
	 */
	public static void centerShell(Display display,Shell shell){   
		Rectangle displayBounds = Display.getDefault().getClientArea(); 
        Rectangle shellBounds = shell.getBounds();   
        int x = displayBounds.x + (displayBounds.width - shellBounds.width)>>1;   
        int y = displayBounds.y + (displayBounds.height - shellBounds.height)>>1;   
        shell.setLocation(x, y);   
    } 
	/**
	 * 下居中
	 * @param display
	 * @param shell
	 */
	public static void centerbelowShell(Display display,Shell shell){   
		Rectangle displayBounds = Display.getDefault().getClientArea(); 
        Rectangle shellBounds = shell.getBounds();   
        int x = displayBounds.x + (displayBounds.width - shellBounds.width)>>1;   
        int y = displayBounds.y + (displayBounds.height - shellBounds.height);   
        shell.setLocation(x, y);   
    }
	/**
	 * 上居中
	 * @param display
	 * @param shell
	 */
	public static void centertopShell(Display display,Shell shell){   
		Rectangle displayBounds = Display.getDefault().getClientArea(); 
        Rectangle shellBounds = shell.getBounds();   
        int x = displayBounds.x + (displayBounds.width - shellBounds.width)>>1;  
        int y = 0;   
        shell.setLocation(x, y);   
    }
	
	/**
	 * 居中
	 * @param display
	 * @param shell
	 */
	public static void leftcenterShell(Display display,Shell shell){   
		Rectangle displayBounds = Display.getDefault().getClientArea(); 
        Rectangle shellBounds = shell.getBounds();   
        int x = 0;   
        int y = displayBounds.y + (displayBounds.height - shellBounds.height)>>1;   
        shell.setLocation(x, y);   
    } 
	/**
	 * 居中
	 * @param display
	 * @param shell
	 */
	public static void rightcenterShell(Display display,Shell shell){   
		Rectangle displayBounds = Display.getDefault().getClientArea(); 
        Rectangle shellBounds = shell.getBounds();   
        int x = displayBounds.x + (displayBounds.width - shellBounds.width); 
        int y = displayBounds.y + (displayBounds.height - shellBounds.height)>>1;   
        shell.setLocation(x, y);   
    } 
	/**
	 * 右下角
	 * @param display
	 * @param shell
	 */
	public static void righttopShell(Display display,Shell shell){   
        Rectangle displayBounds = Display.getDefault().getClientArea(); 
        Rectangle shellBounds = shell.getBounds();   
        int x = displayBounds.x + (displayBounds.width - shellBounds.width);   
        int y = 0;   
        shell.setLocation(x, y);   
    } 
	/**
	 * 右下角
	 * @param display
	 * @param shell
	 */
	public static void lefttopShell(Display display,Shell shell){    
        shell.setLocation(0, 0);   
    } 
	
	/**
	 * 右下角
	 * @param display
	 * @param shell
	 */
	public static void rightbelowShell(Display display,Shell shell){   
        Rectangle displayBounds = Display.getDefault().getClientArea(); 
        Rectangle shellBounds = shell.getBounds();   
        int x = displayBounds.x + (displayBounds.width - shellBounds.width);   
        int y = displayBounds.y + (displayBounds.height - shellBounds.height);   
        shell.setLocation(x, y);   
    } 
	/**
	 * 左下角
	 * @param display
	 * @param shell
	 */
	public static void leftbelowShell(Display display,Shell shell){   
        Rectangle displayBounds = Display.getDefault().getClientArea(); 
        Rectangle shellBounds = shell.getBounds();   
        int x = 0;   
        int y = displayBounds.y + (displayBounds.height - shellBounds.height);   
        shell.setLocation(x, y);   
    } 
}
