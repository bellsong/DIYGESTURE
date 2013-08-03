package com.endcoming.gesture.db.util;

import android.content.ContentUris;
import android.net.Uri;
import android.text.TextUtils;

/** 
 * 
 * @author：bellyuan 
 * E-mail: yuanzhibiao@3g.net.cn 
 * @date：2013-8-3 下午11:44:34 
 */
public class DBUtil {
	/**
	 * Build a query string that will match any row where the column matches
	 * anything in the values list.
	 */
	public static String buildOrWhereString(String column, int[] values) {
		StringBuilder selectWhere = new StringBuilder();
		for (int i = values.length - 1; i >= 0; i--) {
			selectWhere.append(column).append("=").append(values[i]);
			if (i > 0) {
				selectWhere.append(" OR ");
			}
		}
		return selectWhere.toString();
	}
	
	/**
	 * 
	 * <br>类描述:SQL参数组装器
	 * <br>功能详细描述:
	 * 
	 * @author  Y 
	 * email bellyuan.yuan@gmail.com
	 * @date  [2013-8-4]
	 */
	public static class SqlArguments {
		public final String table;
		public final String where;
		public final String[] args;

		public SqlArguments(Uri url, String where, String[] args) {
			if (url.getPathSegments().size() == 1) {
				this.table = url.getPathSegments().get(0);
				this.where = where;
				this.args = args;
			} else if (url.getPathSegments().size() != 2) {
				throw new IllegalArgumentException("Invalid URI: " + url);
			} else if (!TextUtils.isEmpty(where)) {
				throw new UnsupportedOperationException("WHERE clause not supported: " + url);
			} else {
				this.table = url.getPathSegments().get(0);
				this.where = "_id=" + ContentUris.parseId(url);
				this.args = null;
			}
		}

		public SqlArguments(Uri url) {
			if (url.getPathSegments().size() == 1) {
				table = url.getPathSegments().get(0);
				where = null;
				args = null;
			} else {
				throw new IllegalArgumentException("Invalid URI: " + url);
			}
		}
	}
}
