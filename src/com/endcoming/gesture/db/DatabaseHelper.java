package com.endcoming.gesture.db;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.endcoming.gesture.db.table.DiyGestureTable;
import com.endcoming.gesture.db.table.FieldType;

/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  Y 
 * email bellyuan.yuan@gmail.com
 * @date  [2013-8-4]
 */
public class DatabaseHelper extends SQLiteOpenHelper {

	private static final String TAG = "DatabaseHelper";
	private static final boolean LOGD = false;

	public static final String DATABASE_NAME = "diy.db";
	private final static int DB_VERSION_ONE = 1;
	private static final int DATABASE_VERSION = 1; //当前数据库版本号

	private final Context mContext;
	private boolean mUpdateResult = true; // 更新数据库结果，默认是成功的。
	private DatabaseResetListener mListener;

	DatabaseHelper(Context context, DatabaseResetListener listener) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		mListener = listener;
		mContext = context;

		SQLiteDatabase db = null;
		try {
			db = getWritableDatabase();
			if (!mUpdateResult) {
				// 更新失败，则删除数据库，再行创建。
				if (db != null) {
					db.close();
				}

				mContext.deleteDatabase(DATABASE_NAME);
				getWritableDatabase();
			}

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			mContext.deleteDatabase(DATABASE_NAME);
		}

	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		if (LOGD) {
			Log.d(TAG, "creating new database");
		}

		mUpdateResult = true;
		db.beginTransaction();
		try {
			// 创建表
			db.execSQL(DiyGestureTable.CREATE_TABLE_SQL);
			
			db.setTransactionSuccessful();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			db.endTransaction();
		}

		if (mListener != null) {
			mListener.onDatabaseReset(db);
		}
		
