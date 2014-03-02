package com.kld.myexpenses;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddTableActivity extends Activity
{
	EditText txTable, txDescription, txPassword, txAmount;
	Button btnAdd;
	Context ctx;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		ctx = this;
		
		setContentView(R.layout.layout_add_new_table);
		mappingViews();
	}
	
	private void mappingViews()
	{
		txTable = (EditText) findViewById(R.id.txTableName);
		txDescription = (EditText) findViewById(R.id.txTableDescription);
		txPassword = (EditText) findViewById(R.id.txTablePassword);
		txAmount = (EditText) findViewById(R.id.txTableMaximum);
	}
	
	
	private void returnAndFinish()
	{
		if (txTable == null || txTable.getText().toString().trim().length() == 0)
		{
			Toast.makeText(ctx, "You must to type a valid table name", Toast.LENGTH_SHORT).show();
			return;
		}
		// TODO: CHANGE. WRITE DIRECTLY TO DB INSTEAD OF TRANSFER DATA THROUGH INTENTS.
		Intent intAddTable = new Intent("ADDING_TABLE");
		intAddTable.putExtra("TABLE_NAME", txTable.getText().toString());
		intAddTable.putExtra("TABLE_DESCRIPTION", txDescription.getText().toString());
		intAddTable.putExtra("TABLE_PASSWORD", txPassword.getText().toString());
		intAddTable.putExtra("TABLE_LIMIT", txAmount.getText().toString());
		setResult(Activity.RESULT_OK, intAddTable);
		finish();
	}
	
	
	public void btnAddTableOnClick (View v)
	{
		returnAndFinish();
	}
	
	public void btnCancelTableOnClick (View v)
	{
		setResult(Activity.RESULT_CANCELED);
		finish();
	}
}
