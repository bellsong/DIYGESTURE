package com.endcoming.gesture.db.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.app.LauncherActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.view.View;

/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  Y 
 * email bellyuan.yuan@gmail.com
 * @date  [2013-10-12]
 */
public class DeviceUtils {
	//硬件加速
	public static final int LAYER_TYPE_NONE = 0x00000000;
	public static final int LAYER_TYPE_SOFTWARE = 0x00000001;
	public static final int LAYER_TYPE_HARDWARE = 0x00000002;

	private static Method smAcceleratedMethod = null;

	/**
	 * 设置硬件加速
	 * 
	 * @param view
	 * @param mode {@link #LAYER_TYPE_NONE}, 
	 * {@link #LAYER_TYPE_SOFTWARE} or
	 * {@link #LAYER_TYPE_HARDWARE}
	 */
	public static void setHardwareAccelerated(View view, int mode) {
		// honeycomb 以上版本才有硬件加速功能
		if (Build.VERSION.SDK_INT >= 11) {
			try {
				if (null == smAcceleratedMethod) {
					smAcceleratedMethod = View.class.getMethod("setLayerType", new Class[] {
							Integer.TYPE, Paint.class });
				}
				smAcceleratedMethod.invoke(view, new Object[] { Integer.valueOf(mode), null });
			} catch (Exception e) {
			}
		}
	}

	/**
	 * 判断是否平板
	 * @param context
	 * @return
	 */
	public static boolean isTablet(Context context) {
		int layout = context.getResources().getConfiguration().screenLayout;
		return (layout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
	}
	/**
	 * 判断是否支持拨打电话的方法
	 * wangzhuobin
	 * @param context
	 * @return
	 */
	public static boolean isSupportPhoneCall(Context context) {
		boolean result = false;
		if (context != null) {
			TelephonyManager telephonyManager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			if (telephonyManager != null
					&& telephonyManager.getPhoneType() != TelephonyManager.PHONE_TYPE_NONE) {
				result = true;
			}
		}
		return result;
	}

	/**
	 * <br>功能简述:获取版本号
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context
	 * @return
	 */
	public static String getVersionName(Context context) {
		String version = "unknown";
		try {
			PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(),
					PackageManager.GET_ACTIVITIES);
			if (info != null) {
				version = "" + info.versionName;
			}
		} catch (Exception e) {
		}
		return version;
	}

	public static String getVersionCode(Context context) {
		String version = "unknown";
		try {
			PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(),
					PackageManager.GET_ACTIVITIES);
			if (info != null) {
				version = "" + info.versionCode;
			}
		} catch (Exception e) {
		}
		return version;
	}
	
	/**
	 * <br>功能简述:
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param view
	 */
	public static final void setStatusBarTransparent(View view) {
		if (view == null) {
			return;
		}
		
		Integer state = getStatusBarMethodString(view);
		if (state != null){
			view.setSystemUiVisibility(state);
		}
	}
	
	public static final Integer getStatusBarMethodString(View view) {
		if (view == null || view.getContext()== null) {
			return null;
		}
		Context context = view.getContext();
		String[] systemSharedLibraryNames = context.getPackageManager().getSystemSharedLibraryNames();
		 String fieldName = null; 
        for (String lib : systemSharedLibraryNames) {
        	 if ("touchwiz".equals(lib)) {
        		 fieldName = "SYSTEM_UI_FLAG_TRANSPARENT_BACKGROUND";
        	 }
        	 else if (lib.startsWith("com.sonyericsson.navigationbar")) {
        		 fieldName = "SYSTEM_UI_FLAG_TRANSPARENT";
        	 }
        	 else if (lib.startsWith("com.htc.")) {
        		 //TODO HTC的透明设置方式暂时没有找到，先不做
        	 }
        }
        
        if (fieldName != null) {
			try {
				Field field = View.class.getField(fieldName);
				if (field != null) {
					Class<?> type = field.getType();
					if (type == int.class) {
						int value = field.getInt(null);
						return value;
					}
				}
			} catch (Exception e) {
			}
        }
        return null;
	}
	
}
