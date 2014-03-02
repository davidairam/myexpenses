package com.kld.myexpenses.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class TExpenseSqlHelper extends SQLiteOpenHelper
{
	  public static String TABLE_EXPENSES = "internal__table";
	  
	  public static final String COLUMN_ID            = "_id";
	  public static final String COLUMN_TIMESTAMP     = "timestamp";
	  public static final String COLUMN_DESCRIPTION   = "desc";
	  public static final String COLUMN_COST          = "cost";
	  public static final String COLUMN_ATTACHMENT    = "attach";
	  
	  public static final int COLUMN_INDEX_ID           = 0;
	  public static final int COLUMN_INDEX_TIMESTAMP    = 1;
	  public static final int COLUMN_INDEX_DESCRIPTION  = 2;
	  public static final int COLUMN_INDEX_COST         = 3;
	  public static final int COLUMN_INDEX_ATTACHMENT   = 4;

	  public static final String[] ALL_COLUMNS = { COLUMN_ID, COLUMN_TIMESTAMP, COLUMN_DESCRIPTION, COLUMN_COST, COLUMN_ATTACHMENT };

	  Context ctx;
	  // Database creation sql statement
	  private static String DATABASE_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS "
	      + TABLE_EXPENSES + "(" 
		  + COLUMN_ID + " integer primary key autoincrement, " 
	      + COLUMN_TIMESTAMP + " integer not null, "
	      + COLUMN_DESCRIPTION + " text, "
	      + COLUMN_COST + " real not null, "
	      + COLUMN_ATTACHMENT + " blob);";


	  // Name of the current table.
	  
	  public TExpenseSqlHelper(Context context, String tableName)
	  {
		  
		  super(context, DBUtils.DATABASE_NAME, null, DBUtils.DATABASE_VERSION);
		  this.ctx = context;
		  DATABASE_TABLE_CREATE  = DATABASE_TABLE_CREATE.replace(TABLE_EXPENSES, tableName);
		  TABLE_EXPENSES = tableName;
		  Log.d("DAVID", "CONSTRUCTOR --> " + DATABASE_TABLE_CREATE);
	  }

	  @Override
	  public void onCreate(SQLiteDatabase database) 
	  {
		  Log.d("DAVID", "ON CREATE --> " + DATABASE_TABLE_CREATE);
	      database.execSQL(DATABASE_TABLE_CREATE);
	  }
	  
	  @Override
	public void onOpen(SQLiteDatabase db)
	{
		Log.d("DAVID", "OEPNING DB!! --> EJECUTING -> " + DATABASE_TABLE_CREATE);
		db.execSQL(DATABASE_TABLE_CREATE);
		super.onOpen(db);
	}

	  
	  @Override
	  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	    Log.w(TExpenseSqlHelper.class.getName(),
	        "Upgrading database from version " + oldVersion + " to "
	            + newVersion + ", which will destroy all old data");
	    db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSES);
	    onCreate(db);
	  }

}