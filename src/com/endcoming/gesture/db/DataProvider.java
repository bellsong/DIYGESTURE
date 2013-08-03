package com.endcoming.gesture.db;

import com.endcoming.gesture.db.util.DBUtil.SqlArguments;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  Y 
 * email bellyuan.yuan@gmail.com
 * @date  [2013-8-4]
 */
public class DataProvider extends ContentProvider implements DatabaseHelper.DatabaseResetListener {

	public static final String AUTHORITY = "com.endcoming.settings";
	public static final String PARAMETER_NOTIFY = "notify";
	/**
	 * 数据库重新创建的uri
	 */
	public static final Uri DATABASE_RESET_URI = Uri.parse("content://" + AUTHORITY + "/databaseReset");

	private SQLiteOpenHelper mOpenHelper;

	@Override
	public boolean onCreate() {
		mOpenHelper = new DatabaseHelper(getContext(), this);
		return true;
	}

	@Override
	public String getType(Uri uri) {
		SqlArguments args = new SqlArguments(uri, null, null);
		if (TextUtils.isEmpty(args.where)) {
			return "vnd.android.cursor.dir/" + args.table;
		} else {
			return "vnd.android.cursor.item/" + args.table;
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

		SqlArguments args = new SqlArguments(uri, selection, selectionArgs);
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(args.table);

		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		Cursor result = qb.query(db, projection, args.where, args.args, null, null, sortOrder);
		result.setNotificationUri(getContext().getContentResolver(), uri);

		return result;
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		SqlArguments args = new SqlArguments(uri);

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		final long rowId = db.insert(args.table, null, initialValues);
		if (rowId <= 0) {
			return null;
		}

		uri = ContentUris.withAppendedId(uri, rowId);
		sendNotify(uri);

		return uri;
	}

	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {
		SqlArguments args = new SqlArguments(uri);
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			int numValues = values.length;
			for (int i = 0; i < numValues; i++) {
				if (db.insert(args.table, null, values[i]) < 0) {
					return 0;
				}
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}

		sendNotify(uri);
		return values.length;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SqlArguments args = new SqlArguments(uri, selection, selectionArgs);

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count;
		try {
			count = db.delete(args.table, args.where, args.args);
			if (count > 0) {
				sendNotify(uri);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			count = 0;
		}

		return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		SqlArguments args = new SqlArguments(uri, selection, selectionArgs);

		// values为空，通过where传进来需要执行的sql语句
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		if (null == values) {
			int ret = 0;
			String[] arrayStrings = args.where.split("##");
			try {
				db.beginTransaction();
				int len = arrayStrings.length;
				for (int i = 0; i < len; ++i) {
					db.execSQL(arrayStrings[i]);
				}

				db.setTransactionSuccessful();
				ret = 1; // 返回1表示通知成功
			} catch (SQLException e) {
				e.printStackTrace();
				ret = 0;
			} finally {
				db.endTransaction();
			}

			return ret;
		}

		int count = db.update(args.table, values, args.where, args.args);
		if (count > 0) {
			sendNotify(uri);
		}

		return count;
	}

	private void sendNotify(Uri uri) {
		String notify = uri.getQueryParameter(PARAMETER_NOTIFY);
		if (notify == null || "true".equals(notify)) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
	}

	@Override
	public void onDatabaseReset(SQLiteDatabase db) {
		// TODO Auto-generated method stub

	}
}
