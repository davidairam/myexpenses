package com.kld.myexpenses.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.kld.myexpenses.utils.Utils;

public class TTablesDAO
{
	
	public static final String TAG = "TTablesDAO";
	
	
	// Database fields
	private SQLiteDatabase database;
	private TTablesSqlHelper dbHelper;
	private Context ctx;

	public TTablesDAO(Context context)
	{
		ctx = context;
		dbHelper = new TTablesSqlHelper(context);
	}

	public void open() throws SQLException
	{
		database = dbHelper.getWritableDatabase();
	}

	public void openReadable() throws SQLException
	{
		database = dbHelper.getReadableDatabase();
	}

	public void getAllSysTables ()
	{
		if (database == null || !database.isOpen())
		{
			Log.d("DAVID"," Error db is null or is not open.");
			return;
		}
		
		Cursor c = database.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
		
		Log.d("DAVID"," --> El cursor nos devuelve total de TABLAS de -> " + c.getCount());
		Log.d("DAVID"," --> El cursor nos devuelve total de columnas de -> " + c.getColumnCount());
		int auxeltos;
		while (c.moveToNext())
		{
			auxeltos = -1;
			Cursor c1 = database.rawQuery("SELECT COUNT(*) FROM " + c.getString(0) , null);
			if (c1.moveToNext())
				auxeltos = c1.getInt(0);
			
			Log.d("DAVID", "TABLA -> " + c.getString(0) + "[" + auxeltos + "]");
			/*
			Cursor c2 = database.rawQuery("SELECT * FROM " + c.getString(0), null);

			String sata;
			int idata;
			double fdata;
			boolean omit;
			while (c2.moveToNext())
			{
				for (int i = 0; i < c2.getColumnCount(); i++)
				{
					omit = false;
							idata = -1;
							fdata = -1;
							sata ="";
					switch (c2.getType(i))
					{
						case Cursor.FIELD_TYPE_STRING:
							sata = c2.getString(i);

						break;
						case Cursor.FIELD_TYPE_INTEGER:
							idata = c2.getInt(i);
						break;
						case Cursor.FIELD_TYPE_FLOAT:
							fdata = c2.getDouble(i);
						break;
						default:
							omit=true;
					}
					
					if (omit)
						continue;
					
					Log.d("DAVID", "   --> " + c2.getColumnName(i) + " = " +  sata + " " + idata + " " + fdata);
					
				}
				Log.d("DAVID", "    ");

			}
			*/
			
		}
		
		/*try {
			String bindArgs[] = new String[0];
			
			//database.execSQL("DROP TABLE peterd",bindArgs );
			//database.execSQL("DROP TABLE myexpenses_tables",bindArgs );
		}
		catch (Exception e) { Log.e("DAVID", "TABLA NOT DELETED BECASUE -> " + e.getMessage()); }*/
		
		
	}
	public void close()
	{
		if (dbHelper != null)
			dbHelper.close();
	}

	public TTable add(String name)
	{
		return add(name, Utils.STRING_EMPTY);
	}

	public TTable add(String name, String desc)
	{
		return add(name, 0, desc, Utils.STRING_EMPTY);
	}

	public TTable add(String name, int maxAmount)
	{
		return add(name, maxAmount, Utils.STRING_EMPTY, Utils.STRING_EMPTY);
	}

	public TTable add(String name, int maxAmount, String desc)
	{
		return add(name, maxAmount, desc, Utils.STRING_EMPTY);
	}

	public TTable add(String name, int maxAmount, String desc, String pwd)
	{

		TTable auxTable = new TTable(name, maxAmount, desc, pwd);
		return add(auxTable); 

	}

