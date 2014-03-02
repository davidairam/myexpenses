package com.kld.myexpenses.database;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class TExpensesDAO
{
	  // Database fields
	  private SQLiteDatabase database;
	  private TExpenseSqlHelper dbHelper;
	  
	  private final String currentTable;
	  
	  public TExpensesDAO(Context context, String tableName) 
	  {
		  currentTable = tableName;
		  dbHelper = new TExpenseSqlHelper(context, tableName);
	  }

	  public void open() { this.open(false); } 
	  public void open(boolean readOnly) throws SQLException 
	  {
		  if (readOnly)
			  database = dbHelper.getReadableDatabase();
		  else
			  database = dbHelper.getWritableDatabase();
	  }
	  
	  public String getTableName()
	  {
		  return currentTable;
	  }

	  public void close() 
	  {
		  if (dbHelper != null)
			  dbHelper.close();
	  }
	  
	  public TExpense createExpense(String desc, double cost, String attachment) { return createExpense(null, desc, cost, attachment); }
	  public TExpense createExpense(Long dateTime, String desc, double cost, String attachment) {
	    
		ContentValues values = new ContentValues();
	    
		if (dateTime == null)
			values.put(TExpenseSqlHelper.COLUMN_TIMESTAMP, (new Date()).getTime());
		else 
			values.put(TExpenseSqlHelper.COLUMN_TIMESTAMP, dateTime);
		
		values.put(TExpenseSqlHelper.COLUMN_DESCRIPTION, desc);
	    values.put(TExpenseSqlHelper.COLUMN_COST, cost);
	    values.put(TExpenseSqlHelper.COLUMN_ATTACHMENT, attachment);
	    
	    	
	    
	    long insertId = database.insert(currentTable, null, values);
	    
	    Cursor cursor = database.query(currentTable,
	        TExpenseSqlHelper.ALL_COLUMNS, TExpenseSqlHelper.COLUMN_ID + " = " + insertId, null,
	        null, null, null);
	    
	    cursor.moveToFirst();
	    TExpense newExpense = cursorToExpense(cursor);
	    cursor.close();
	    return newExpense;
	  }
	  
	  public TExpense getExpenseById(long id)
	  {
		  Log.d("DAVID", " LA TABLA DE ESTA EXPENSE ES --> " + currentTable);
		  Cursor c = database.query(currentTable, TExpenseSqlHelper.ALL_COLUMNS, TExpenseSqlHelper.COLUMN_ID + " = " + id, null, null, null, null);
		  if (!c.moveToFirst() || c.getCount() < 1)
			  return null;
		  
		  return cursorToExpense(c);
	  }
	  

	  public boolean updateExpense(TExpense expense)
	  {
		  long id = expense.getId();

		  ContentValues values = new ContentValues();
		    
		  values.put(TExpenseSqlHelper.COLUMN_DESCRIPTION, expense.getDescription());
		  values.put(TExpenseSqlHelper.COLUMN_COST, expense.getCost());
		  values.put(TExpenseSqlHelper.COLUMN_ATTACHMENT, expense.getAttachment());
		  values.put(TExpenseSqlHelper.COLUMN_TIMESTAMP, expense.getTimestamp());

		  if (database.update(currentTable, values, TExpenseSqlHelper.COLUMN_ID + " = " + id, null) > 0)
		  {
			  Log.i("MyExpenses", "Expense updated with id: " + id);
			  return true;
		  }
		  
		  Log.e("MyExpenses", "Error trying to update expense with id: " + id);
		  return false;
	  }
	  
	  
	  public boolean deleteExpense(TExpense expense) { return deleteExpense(expense, false); }
	  public boolean deleteExpense(TExpense expense, boolean holdAttachment)
	  {
		long id = expense.getId();

		//Deleting the attachment if there is anyone.
		if (!holdAttachment)
			expense.deleteAttachment();
		
	    if (database.delete(currentTable, TExpenseSqlHelper.COLUMN_ID + " = " + id, null) > 0)
	    {
	    	Log.i("MyExpenses", "Expense deleted with id: " + id);
	    	return true;
	    }
	    
	    Log.e("MyExpenses", "Error trying to delete expense with id: " + id);
	    return false;
	  }
	 
	  public List<String> getAllDescriptions() {
		  
		  	List<String> auxList = new ArrayList<String>();
		  
		    
		    String orderBy = TExpenseSqlHelper.COLUMN_TIMESTAMP + " DESC";
		    //String where = String.format("%s BETWEEN %d AND %d", TExpenseSqlHelper.COLUMN_TIMESTAMP, lfrom, lto);
		    String[] cols = { TExpenseSqlHelper.COLUMN_DESCRIPTION };
		    Cursor cursor = database.query(currentTable,
				        cols, null, null, null, null, orderBy);
		    //Cursor cursor = database.rawQuery("SELECT DISTINCT " + TExpenseSqlHelper.COLUMN_DESCRIPTION + " FROM " + currentTable + " ORDER BY " + orderBy, null);
	
		    cursor.moveToFirst();
		    while (!cursor.isAfterLast()) 
		    {
		      //TExpense expense = cursorToExpense(cursor);
		    	
		    	if (!auxList.contains(cursor.getString(0)))
		    		auxList.add(cursor.getString(0));
		      cursor.moveToNext();
		    }
		    
		    // Make sure to close the cursor
		    cursor.close();
		    return auxList;
	  }
	  
	  public List<TExpense> getAllRows() { return getAllRows(null); }
	  public List<TExpense> getAllRows(Calendar month) {
	    List<TExpense> expensesList = new ArrayList<TExpense>();

	    Calendar auxCal = month;
	    if (auxCal == null)
	    	auxCal = Calendar.getInstance();
	    
	    auxCal.set(Calendar.DAY_OF_MONTH, 1);
	    auxCal.set(Calendar.HOUR_OF_DAY, 0);
	    auxCal.set(Calendar.MINUTE, 0);
	    auxCal.set(Calendar.SECOND, 0);
	    auxCal.set(Calendar.MILLISECOND, 0);
	    
	    
	    
	    long lfrom = auxCal.getTimeInMillis();

	    // Calculating lto...
	    auxCal.add(Calendar.MONTH, 1);
	    auxCal.add(Calendar.MILLISECOND, -1);
	    long lto = auxCal.getTimeInMillis();
	    
	    String orderBy = TExpenseSqlHelper.COLUMN_TIMESTAMP + " DESC";
	    String where = String.format("%s BETWEEN %d AND %d", TExpenseSqlHelper.COLUMN_TIMESTAMP, lfrom, lto);
	    
	    Cursor cursor = database.query(currentTable,
			        TExpenseSqlHelper.ALL_COLUMNS, where, null, null, null, orderBy);

	    cursor.moveToFirst();
	    while (!cursor.isAfterLast()) 
	    {
	      TExpense expense = cursorToExpense(cursor);
	      expensesList.add(expense);
	      cursor.moveToNext();
	    }
	    
	    // Make sure to close the cursor
	    cursor.close();
	    return expensesList;
	  }
	  

	  private TExpense cursorToExpense(Cursor cursor) 
	  {
	    TExpense expense = new TExpense();
	    expense.setId(cursor.getLong(TExpenseSqlHelper.COLUMN_INDEX_ID));
	    expense.setTimestamp(cursor.getLong(TExpenseSqlHelper.COLUMN_INDEX_TIMESTAMP));
	    expense.setDescription(cursor.getString(TExpenseSqlHelper.COLUMN_INDEX_DESCRIPTION));
	    expense.setCost(cursor.getDouble(TExpenseSqlHelper.COLUMN_INDEX_COST));
	    expense.setAttachment(cursor.getString(TExpenseSqlHelper.COLUMN_INDEX_ATTACHMENT));
	    return expense;
	  }
	  
	  public double getSum () { return getSum(null); }
	  public double getSum (Calendar cal)
	  {
  		    Calendar auxCal = cal;
		  
		    if (auxCal == null)
		      auxCal = Calendar.getInstance();
		  
		    auxCal.set(Calendar.DAY_OF_MONTH, 1);
		    auxCal.set(Calendar.HOUR_OF_DAY, 0);
		    auxCal.set(Calendar.MINUTE, 0);
		    auxCal.set(Calendar.SECOND, 0);
		    auxCal.set(Calendar.MILLISECOND, 0);
		    
		    long lfrom = auxCal.getTimeInMillis();

		    // Calculating lto...
		    auxCal.add(Calendar.MONTH, 1);
		    auxCal.add(Calendar.MILLISECOND,-1);
		    long lto = auxCal.getTimeInMillis();
		    
		    Cursor cursor  = database.rawQuery("select sum(cost) from " + currentTable + " where " + TExpenseSqlHelper.COLUMN_TIMESTAMP + " BETWEEN " + lfrom + " and " + lto, null);
		    if (!cursor.moveToFirst())
			   return -1;
		  
		  return cursor.getDouble(0);		  
	  }
	  
	  public boolean deleteAll ()
	  {
		  //return (database.delete(currentTable, "1", null) > 0);
		  return false;
	  }
}