		sendDatabaseResetNotify();
	}

	/**
	 * {@inheritDoc}
	 * 默认支持向下兼容。（oldVersion = 2, newVersion = 1）
	 * 后期在做版本降级处理时，在此可根据需要做相应处理
	 */
	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		super.onDowngrade(db, oldVersion, newVersion);
		Log.i("DatabaseHelper", "onDowngrade oldVersion=" + oldVersion + ", newVersion=" + newVersion);
		return;
	}

	/**
	 * {@inheritDoc}
	 * 注意，升级后要修改 DATABASE_VERSION +1 
	 * 并注明每个版本升级修改了什么
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion < DB_VERSION_ONE || oldVersion > newVersion
		        || newVersion > DATABASE_VERSION) {
			return;
		}
		
		ArrayList<UpgradeDB> upgradeDBFuncS = new ArrayList<DatabaseHelper.UpgradeDB>();
		
		upgradeDBFuncS.add(new UpgradeDB1To2());

		int verMin = oldVersion - 1; //TODO 为什么要减去1
		int verMax = newVersion - 1;
		for (int i = verMin; i < verMax; i++) {
			mUpdateResult = upgradeDBFuncS.get(i).onUpgradeDB(db);
			if (!mUpdateResult) {
				break; // 中间有任何一次升级失败，则直接返回
			}
		}
		upgradeDBFuncS.clear();
	}
	
	/**
	 * 
	 * <br>类描述: 第一版本升级到第二版本
	 * <br>功能详细描述:
	 * 
	 * @author  Y 
	 * email bellyuan.yuan@gmail.com
	 * @date  [2013-8-4]
	 */
	class UpgradeDB1To2 extends UpgradeDB {
		boolean onUpgradeDB(SQLiteDatabase db) {
			return onUpgrade1To2(db);
		}
	}

	/**
	 * <br>功能简述:
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param db
	 * @return
	 */
	private boolean onUpgrade1To2(SQLiteDatabase db) {
		addColumnToTable(db, DiyGestureTable.TABLE_NAME, DiyGestureTable.NAME,
		        FieldType.TYPE_TEXT, null);
		return true;
	}

	/**
	 * <br>功能简述:新添加字段到表中
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param db
	 * @param tableName 要修改的表名
	 * @param columnName 新增字段名
	 * @param columnType 新增字段类型
	 * @param defaultValue 新增字段默认值。为null，则不提供默认值
	 * for example：
	 * 新增一个默认值为0的整型字段：
	 *	addColumnToTable(db, XXX.TABLE_NAME, columnName, "numeric", "0");
	 */
	private void addColumnToTable(SQLiteDatabase db, String tableName,
	        String columnName, String columnType, String defaultValue) {
		if (!isExistColumnInTable(db, tableName, columnName)) {
			db.beginTransaction();
			try {
				// 增加字段
				String updateSql = "ALTER TABLE " + tableName + " ADD "
				        + columnName + " " + columnType;
				db.execSQL(updateSql);

				// 提供默认值
				if (defaultValue != null) {
					if (columnType.equals(FieldType.TYPE_TEXT)) {
						// 如果是字符串类型，则需加单引号
						defaultValue = "'" + defaultValue + "'";
					}

					updateSql = "update " + tableName + " set " + columnName
					        + " = " + defaultValue;
					db.execSQL(updateSql);
				}

				db.setTransactionSuccessful();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				db.endTransaction();
			}
		}
	}
	
	/**
	 * <br>功能简述:更新某一张表字段
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param db 
	 * @param tableName 要修改的表名
	 * @param columnName 要修改的表字段
	 * @param columnType 要修改的表字段类型
	 * @param value 想修改的值
	 */
	private void updateColumnToTable(SQLiteDatabase db, String tableName, String columnName, String columnType, String value) {
		if (!isExistColumnInTable(db, tableName, columnName)) {
			db.beginTransaction();
			try {
				if (columnType.equals(FieldType.TYPE_TEXT)) {
					// 如果是字符串类型，则需加单引号
					value = "'" + value + "'";
				}

				String updateSql = "update " + tableName + " set " + columnName + " = " + value;
				db.execSQL(updateSql);

				db.setTransactionSuccessful();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				db.endTransaction();
			}
		}
	}
	

	/**
	 * 检查指定的表是否存在
	 * 
	 * @author huyong
	 * @param tableName
	 * @return
	 */
	private boolean isExistTable(final SQLiteDatabase db, String tableName) {
		boolean result = false;
		Cursor cursor = null;
		String where = "type='table' and name='" + tableName + "'";
		try {
			cursor = db.query("sqlite_master", null, where, null, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				result = true;
			}
		} catch (SQLiteException e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return result;
	}

	/**
	 * 检查表中是否存在该字段
	 * 
	 * @author huyong
	 * @param db
	 * @param tableName
	 * @param columnName
	 * @return
	 */
	private boolean isExistColumnInTable(SQLiteDatabase db, String tableName,
	        String columnName) {
		boolean result = false;
		Cursor cursor = null;
		try {
			// 查询列数
			String columns[] = { columnName };
			cursor = db.query(tableName, columns, null, null, null, null, null);
			if (cursor != null && cursor.getColumnIndex(columnName) >= 0) {
				result = true;
			}
		}
		catch (Exception e) {
			Log.i("DatabaseHelper", "isExistColumnInTable has exception");
			e.printStackTrace();
			result = false;
		}
		finally {
			if (null != cursor) {
				cursor.close();
			}
		}

		return result;
	}
	
	/**
	 * <br>功能简述: 将当前数据库中的某字段值拿出来插到另外一个新增字段中做默认值
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param db
	 * @return
	 */
	private boolean changeColumnToAnother(SQLiteDatabase db) {
//		int appCover = 0;
//		String[] appcoverStrings = {SettingIndividualTable.APPCOVER};
//		Cursor cursor = null;
//		try {
//			cursor = db.query(SettingIndividualTable.TABLE_NAME,
//					appcoverStrings, null, null, null, null, null);
//			if (null != cursor) {
//				if (cursor.moveToFirst()) {
//					int appCoverIndex = cursor
//							.getColumnIndex(SettingIndividualTable.APPCOVER);
//					appCover = cursor.getInt(appCoverIndex);
//					cursor.close();
//					cursor = null;
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			if (cursor != null) {
//				cursor.close();
//				cursor = null;
//			}
//		}
//		
//		addColumnToTable(db, SettingIndividualTable.TABLE_NAME,
//				SettingIndividualTable.ICON_MODE_TYPE, TYPE_NUMERIC, String.valueOf(appCover));
		return true;
	}
	
	/**
	 * <br>功能简述:将旧数据搬迁到新的表
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param db
	 * @return
	 */
	private boolean changeOldDataToNew(SQLiteDatabase db) {
//		//把旧的功能表行列数信息搬到新表ResolutionTable
//		String [] columns = {
//				SettingAppTable.ROWNUM, 
//				SettingAppTable.COLNUM, 
//				SettingAppTable.ROWNUM_LANDSCAPE,
//				SettingAppTable.COLNUM_LANDSCAPE
//				};
//		Cursor cursor = db.query(SettingAppTable.TABLE_NAME, columns, null, null, null, null, null);
//		if (cursor != null) {
//			if (cursor.moveToFirst()) {
//				int row = cursor.getInt(0);
//				int col = cursor.getInt(1);
//				int rowLand = cursor.getInt(2);
//				int colLand = cursor.getInt(3);
//				
//				DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
//				int screenWidth = metrics.widthPixels;
//				int screenHeight = metrics.heightPixels;
//				
//				int small = Math.min(screenHeight, screenWidth);
//				int large = Math.max(screenHeight, screenWidth);
//				
//				Resolution resolution = new Resolution(large, small);
//				resolution.mAppCurrentRow = row;
//				resolution.mAppCurrentCol = col;
//				ContentValues contentValues = resolution.toContentValues();
//				
//				Resolution resolutionLand = new Resolution(small, large);
//				resolution.mAppCurrentRow = rowLand;
//				resolution.mAppCurrentCol = colLand;
//				ContentValues contentValuesLand = resolutionLand.toContentValues();
//				
//				db.insert(ResolutionTable.TABLE_NAME, null, contentValues);
//				db.insert(ResolutionTable.TABLE_NAME, null, contentValuesLand);
//			}
//			cursor.close();
//		}
		return true;
	}

	public static String getDBName() {
		return DATABASE_NAME;
	}
	

	/**
	 * 
	 * <br>类描述: 数据库重置，恢复默认回调
	 * <br>功能详细描述:
	 * 
	 * @author  Y 
	 * email bellyuan.yuan@gmail.com
	 * @date  [2013-8-4]
	 */
	interface DatabaseResetListener {
		// 数据库恢复默认后的回调，在此接口进行默认数据的初始化操作
		void onDatabaseReset(SQLiteDatabase db);
	}

	/**
	 * 
	 * <br>类描述:数据库升级接口
	 * <br>功能详细描述:
	 * 
	 * @author  Y 
	 * email bellyuan.yuan@gmail.com
	 * @date  [2013-8-4]
	 */
	abstract class UpgradeDB {
		abstract boolean onUpgradeDB(SQLiteDatabase db);
	}
	
	/**
	 * <br>功能简述:发送数据库reset通知
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void sendDatabaseResetNotify() {
		final ContentResolver resolver = mContext.getContentResolver();
		resolver.notifyChange(DataProvider.DATABASE_RESET_URI, null);
	}
}
