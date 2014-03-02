package com.kld.myexpenses.database;

import com.kld.myexpenses.utils.Utils;


public class TTable
{
	  private long id;
	  private long timestamp;
	  private String name;
	  private String description;
	  private String pwd;
	  private int limit;

	  
	  public long getId() { return this.id; }
	  public void setId(long id) { this.id = id; }
	  
	  public long getTimestamp () { return this.timestamp; } 
	  public void setTimestamp (long timestamp) { this.timestamp = timestamp; }
	  
	  public String getName() { return this.name;  }
	  public void setName(String tableName) { this.name = tableName; }
	   
	  public String getDescription() { return this.description;  }
	  public void setDescription(String desc) { this.description = desc; }

	  public String getPassword() { return this.pwd;  }
	  public void setPassword(String password) { this.pwd = password; }
	  
	  public int getLimit() { return this.limit; }
	  public void setLimit(int limit) { this.limit = limit; }
	  // Will be used by the ArrayAdapter in the ListView
	  
	  @Override
	  public String toString() 
	  {
		  return this.name + "(" + this.description + ")" ;
	  }

	  public TTable (String tableName)
	  {
		  if (tableName.trim().length() == 0)
			  tableName = "Unnamed";
		  
		  setTimestamp(System.currentTimeMillis());
		  setName(tableName);
		  setDescription(Utils.STRING_EMPTY);
		  setPassword(Utils.STRING_EMPTY);
		  setLimit(0);
	  }


	  public TTable (String tableName, int limit, String desc)
	  {
		  if (tableName.trim().length() == 0)
			  tableName = "Unnamed";
		  
		  setTimestamp(System.currentTimeMillis());
		  setDescription(desc);
		  setName(tableName);
		  setPassword(Utils.STRING_EMPTY);
		  setLimit(limit);
	  }

	  public TTable (String tableName, int limit)
	  {
		  if (tableName.trim().length() == 0)
			  tableName = "Unnamed";
		  
		  setTimestamp(System.currentTimeMillis());
		  setName(tableName);
		  setDescription(Utils.STRING_EMPTY);
		  setPassword(Utils.STRING_EMPTY);
		  setLimit(limit);
	  }

	  public TTable (String tableName, int limit, String desc, String pwd)
	  {
		  if (tableName.trim().length() == 0)
			  tableName = "Unnamed";
		  
		  setTimestamp(System.currentTimeMillis());
		  setDescription(desc);
		  setName(tableName);
		  setPassword(pwd);
		  setLimit(limit);
	  }
}
