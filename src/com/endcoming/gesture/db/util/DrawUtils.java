package com.endcoming.gesture.db.util;

import java.lang.reflect.Method;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.ImageView.ScaleType;

import com.endcoming.gesture.R;

/**
 * 
 * <br>类描述:绘制工具类
 * <br>功能详细描述:
 * 
 * @author  Y 
 * email bellyuan.yuan@gmail.com
 * @date  [2013-10-12]
 */
public class DrawUtils {
	public static float sDensity = 1.0f;
	public static int sWidthPixels;
	public static int sHeightPixels;
	public static float sFontDensity;
	public static int sTouchSlop = 15; // 点击的最大识别距离，超过即认为是移动 //CHECKSTYLE IGNORE
	public static int sDefaultBarHeight = 25; // 默认状态栏高度，目前手机与平板都使用25dp //CHECKSTYLE IGNORE
	
	public static int sGridViewSpacing = 30;

	public static boolean sIsPad = false; // 是否是平板
	public static int sStatusHeight; //平板中底边的状态栏高度
	@SuppressWarnings("rawtypes")
	private static Class sDisplayClass = null;
	private static Method sMethodForWidth = null;
	private static Method sMethodForHeight = null;

	/**
	 * dip/dp转像素
	 * @param dipValue dip或 dp大小
	 * @return 像素值
	 */
	public static int dip2px(float dipVlue) {
		return (int) (dipVlue * sDensity + 0.5f); //CHECKSTYLE IGNORE
	}

	/**
	 * 像素转dip/dp
	 * @param pxValue 像素大小
	 * @return dip值
	 */
	public static int px2dip(float pxValue) {
		final float scale = sDensity;
		return (int) (pxValue / scale + 0.5f); //CHECKSTYLE IGNORE
	}

	/**
	 * sp 转 px
	 * @param spValue sp大小
	 * @return 像素值
	 */
	public static int sp2px(float spValue) {
		final float scale = sDensity;
		return (int) (scale * spValue);
	}

	/**
	 * px转sp
	 * @param pxValue 像素大小
	 * @return sp值
	 */
	public static int px2sp(float pxValue) {
		final float scale = sDensity;
		return (int) (pxValue / scale);
	}