	public TTable add(TTable table)
	{
		if (table == null)
			return null;

		ContentValues values = new ContentValues();

		values.put(TTablesSqlHelper.COLUMN_TIMESTAMP, table.getTimestamp());
		values.put(TTablesSqlHelper.COLUMN_NAME, table.getName());
		values.put(TTablesSqlHelper.COLUMN_DESCRIPTION, table.getDescription());
		values.put(TTablesSqlHelper.COLUMN_PWD, table.getPassword());
		values.put(TTablesSqlHelper.COLUMN_LIMIT, table.getLimit());

		long insertId = database.insert(TTablesSqlHelper.TABLE_NAME, null,
				values);
		
		if (insertId == -1)
		{
			Log.e("DAVID", "Table NOT ADDED. Duplicated !?");
			return null;
		}

		Cursor cursor = database.query(TTablesSqlHelper.TABLE_NAME,
				TTablesSqlHelper.ALL_COLUMNS, TTablesSqlHelper.COLUMN_ID
						+ " = " + insertId, null, null, null, null);

		cursor.moveToFirst();
		TTable newTable = cursorToTable(cursor);
		return newTable;
	}

	/*
	 * public boolean updateTable(TTable table) { long id = expense.getId();
	 * 
	 * ContentValues values = new ContentValues();
	 * 
	 * values.put(MyExpensesDbHelper.COLUMN_DESCRIPTION,
	 * expense.getDescription()); values.put(MyExpensesDbHelper.COLUMN_COST,
	 * expense.getCost()); values.put(MyExpensesDbHelper.COLUMN_ATTACHMENT,
	 * expense.getAttachment());
	 * 
	 * if (database.update(MyExpensesDbHelper.TABLE_EXPENSES, values,
	 * MyExpensesDbHelper.COLUMN_ID + " = " + id, null) > 0) {
	 * Log.i("MyExpenses", "Expense updated with id: " + id); return true; }
	 * 
	 * Log.e("MyExpenses", "Error trying to update expense with id: " + id);
	 * return false; }
	 */

	public boolean deleteTable(TTable table)
	{
		if (table == null || table.getId() < 0)
			return false;

		long id = table.getId();

		if (database.delete(TTablesSqlHelper.TABLE_NAME,
				TTablesSqlHelper.COLUMN_ID + " = " + id, null) > 0)
		{
			database.execSQL("DROP TABLE IF EXISTS " + table.getName());
			Log.i("MyExpenses", "Table deleted with id: " + id);
			return true;
		}

		Log.e("MyExpenses", "Error trying to delete table with id: " + id);
		return false;
	}

	public static TTable getTableById(Context context, long tableId)
	{
		if (context == null || tableId < 0) {
			Log.e(TAG, "ERROR: Context is null or tableId < 0");
			return null;
		}
		
		TTablesSqlHelper dbHlp;
		SQLiteDatabase db;
		
		try {
			dbHlp = new TTablesSqlHelper(context);
			db = dbHlp.getReadableDatabase();
		}
		catch (Exception e) {
			Log.e(TAG, "ERROR: Problem trying to open database. -> " + e.getMessage());
			return null;
		}
		
		
		Cursor cursor = db.query(TTablesSqlHelper.TABLE_NAME,
				TTablesSqlHelper.ALL_COLUMNS, TTablesSqlHelper.COLUMN_ID + "="
						+ tableId, null, null, null, null);

		TTable result = null;
		if (cursor.moveToFirst())
			result = cursorToTable(cursor);
		
		cursor.close();
		return result;
	}
	
	public TTable getTableByName(String tableName)
	{
		if (tableName == null || tableName.length() == 0)
			return null;
		
		Cursor cursor = database.query(TTablesSqlHelper.TABLE_NAME,
				TTablesSqlHelper.ALL_COLUMNS, TTablesSqlHelper.COLUMN_NAME + " = '"
						+ tableName+"'", null, null, null, null);

		TTable resTable = null;
		if (cursor.moveToFirst())
			resTable = cursorToTable(cursor);
		
		cursor.close();
		return resTable;
	}

