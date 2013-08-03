package com.endcoming.gesture.db.table;

import android.net.Uri;

import com.endcoming.gesture.db.DataProvider;

/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  Y 
 * email bellyuan.yuan@gmail.com
 * @date  [2013-8-3]
 */
public class DiyGestureTable {

	public static final String TABLE_NAME = "diygesture"; // 表名

	public static final Uri CONTENT_URI = Uri.parse("content://" + DataProvider.AUTHORITY + "/" + TABLE_NAME);
	public static final String ID = "mid"; // 手势id,唯一标识符
	public static final String NAME = "name"; // 手势名称，用于view中显示手势名称
	public static final String INTENT = "intent"; // 手势intent，用于起程序
	public static final String TYPE = "itemtype"; // itemtype

	/**
	 * 表语句
	 */
	public static final String CREATE_TABLE_SQL = "create table " + TABLE_NAME + "(" + ID + " numeric, " + NAME + " text, " + INTENT + " text, " + TYPE
			+ " numeric" + ")";
}