	public static void resetDensity(Context context) {
		if (context != null && null != context.getResources()) {
			DisplayMetrics metrics = context.getResources().getDisplayMetrics();
			sDensity = metrics.density;
			sFontDensity = metrics.scaledDensity;
			sWidthPixels = metrics.widthPixels;
			sHeightPixels = metrics.heightPixels;
			if (DeviceUtils.isTablet(context)) {
				sStatusHeight = getTabletScreenHeight(context) - sHeightPixels;
				sIsPad = true;
			}
			final ViewConfiguration configuration = ViewConfiguration.get(context);
			if (null != configuration) {
				sTouchSlop = configuration.getScaledTouchSlop();
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static int getTabletScreenWidth(Context context) {
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		int width = 0;
		try {
			if (sDisplayClass == null) {
				sDisplayClass = Class.forName("android.view.Display");
			}
			if (sMethodForWidth == null) {
				sMethodForWidth = sDisplayClass.getMethod("getRealWidth");
			}
			width = (Integer) sMethodForWidth.invoke(display);
		} catch (Exception e) {
		}

		//		Rect rect= new Rect();  
		//		((Activity) context).getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);  
		//		int statusbarHeight = height - rect.bottom;
		if (width <= 0) {
			width = sWidthPixels;
		}

		return width;
	}

	@SuppressWarnings("unchecked")
	public static int getTabletScreenHeight(Context context) {
		int height = 0;
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		try {
			if (sDisplayClass == null) {
				sDisplayClass = Class.forName("android.view.Display");
			}
			if (sMethodForHeight == null) {
				sMethodForHeight = sDisplayClass.getMethod("getRealHeight");
			}
			height = (Integer) sMethodForHeight.invoke(display);
		} catch (Exception e) {
		}

		//		Rect rect= new Rect();  
		//		((Activity) context).getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);  
		//		int statusbarHeight = height - rect.bottom;
		if (height <= 0) {
			height = sHeightPixels;
		}

		return height;
	}

	/**
	 * 获取屏幕物理尺寸
	 * @param activity
	 * @return
	 */
	public static double getScreenInches(Activity activity) {
		DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		double x = Math.pow(dm.widthPixels / dm.xdpi, 2);
		double y = Math.pow(dm.heightPixels / dm.ydpi, 2);
		double screenInches = Math.sqrt(x + y);
		Log.v("System.out.print", "Screen inches : " + screenInches);
		return screenInches;
	}

	/**
	 * 获取是否全屏
	 * @return 是否全屏
	 */
	public static boolean getIsFullScreen(Activity activity) {
		boolean ret = false;
		try {
			WindowManager.LayoutParams attrs = activity.getWindow().getAttributes();
			ret = (attrs.flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	/**
	 * <br>功能简述: 将源区域（例如图片）缩放到适配目标区域
	 * <br>功能详细描述:
	 * <br>注意: 不支持 {@link ScaleType#MATRIX} 类型的缩放
	 * @param srcWidth
	 * @param srcHeight
	 * @param dstWidth
	 * @param dstHeight
	 * @param type 缩放类型。其中，
	 * {@link ScaleType#FIT_XY} 是刚好填满；
	 * {@link ScaleType#FIT_START}, {@link ScaleType#FIT_CENTER}, {@link ScaleType#FIT_END} 
	 * 是刚好填满一个方向，另一个方向上不一定能填满，然后在这个方向分别是偏左/上，居中，偏右/下；
	 * {@link ScaleType#CENTER} 是居中，完全不缩放；
	 * {@link ScaleType#CENTER_CROP} 是居中并且刚好填满一个方向，另一个方向填满但可能超出。
	 * {@link ScaleType#CENTER_INSIDE} 是居中并且不超出，可能会缩小但不会放大。
	 * @param outRect 计算得到的结果边界
	 */
	public static void scaleToFit(int srcWidth, int srcHeight, int dstWidth,
			int dstHeight, ScaleType type, Rect outRect) {
		if (type == ScaleType.FIT_XY) {
			outRect.left = 0;
			outRect.top = 0;
			outRect.right = dstWidth;
			outRect.bottom = dstHeight;
		} else if (type == ScaleType.FIT_START
				|| type == ScaleType.FIT_END) {
			if (srcWidth * dstHeight > srcHeight * dstWidth) {
				// 源区域宽度较大时，缩放到目标区域宽度
				int w = dstWidth;
				int h = srcHeight * w / srcWidth;
				outRect.left = 0;
				outRect.right = dstWidth;
				outRect.top = type == ScaleType.FIT_START ? 0 : dstHeight - h;
				outRect.bottom = outRect.top + h;
			} else {
				// 源区域高度较大时，缩放到目标区域高度
				int h = dstHeight;
				int w = srcWidth * h / srcHeight;
				outRect.left = type == ScaleType.FIT_START ? 0 : dstWidth - w;
				outRect.right = outRect.left + w;
				outRect.top = 0;
				outRect.bottom = dstHeight;
			}
		} else {
			// 处理居中绘制的类型
			int w, h;
			if (type == ScaleType.CENTER) {
				w = srcWidth;
				h = srcHeight;
			} else if (type == ScaleType.CENTER_INSIDE) {
				if (srcWidth * dstHeight > srcHeight * dstWidth) {
					// 源区域宽度较大时，如果比目标区域宽度还大就要缩小
					w = Math.min(srcWidth, dstWidth);
					h = srcHeight * w / srcWidth;
				} else {
					// 源区域高度较大时，如果比目标区域高度还大就要缩小
					h = Math.min(srcHeight, dstHeight);
					w = srcWidth * h / srcHeight;
				}
			} else if (type == ScaleType.CENTER_CROP
					|| type == ScaleType.FIT_CENTER) {
				if (srcWidth * dstHeight > srcHeight * dstWidth
						^ type == ScaleType.FIT_CENTER) {
					// 源区域宽度较大且为CROP模式，或者宽度较小且为FIT模式时，缩放到目标高度
					h = dstHeight;
					w = srcWidth * h / srcHeight;
				} else {
					// 源区域高度较大且为CROP模式，或者高度较小且为FIT模式时，缩放到目标宽度
					w = dstWidth;
					h = srcHeight * w / srcWidth;
				}
			} else {
				// 剩下的 MATRIX 类型不支持了
				throw new UnsupportedOperationException(
						"This scale type needs for support.");
			}

			outRect.left = (dstWidth - w) / 2;
			outRect.right = outRect.left + w;
			outRect.top = (dstHeight - h) / 2;
			outRect.bottom = outRect.top + h;
		}

	}
	
	public static int getGridViewSpacing() {
		return dip2px(sGridViewSpacing);
	}

}