package com.endcoming.gesture;

import android.app.Application;

import com.endcoming.gesture.db.util.DeviceUtils;
import com.endcoming.gesture.db.util.DrawUtils;

/** 
 * 
 * @author：bellyuan 
 * E-mail: yuanzhibiao@3g.net.cn 
 * @date：2013-10-12 上午1:06:46 
 */
public class MyGestureApplication extends Application{
	
	private static MyGestureApplication sInstance = null;
	
	private static boolean sIsTablet; //是否平板
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		sInstance = this;
		sIsTablet = DeviceUtils.isTablet(this);
	}
	
	/**
	 * 屏幕高度(px)(包含了SystemBar的高度)
	 * 
	 * @return
	 */
	public static int getScreenHeight() {
		if (sIsTablet) {
			return DrawUtils.getTabletScreenHeight(sInstance);
		}
		return DrawUtils.sHeightPixels;
	}

	/**
	 * 实现显示高度(px)(不包含SystemBar的高度)
	 * 
	 * @return
	 */
	public static int getLayoutHeight() {
		return DrawUtils.sHeightPixels;
	}

	/**
	 * 屏幕宽度(px)
	 * 
	 * @return
	 */
	public static int getScreenWidth() {
		if (sIsTablet) {
			return DrawUtils.getTabletScreenWidth(sInstance);
		}
		return DrawUtils.sWidthPixels;
	}

	/**
	 * 实现显示宽度(px)
	 * 
	 * @return
	 */
	public static int getLayoutWidth() {
		return DrawUtils.sWidthPixels;
	}

	// 横竖屏
	public static boolean isPortait() {
		return (getScreenHeight() > getScreenWidth()) ? true : false;
	}

	// 是否平板
	public static boolean isTablet() {
		return sIsTablet;
	}

}
