package com.kld.myexpenses.database;

import java.io.File;

public class TExpense
{
	  private long id;
	  private long timestamp;
	  private String description;
	  private double cost;
	  private String attachment;
	  private int auxPos=-1;

	  public long getId() { return this.id; }
	  public void setId(long id) { this.id = id; }
	  
	  public long getTimestamp () { return this.timestamp; } 
	  public void setTimestamp (long timestamp) { this.timestamp = timestamp; }
	  
	  public double getCost () { return this.cost; } 
	  public void setCost (double cost) { this.cost = cost; }
	  
	  public String getAttachment() { return this.attachment;  }
	  public void setAttachment(String attachment) { this.attachment = attachment; }	  

	  public int getAuxPos () { return this.auxPos; } 
	  public void setAuxPos (int pos) { this.auxPos = pos; }
	  
	  public boolean hasAttachment() 
	  {
		  if (this.getAttachment() == null)
			  return false;
		  
		  String fname = this.getAttachment().trim();
		  if (fname.length() == 0)
			return false;
		
		  File faux = new File(fname);
		  return faux.exists();
	  }
	  
	  public boolean deleteAttachment() 
	  {
		if (!this.hasAttachment())
			return true;
		
		File faux = new File(this.getAttachment().trim());
		return faux.delete();
	  }
	  
	  public String getDescription() { return this.description;  }
	  public void setDescription(String desc) { this.description = desc; }

	  
	  // Will be used by the ArrayAdapter in the ListView
	  
	  @Override
	  public String toString() 
	  {
	    return this.description + " -->  " + this.cost ;
	  }
}
