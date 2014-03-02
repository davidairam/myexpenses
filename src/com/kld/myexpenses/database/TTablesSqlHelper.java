package com.kld.myexpenses.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class TTablesSqlHelper extends SQLiteOpenHelper
{
	  public static final String TABLE_NAME = "myexpenses_tables";
	  
	  public static final String COLUMN_ID            = "_id";
	  public static final String COLUMN_TIMESTAMP     = "timestamp";
	  public static final String COLUMN_NAME          = "name";
	  public static final String COLUMN_LIMIT         = "limite";
	  public static final String COLUMN_DESCRIPTION   = "desc";
	  public static final String COLUMN_PWD           = "pwd";
	  
	  public static final int COLUMN_INDEX_ID           = 0;
	  public static final int COLUMN_INDEX_TIMESTAMP    = 1;
	  public static final int COLUMN_INDEX_NAME         = 2;
	  public static final int COLUMN_INDEX_DESCRIPTION  = 3;
	  public static final int COLUMN_INDEX_PWD          = 4;
	  public static final int COLUMN_INDEX_LIMIT        = 5;

	  public static final String[] ALL_COLUMNS = { COLUMN_ID, COLUMN_TIMESTAMP, COLUMN_NAME, COLUMN_DESCRIPTION, COLUMN_PWD, COLUMN_LIMIT };

	  // Database creation sql statement
	  private static final String DATABASE_CREATE = "CREATE TABLE IF NOT EXISTS "
	      + TABLE_NAME + "(" 
		  + COLUMN_ID + " integer PRIMARY KEY AUTOINCREMENT, " 
	      + COLUMN_TIMESTAMP + " integer NOT NULL, "
	      + COLUMN_NAME + " text NOT NULL UNIQUE, "
	      + COLUMN_LIMIT + " integer NOT NULL, "
	      + COLUMN_DESCRIPTION + " text, "
	      + COLUMN_PWD + " text);";

	  public TTablesSqlHelper(Context context) 
	  {
	    super(context, DBUtils.DATABASE_NAME, null, DBUtils.DATABASE_VERSION);
	  }
	  
	  @Override
	public void onOpen(SQLiteDatabase db)
	{
		super.onOpen(db);
		
		onCreate(db);
	}

	  @Override
	  public void onCreate(SQLiteDatabase database) 
	  {
		  Log.d("DAVID", "on create --> " + DATABASE_CREATE);
	    database.execSQL(DATABASE_CREATE);
	  }

	  @Override
	  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	    Log.w(TTablesSqlHelper.class.getName(),
	        "Upgrading database from version " + oldVersion + " to "
	            + newVersion + ", which will destroy all old data");
	    db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
	    onCreate(db);
	  }

}