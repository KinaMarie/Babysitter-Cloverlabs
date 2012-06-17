
	
package com.singularity.clover.babysitter.database;


import java.util.NoSuchElementException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.singularity.clover.babysitter.Global;
import com.singularity.clover.babysitter.notification.Notification;

// TODO 添加、修改注释
/**
 * <p> 数据库适配器类，提供数据库基础功能，以及最底层的数据操作。 </p>
 *  <p> 包括打开与关闭数据库，以及对各表的CRUD
 *  （Create, Retrieve, Update, Delete）操作。 </p>
 */
public class DBAdapter extends SQLiteOpenHelper {
	
	static{
		try {
			Class.forName("com.singularity.clover.babysitter.notification.Notification");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 单件模式：唯一实例
	 */
	private static DBAdapter self = null;
	/**
	 * 数据库名
	 */
	private static final String DATABASE_NAME = "BabysitterDB.db";
	/**
	 * 数据库版本
	 */
	private static int DATABASE_VERSION = 1;

	/**
	 * 数据库适配器初始化，构造唯一实例。
	 * 
	 * @param context
	 *            程序上下文
	 */
	public static synchronized void initialize(Context context) {
		if (self == null)
			self = new DBAdapter(context);
	}

	/**
	 * 返回适配器对象。
	 * 
	 * @return 唯一实例
	 */
	public static DBAdapter instance() {
		return self;
	}

	/**
	 * 私有构造函数，禁止直接构造。
	 * 
	 * @param _context
	 *            程序上下文
	 */
	private DBAdapter(Context _context) {
		super(_context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public void setDBVersion(int newVersion){
		DATABASE_VERSION = newVersion;
	}

	/**
	 * 当数据库不存在时调用以建立新数据库。
	 * 
	 * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.
	 * 							database.sqlite.SQLiteDatabase)
	 */
	@Override
	public void onCreate(SQLiteDatabase _db) {
		_db.beginTransaction();
		try {	
			_db.execSQL(Notification.TABLE_SCHEMA);
			initializeDBData(_db);
			_db.setTransactionSuccessful();
		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		} finally {
			_db.endTransaction();
		}

	}

	/**
	 * 当原有数据库版本与程序所需要的数据库版本不一致时调用，升级原有数据库。
	 * 
	 * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.
	 * 							database.sqlite.SQLiteDatabase,
	 *      int, int)
	 */
	@Override
	public void onUpgrade(SQLiteDatabase _db, int _oldVersion, int _newVersion) {
		_db.beginTransaction();
		try {
			/*for (Map.Entry<String,Persisable> entry:
						EntityPool.instance().getAllPrototype()){
				_db.execSQL("DROP TABLE IF EXISTS "+entry.getKey());
				_db.execSQL(entry.getValue().getSchema());
			}
			initializeDBData(_db);*/
            _db.setTransactionSuccessful();
        } catch (SQLException e) {
        	throw e;
        } finally {
            _db.endTransaction();
        }
	}

	public long insert(String tableName, 
			ContentValues values) throws SQLException {
		long rowId = Global.ID_INVALID;
		rowId = getWritableDatabase().insertOrThrow(tableName, "null", values);
		return rowId;
	}


	public Cursor retrieveById(String tableName, long id) {
		return getReadableDatabase().rawQuery("SELECT * FROM " + tableName + 
				" WHERE _id=?",new String[] { Long.toString(id) });
	}
	
	public Cursor retrieveAll(String tableName,
			String whereClause,String[] whereArgs) {
		if(whereClause != null)
			return getReadableDatabase().rawQuery(
					"SELECT * FROM " + tableName + " "+whereClause, whereArgs);
		else
			return getReadableDatabase().rawQuery(
					"SELECT * FROM " + tableName, null);
	}

	/**
	 * 获取最近一次插入表中的项目的ID
	 * 
	 * @param tableName
	 *            要查询的表名称
	 * @return 最近一次插入项目的ID
	 */
	public long lastInsertId(String tableName) {
		Cursor cursor = getReadableDatabase().rawQuery(
				"SELECT seq FROM SQLITE_SEQUENCE WHERE name = ?",
				new String[] { tableName });
		long id = Global.ID_INVALID;
		if(cursor.moveToFirst())
			id = cursor.getLong(0);
		cursor.close();
		return id;
	}
	
	
	public void clearSqliteSequence(){
		deleteEntry("SQLITE_SEQUENCE", "1", null);
	}
	/**
	 * 更新数据条目的辅助函数。
	 * 
	 * @param tableName
	 *            要更新的条目所在表
	 * @param id
	 *            要更新条目的id
	 * @param map
	 *            要更新的内容（字段名-值的映射，使用ContentValues结构）
	 */
	public void updateEntry(String tableName, long id, ContentValues values) {
		String[] whereArgs = new String[] { Long.toString(id) };
		int rows = getWritableDatabase().update(tableName, values, "_id =?", whereArgs);
		if(rows == 0)
			Log.d("DBAdapter", "Update not found element.");
	}

	/**
	 * 删除条目辅助函数。
	 * 
	 * @param tableName
	 *            要删除的条目所在表
	 * @param where
	 *            索引字段
	 * @param value
	 *            字段值
	 */
	public void deleteEntry(String tableName, String where, String[] whereArgs) {
		getWritableDatabase().delete(tableName, where, whereArgs);
	}

	public void addColumn(String tableName,String columnName,String columnType) throws SQLException{
		getWritableDatabase().execSQL("ALTER TABLE "+tableName+
										" ADD COLUMN "+columnName+" "+columnType+";");
	}
	
	public Cursor execQuery(String sql, String[] whereArgs) {
		return getReadableDatabase().rawQuery(sql, whereArgs);
	}
	
	public void execSql(String sql, Object[] bindArgs) {
		getWritableDatabase().execSQL(sql, bindArgs);
	}


	public void beginTransaction() {
		getWritableDatabase().beginTransaction();
	}
	
	public void setTransactionSuccessful() {
		getWritableDatabase().setTransactionSuccessful();
	}
	
	public void endTransaction() {
		getWritableDatabase().endTransaction();
	}
	
	private void dropTable(String tag){
		getWritableDatabase().execSQL("DROP TABLE IF EXISTS "+tag);
	}
	
	public void createTableFromDrop(){
		dropTable(Notification.TAG);
		getWritableDatabase().execSQL(Notification.TABLE_SCHEMA);
	}
	
	private void initializeDBData(SQLiteDatabase _db){
		ContentValues contentValues = new ContentValues();
		contentValues.put("create_date", System.currentTimeMillis());
		contentValues.put("trigger_date", System.currentTimeMillis());
		contentValues.put("status",Notification.Status.OFF.name());
		contentValues.put("attribute",Notification.Attribute.RECEIVED.name());
		contentValues.put("notification_type",Notification.NotificationType.RING.name());
		long id = _db.insert(Notification.TAG, null, contentValues);
	}
}
