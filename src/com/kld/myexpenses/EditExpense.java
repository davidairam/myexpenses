package com.kld.myexpenses;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.kld.myexpenses.database.TExpense;
import com.kld.myexpenses.database.TExpensesDAO;
import com.kld.myexpenses.utils.TDatePickerDialog;
import com.kld.myexpenses.utils.TTimePickerDialog;

public class EditExpense extends Activity
{

	long expenseId;
	String tableName;

	TExpense currentExpense;
	TExpensesDAO ds;
	boolean forceClose;
	
	// Views
	EditText txDescription, txCost, txDate, txTime;
	ImageView imgPhoto;
	Button btnSelPhoto, btnDelPhoto;
	
	// Date/time formats
	SimpleDateFormat sfdDate = new SimpleDateFormat("EEEE dd MMMM yyyy");
	SimpleDateFormat sfdTime = new SimpleDateFormat("HH:mm");

	
	Context ctx;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_edit_expense);
		ctx = this;

		// Getting params...
		expenseId = getIntent().getLongExtra("EXPENSE_ID", -1);
		tableName = getIntent().getStringExtra("TABLE_NAME");

		// Checking if params are OK
		forceClose = (expenseId == -1 || tableName.length() == 0);

		// Aborting if any params are bad
		if (forceClose)
			return;

		// Opening datasource.
		ds = new TExpensesDAO(ctx, tableName);
		try { ds.open(true); }
		catch (Exception e) 
		{ 
			forceClose = true;
			Toast.makeText(ctx, "Error trying to open database", Toast.LENGTH_SHORT).show();
			return;
		}
		
		// Mapping views.
		mappingViews();
		
		// Loading selected expense data
		loadData();
		
		// Setting the twatcher events
		txCost.addTextChangedListener(onTextChangedEvent);
		txDescription.addTextChangedListener(onTextChangedEvent);
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();

		// Aborting...
		if (forceClose)
			finish();
		
	}
	
	private void mappingViews()
	{
		txDate = (EditText) findViewById(R.id.txDateEdit);
		txTime = (EditText) findViewById(R.id.txTimeEdit);
		txDescription = (EditText) findViewById(R.id.txDescEdit);
		txCost = (EditText) findViewById(R.id.txCostEdit);
		imgPhoto = (ImageView) findViewById(R.id.imgPhotoEdit);
		btnDelPhoto = (Button) findViewById(R.id.btnDelPhotoEdit);
		btnSelPhoto = (Button) findViewById(R.id.btnSelPhotoEdit);
		
		
		txDate.setOnClickListener(onClickBtnDateTimeEvent);
		txTime.setOnClickListener(onClickBtnDateTimeEvent);
	}

	private TextWatcher onTextChangedEvent = new TextWatcher()
	{
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count)
		{
			if (s.toString().equalsIgnoreCase(txDescription.getText().toString())) 
			{
				currentExpense.setDescription(txDescription.getText().toString());
			}
			else if (s.toString().equalsIgnoreCase(txCost.getText().toString()))
			{
				
				double auxCost = currentExpense.getCost();
				
				try { auxCost = Double.parseDouble(txCost.getText().toString()); }
				catch (Exception e) { 
					Toast.makeText(ctx, "El formato del importe es incorrecto.", Toast.LENGTH_SHORT).show();
					Log.e("david", "fail parsing the double cost -> "  + e.getMessage());
					return;
				}
				
				currentExpense.setCost(auxCost);
			}
				
		}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after)
		{
		}
		
		@Override
		public void afterTextChanged(Editable s)
		{
		}
	};

	
	private View.OnClickListener onClickBtnDateTimeEvent = new View.OnClickListener()
	{
		
		@Override
		public void onClick(View v)
		{
			Calendar auxCal = Calendar.getInstance();
			auxCal.setTimeInMillis(currentExpense.getTimestamp());

			if (v.getId() == R.id.txDateEdit)
			{
				TDatePickerDialog datePickDlg = new TDatePickerDialog(ctx, onDateSetted,"", "Cancel", auxCal.get(Calendar.YEAR), auxCal.get(Calendar.MONTH), auxCal.get(Calendar.DAY_OF_MONTH));
				datePickDlg.show();
			}
			else if (v.getId() == R.id.txTimeEdit)
			{
				TTimePickerDialog timePickDlg = new TTimePickerDialog(ctx, onTimeSetted, "", "Cancel", auxCal.get(Calendar.HOUR_OF_DAY) , auxCal.get(Calendar.MINUTE), true);
				timePickDlg.show();
			}
			
		}
	};
	
	private DatePickerDialog.OnDateSetListener onDateSetted = new OnDateSetListener()
	{
		
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth)
		{
			Calendar auxCal = Calendar.getInstance();
			auxCal.setTimeInMillis(currentExpense.getTimestamp());
			auxCal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			auxCal.set(Calendar.MONTH, monthOfYear);
			auxCal.set(Calendar.YEAR, year);
			currentExpense.setTimestamp(auxCal.getTimeInMillis());
			refreshDateTime();
		}
	};
	
	private TimePickerDialog.OnTimeSetListener onTimeSetted = new OnTimeSetListener()
	{
		
		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute)
		{
			Calendar auxCal = Calendar.getInstance();
			auxCal.setTimeInMillis(currentExpense.getTimestamp());
			auxCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
			auxCal.set(Calendar.MINUTE, minute);
			currentExpense.setTimestamp(auxCal.getTimeInMillis());
			refreshDateTime();
		}
	};
	

	private void refreshDateTime() {
		txDate.setText(sfdDate.format(new Date(currentExpense.getTimestamp())));
		txTime.setText(sfdTime.format(new Date(currentExpense.getTimestamp())));
	}
	
	
	private void loadData()
	{

		currentExpense = ds.getExpenseById(expenseId);
		
		if (currentExpense == null)
		{
			Toast.makeText(ctx, "Error: The selected expense has not been found.", Toast.LENGTH_SHORT).show();
			forceClose = true;
			return;
		}
		
		// Loading data...
		refreshDateTime();
		
		txDescription.setText(currentExpense.getDescription());
		txCost.setText(String.format("%.2f", currentExpense.getCost()));
		String photoAttachmentPath = currentExpense.getAttachment().trim();
		
		if (photoAttachmentPath.length() == 0)
			return;
		
		File f = new File(photoAttachmentPath);
		if (!f.exists())
			return;
		
		Bitmap bmpPhotoAttach = BitmapFactory.decodeFile(photoAttachmentPath);
		
		if (bmpPhotoAttach == null)
			return;
		
		imgPhoto.setImageBitmap(bmpPhotoAttach);
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		ds.updateExpense(currentExpense);
		getMenuInflater().inflate(R.menu.menu_edit_expenses, menu);
		return true;
	}
	
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.action_btnEditOK:
				if (updateExpense()) {
					Toast.makeText(ctx, "Modificación con éxito!", Toast.LENGTH_SHORT).show();
					setResult(RESULT_OK);
					ds.close();
					finish();
				}
				else
					Toast.makeText(ctx, "Error al modificar :(", Toast.LENGTH_SHORT).show();
			break;
			
			case R.id.action_btnEditCancel:
				setResult(RESULT_CANCELED);
				finish();
			break;
			
		}
		return super.onOptionsItemSelected(item);
	}
	private boolean updateExpense()
	{
		return ds.updateExpense(currentExpense);
		
	}
}
	
