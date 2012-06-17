
	
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

// TODO ��ӡ��޸�ע��
/**
 * <p> ���ݿ��������࣬�ṩ���ݿ�������ܣ��Լ���ײ�����ݲ����� </p>
 *  <p> ��������ر����ݿ⣬�Լ��Ը����CRUD
 *  ��Create, Retrieve, Update, Delete�������� </p>
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
	 * ����ģʽ��Ψһʵ��
	 */
	private static DBAdapter self = null;
	/**
	 * ���ݿ���
	 */
	private static final String DATABASE_NAME = "BabysitterDB.db";
	/**
	 * ���ݿ�汾
	 */
	private static int DATABASE_VERSION = 1;

	/**
	 * ���ݿ���������ʼ��������Ψһʵ����
	 * 
	 * @param context
	 *            ����������
	 */
	public static synchronized void initialize(Context context) {
		if (self == null)
			self = new DBAdapter(context);
	}

	/**
	 * ��������������
	 * 
	 * @return Ψһʵ��
	 */
	public static DBAdapter instance() {
		return self;
	}

	/**
	 * ˽�й��캯������ֱֹ�ӹ��졣
	 * 
	 * @param _context
	 *            ����������
	 */
	private DBAdapter(Context _context) {
		super(_context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public void setDBVersion(int newVersion){
		DATABASE_VERSION = newVersion;
	}

	/**
	 * �����ݿⲻ����ʱ�����Խ��������ݿ⡣
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
	 * ��ԭ�����ݿ�汾���������Ҫ�����ݿ�汾��һ��ʱ���ã�����ԭ�����ݿ⡣
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
	 * ��ȡ���һ�β�����е���Ŀ��ID
	 * 
	 * @param tableName
	 *            Ҫ��ѯ�ı�����
	 * @return ���һ�β�����Ŀ��ID
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
	 * ����������Ŀ�ĸ���������
	 * 
	 * @param tableName
	 *            Ҫ���µ���Ŀ���ڱ�
	 * @param id
	 *            Ҫ������Ŀ��id
	 * @param map
	 *            Ҫ���µ����ݣ��ֶ���-ֵ��ӳ�䣬ʹ��ContentValues�ṹ��
	 */
	public void updateEntry(String tableName, long id, ContentValues values) {
		String[] whereArgs = new String[] { Long.toString(id) };
		int rows = getWritableDatabase().update(tableName, values, "_id =?", whereArgs);
		if(rows == 0)
			Log.d("DBAdapter", "Update not found element.");
	}

	/**
	 * ɾ����Ŀ����������
	 * 
	 * @param tableName
	 *            Ҫɾ������Ŀ���ڱ�
	 * @param where
	 *            �����ֶ�
	 * @param value
	 *            �ֶ�ֵ
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