	public List<TTable> getAllRows()
	{
		List<TTable> tablesList = new ArrayList<TTable>();

		String orderBy = TTablesSqlHelper.COLUMN_TIMESTAMP + " DESC";

		Cursor cursor = database.query(TTablesSqlHelper.TABLE_NAME,
				TTablesSqlHelper.ALL_COLUMNS, null, null, null, null, orderBy);

		cursor.moveToFirst();
		while (!cursor.isAfterLast())
		{
			TTable table = cursorToTable(cursor);
			tablesList.add(table);
			cursor.moveToNext();
		}

		// Make sure to close the cursor
		cursor.close();
		return tablesList;
	}

	public static boolean existsTable (Context context, String tableName) {
		
		TTablesSqlHelper sqlHelper = new TTablesSqlHelper(context);
		SQLiteDatabase db = sqlHelper.getReadableDatabase();
		
		if (db == null) {
			Log.e(TAG, "ERROR: existsTable() problem trying getting a database.");
			return false;
		}
		
		// Checking
		Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND UPPER(name) = '"+ tableName.toUpperCase() +"'", null); 
		boolean res = (c != null && c.getCount() > 0);
		c.close();
		
		return res;
	}
	public boolean rename (TTable table2Rename, String newName) {
		
		// check if newName exists in our db and in the system
		// if exists return false if not try to rename.
		if (table2Rename == null || newName.trim().length() == 0)
			return false;
		
		if (existsTable(ctx, newName)) {
			Log.d(TAG, "RENAME TABLE: Ya existe una tabla con el nombre " + newName);
			return false;
		}
			
		
		if (database == null || !database.isOpen()) 
		{
			Log.e(TAG, "RENAME TABLE: La instancia de la bd no está operativa.");
			return false;
		}
		
		try {
			Log.d(TAG, "--> " + "ALTER TABLE " + table2Rename.getName() + " RENAME TO " + newName);
			database.execSQL("ALTER TABLE " + table2Rename.getName() + " RENAME TO " + newName);
		} 
		catch (Exception e) 
		{
			if (e.getMessage().contains("no such table")) {
				deleteTable(table2Rename);
				table2Rename.setName(newName);
				if (add(table2Rename).getName().equals(newName)) {
					Log.d(TAG, "RENAME TABLE: Table " + table2Rename.getName() + " renamed correctly to -> " + newName);
					return true;
				}
			}
			else {
				Log.e(TAG, "RENAME TABLE ERROR: SQL syntax error -> " + e.getMessage());
				return false;
			}
		}
		
		if (existsTable(ctx, newName)) {
			Log.d(TAG, "RENAME TABLE: Table " + table2Rename.getName() + " renamed correctly to -> " + newName);
			deleteTable(table2Rename);
			table2Rename.setName(newName);
			if (add(table2Rename).getName().equals(newName)) {
				Log.d(TAG, "RENAME TABLE: Table " + table2Rename.getName() + " renamed correctly to -> " + newName);
				return true;
			}
			else {
				Log.e(TAG, "ERROR: Trying to rename the table: " + table2Rename.getName());
				return false;
			}
		}
		else {
			Log.e(TAG, "ERROR: Trying to rename the table: " + table2Rename.getName());
			return false;
		}
	}
	
	private static TTable cursorToTable(Cursor cursor)
	{
		TTable expense = new TTable(cursor.getString(TTablesSqlHelper.COLUMN_INDEX_NAME));
		expense.setId(cursor.getLong(TTablesSqlHelper.COLUMN_INDEX_ID));
		expense.setTimestamp(cursor.getLong(TTablesSqlHelper.COLUMN_INDEX_TIMESTAMP));
		expense.setLimit(cursor.getInt(TTablesSqlHelper.COLUMN_INDEX_LIMIT));
		expense.setDescription(cursor.getString(TTablesSqlHelper.COLUMN_INDEX_DESCRIPTION));
		expense.setPassword(cursor.getString(TTablesSqlHelper.COLUMN_INDEX_PWD));
		return expense;
	}

}
