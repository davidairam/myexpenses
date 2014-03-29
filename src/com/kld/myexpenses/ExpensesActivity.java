package com.kld.myexpenses;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.kld.myexpenses.database.TExpense;
import com.kld.myexpenses.database.TExpenseSqlHelper;
import com.kld.myexpenses.database.TExpensesDAO;
import com.kld.myexpenses.database.TTable;
import com.kld.myexpenses.database.TTablesDAO;

public class ExpensesActivity extends Activity
{

	private static final int REQ_CODE_TAKE_PICURE = 150;
	private static final int REQ_CODE_EDIT_EXPENSE = 100;
	
	private TExpensesDAO datasource;
	
	EditText txtCost;
	AutoCompleteTextView txtDesc;
	TextView lblResult, lblDifference;
	ImageView btnAdd;
	Menu menuApp;
	
	Context ctx;
	ListView lstView;
	TListViewExpenses_Adapter lAdapter;
	List<TExpense> valuesList = new ArrayList<TExpense>();

	TExpense selItem;

	boolean hideKeyboard=true;
	SimpleDateFormat sfdMonthText;
	
	// This view is a reference to the optionsMenu
	View lastOptionsLayoutVisible;

	/** Current table name */
	String cTableName;

	/**
	 * This is used to abort launch in case of we do not have a valid table
	 * parameter to load
	 */
	boolean abortLaunch = false;

	/** The var below is used to save the current month */
	Calendar currentMonth;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_expenses);
		ctx = this;

		if (getIntent() == null || !getIntent().hasExtra("PARAM_TABLE_NAME"))
		{
			abortLaunch = true;
			return;
		}

		// Getting the current table name to load it.
		cTableName = getIntent().getStringExtra("PARAM_TABLE_NAME");
		if (cTableName.trim().length() == 0)
		{
			abortLaunch = true;
			return;
		}

		// Open table
		datasource = new TExpensesDAO(this, cTableName);
		datasource.open();

		// View mapping and other layout stuff
		mappingViews();
	}

	private void mappingViews()
	{
		getActionBar().setBackgroundDrawable(new ColorDrawable(Color.DKGRAY));
		sfdMonthText = new SimpleDateFormat("MMMM", Locale.getDefault());

		
		txtDesc = (AutoCompleteTextView) findViewById(R.id.txDescription);
		txtCost = (EditText) findViewById(R.id.txCost);
		lblResult = (TextView) findViewById(R.id.lblResult);
		lblDifference = (TextView) findViewById(R.id.lblDifference);
		btnAdd = (ImageView) findViewById(R.id.btnAddNew);
		
		ArrayAdapter<String> descAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, datasource.getAllDescriptions());
		txtDesc.setAdapter(descAdapter);
		
		lstView = (ListView) findViewById(android.R.id.list);
		go2currentMonth();
		lAdapter = new TListViewExpenses_Adapter(ctx,
				R.layout.item_list_expenses, valuesList);

		lstView.setAdapter(lAdapter);

		
		btnAdd.setOnLongClickListener(new View.OnLongClickListener()
		{
			
			@Override
			public boolean onLongClick(View v)
			{
				hideKeyboard = !hideKeyboard;
				btnAdd.setImageResource(hideKeyboard ? R.drawable.add : R.drawable.add_multi);
				Toast.makeText(ctx, "Insercción por Lotes -> " + (hideKeyboard ? "Desactivada" : "Activada"), Toast.LENGTH_SHORT).show();
				return true;
			}
		});
		
		txtCost.setOnEditorActionListener(new OnEditorActionListener()
		{

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event)
			{
				if (actionId == EditorInfo.IME_ACTION_DONE)
				{
					addNewItem();
					return true;
				}
				return false;
			}
		});
	}

	public void onClick(View view)
	{
		switch (view.getId())
		{
			case R.id.btnAddNew:
				addNewItem();
				break;
		}
	}

	private void addNewItem()
	{

		if (txtCost.getText().toString().trim().length() == 0)
		{
			Toast.makeText(ctx, "Error: Not Added. Missing data.",
					Toast.LENGTH_SHORT).show();
			return;
		}

		TExpense expense = null;
		double auxCost;

		try
		{
			auxCost = Double.parseDouble(txtCost.getText().toString());
		} catch (Exception e)
		{
			return;
		}

		String auxDesc = txtDesc.getText().toString().trim();
		if (auxDesc.trim().length() == 0)
			auxDesc = "[No especificado]";
		
		// Checking if we are in the current month or NOT.. if NOT then...
		if (currentMonth != null && (currentMonth.get(Calendar.MONTH) != Calendar.getInstance().get(Calendar.MONTH) ||
				currentMonth.get(Calendar.YEAR) != Calendar.getInstance().get(Calendar.YEAR))) {

			currentMonth.set(Calendar.SECOND, 0);
			currentMonth.set(Calendar.MILLISECOND, 0);

			
			// -- If we are in the previous month we set the day of month to 31 (or maximum). --
			if (currentMonth.getTimeInMillis() < System.currentTimeMillis()) {
				currentMonth.set(Calendar.DAY_OF_MONTH, currentMonth.getActualMaximum(Calendar.DAY_OF_MONTH));
				currentMonth.set(Calendar.HOUR_OF_DAY, 23);
				currentMonth.set(Calendar.MINUTE, 59);
			}
			// -- If we are in the next month we set the day of month to 1. --
			else {
				currentMonth.set(Calendar.DAY_OF_MONTH, 1);
				currentMonth.set(Calendar.HOUR_OF_DAY, 9);
				currentMonth.set(Calendar.MINUTE, 0);
			}
			// Create expense with the month specified.
			expense = datasource.createExpense(currentMonth.getTimeInMillis(),auxDesc, auxCost, "");
			
			// After creating expense we set the day of month to 1 again due to security reasons.
			currentMonth.set(Calendar.DAY_OF_MONTH, 1);
		}
		else {
			// -- Creating expense with the current dateTime. --
			expense = datasource.createExpense(auxDesc, auxCost, "");
		}
		valuesList.add(expense);
		// adapter.add(expense);
		updateResult();
		txtCost.setText("");
		txtDesc.setText("");
		txtDesc.requestFocus();
		Toast.makeText(ctx, "Added!", Toast.LENGTH_SHORT).show();
		Collections.sort(valuesList, new compatatorTExpenses());
		lAdapter.notifyDataSetChanged();

		// hiding optionsnMenu
		hideExpenseOptionsMenu();
		
		// hiding keyboard
		if (!hideKeyboard)
			return;
		
		hideKeyboard();

	}
	
	private void hideKeyboard() {
		if (txtDesc == null)
			return;
		
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(txtDesc.getWindowToken(), 0);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		
		if (abortLaunch)
		{
			Toast.makeText(ctx, "Invalid Table.", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}

		datasource.open();
		updateAB();
	}

	private void updateAB()
	{
		
		// Setting the correct menu item icon.
		Calendar calToday = Calendar.getInstance();
		if (calToday.get(Calendar.MONTH) == currentMonth.get(Calendar.MONTH) && calToday.get(Calendar.YEAR) == currentMonth.get(Calendar.YEAR)) {
			if (menuApp != null)
				menuApp.findItem(R.id.action_currentMonth).setIcon(R.drawable.current_month_disabled);
		}
		else {
			if (menuApp != null)
				menuApp.findItem(R.id.action_currentMonth).setIcon(R.drawable.current_month);
		}
		String auxCad = String.format("%s (%d)",
				sfdMonthText.format(currentMonth.getTime()), valuesList.size());
		auxCad = auxCad.substring(0, 1).toUpperCase() + auxCad.substring(1);
		getActionBar().setTitle(auxCad);
		updateResult();
	}

	@Override
	protected void onPause()
	{
		datasource.close();
		super.onPause();
	}

	private void updateResult()
	{

		double totalSum = datasource.getSum(currentMonth);
		
		// TODO: PONER EL TTABLESDAO EN EL CONCREATE y dejar el object siempre
		// marcado.
		TTablesDAO tablesDAO = new TTablesDAO(ctx);
		tablesDAO.openReadable();
		TTable currTable = tablesDAO.getTableByName(cTableName);
		int limit = 300;
		tablesDAO.close();
		if (currTable != null)
			limit = currTable.getLimit();

		double difference = limit - totalSum;
		Calendar calToday = Calendar.getInstance();
		
		int daysMonth = currentMonth.getActualMaximum(Calendar.DAY_OF_MONTH);
		int currentDay = calToday.get(Calendar.DAY_OF_MONTH);

		// TODO: CHEQUEAR EL PUTO AÑO!
		if (currentMonth.get(Calendar.MONTH) < calToday.get(Calendar.MONTH) && 
			currentMonth.getTimeInMillis() < calToday.getTimeInMillis())
			currentDay = daysMonth+1;
		else if (currentMonth.get(Calendar.MONTH) > calToday.get(Calendar.MONTH) && 
				currentMonth.getTimeInMillis() > calToday.getTimeInMillis()) 
			currentDay = 1;
		
		int leftDays = daysMonth - currentDay + 1;

		lblResult.setText(String.format("%.2f €", totalSum));
		if (difference > 0)
		{
			lblDifference.setText(String.format("+%.2f€  (%d days left)",
					difference, leftDays));
			lblDifference.setTextColor(Color.GREEN);
		} else
		{
			lblDifference.setText(String.format("%.2f€  (%d days left)",
					difference, leftDays));
			lblDifference.setTextColor(Color.RED);
		}
	}
	
	// TODO: PASAR TODO A FUNCIONES ON_CLICK y hacer el delete y el details.

	private class TListViewExpenses_Adapter extends ArrayAdapter<TExpense>
	{

		LayoutInflater vi;
		TExpense currentItem;
		int resId;
		Calendar calToday, calItem, calYesterday;
		SimpleDateFormat sfd;
		LinearLayout lytEdit, mainLayout;

		public TListViewExpenses_Adapter(Context context,
				int textViewResourceId, List<TExpense> objects)
		{
			super(context, textViewResourceId, objects);
			resId = textViewResourceId;
			vi = (LayoutInflater) getContext().getSystemService(
					Context.LAYOUT_INFLATER_SERVICE);
			calToday = Calendar.getInstance();
			calItem = Calendar.getInstance();
			calYesterday = Calendar.getInstance();
			calYesterday.add(Calendar.DAY_OF_YEAR, -1);

			lastOptionsLayoutVisible = null;
			sfd = new SimpleDateFormat("EEE\ndd/MM", Locale.getDefault());
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{

			currentItem = getItem(position);
			currentItem.setAuxPos(position);
			
			if (convertView == null)
			{
				mainLayout = new LinearLayout(getContext());
				vi.inflate(resId, mainLayout, true);
			} else
				mainLayout = (LinearLayout) convertView;

			TextView lblTimestamp = (TextView) mainLayout.findViewById(R.id.lblTimestamp);
			TextView lblDesc = (TextView) mainLayout.findViewById(R.id.lblDescription);
			TextView lblCost = (TextView) mainLayout.findViewById(R.id.lblCost);
			ImageView imgAttachment = (ImageView) mainLayout.findViewById(R.id.imgAttachment);
			RelativeLayout rlyt = (RelativeLayout) mainLayout.findViewById(R.id.lytExpensesItem);
			Button btnEdit = (Button) mainLayout.findViewById(R.id.btnOptionsExpenseEdit);
			Button btnMove = (Button) mainLayout.findViewById(R.id.btnOptionsExpenseMove);
			Button btnDelItem = (Button) mainLayout.findViewById(R.id.btnOptionsExpenseDelete);
			Button btnPhoto = (Button) mainLayout.findViewById(R.id.btnOptionsExpensePicture);
			mainLayout.findViewById(R.id.lytOptionsExpensesItem).setVisibility(View.GONE);
			
			if (lastOptionsLayoutVisible != null && lastOptionsLayoutVisible.getTag() != null) {
				
				try {
					int auxPos = (Integer) lastOptionsLayoutVisible.getTag();
					if (auxPos == position)  {
						Log.d("DAVID", " MOSTRAMOS EL CORRECT");
						mainLayout.findViewById(R.id.lytOptionsExpensesItem).setVisibility(View.VISIBLE);
					}
				}
				catch (Exception e) { e.printStackTrace(); }
			}
			
			rlyt.setTag(position);
			rlyt.setOnClickListener(new View.OnClickListener()
			{

				@Override
				public void onClick(View v)
				{

					lytEdit = (LinearLayout) ((View) v.getParent())
							.findViewById(R.id.lytOptionsExpensesItem);

					if (lytEdit.getVisibility() == View.VISIBLE)
					{
						lytEdit.setVisibility(View.GONE);
						lastOptionsLayoutVisible = null;
					} else
					{
						if (lastOptionsLayoutVisible != null)
							lastOptionsLayoutVisible.setVisibility(View.GONE);

						lytEdit.setVisibility(View.VISIBLE);
						lastOptionsLayoutVisible = lytEdit;
						Log.d("DAVID", " THE POSITION TO SAVE IS --> " + v.getTag());
						lastOptionsLayoutVisible.setTag(v.getTag());
						// hiding the keyboard
						hideKeyboard();
					}
				}
			});

			btnPhoto.setTag(currentItem);
			btnPhoto.setOnClickListener(new View.OnClickListener()
			{
				TExpense item;
				@Override
				public void onClick(View v)
				{
					if (v.getTag() == null)
						return;

					item = (TExpense) v.getTag();
					if (item != null && item.hasAttachment()) {
						AlertDialog.Builder dlgDelPhoto = new Builder(ctx);
						dlgDelPhoto.setMessage("Are you sure to delete the attached picture ?");
						dlgDelPhoto.setNegativeButton("NO!", null);
						dlgDelPhoto.setPositiveButton("Yeah", new DialogInterface.OnClickListener()
						{
							
							@Override
							public void onClick(DialogInterface arg0, int arg1)
							{
								if (deleteAttachedPicture(item))
									Toast.makeText(ctx, "Foto borrada", Toast.LENGTH_SHORT).show();
							}
						});
						dlgDelPhoto.show();

					}
					else
						callTakePhoto(item.getId());
				}
			});
			
			btnEdit.setTag(currentItem);
			btnEdit.setOnClickListener(new View.OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					TExpense auxItem = (TExpense) v.getTag();
					
					Intent iLaunchEditExpense = new Intent(ctx, EditExpense.class);
					iLaunchEditExpense.putExtra("EXPENSE_ID", auxItem.getId());
					iLaunchEditExpense.putExtra("TABLE_NAME", cTableName);
					startActivityForResult(iLaunchEditExpense, REQ_CODE_EDIT_EXPENSE);
				}
			});
			
			btnDelItem.setTag(currentItem);
			btnDelItem.setOnClickListener(new View.OnClickListener()
			{
				TExpense selItem;
				@Override
				public void onClick(View v)
				{
					selItem = (TExpense) v.getTag();
					
					if (selItem == null)
						return;
					
					AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(ctx);
					dlgBuilder.setTitle("DELETE ITEM");
				    dlgBuilder.setMessage("Are you sure to delete [" +	 selItem.getDescription() + " " + selItem.getCost() + "] ?");
					dlgBuilder.setPositiveButton("Yes, I am sure", new OnClickListener()
					{
					  
					    @Override public void onClick(DialogInterface dialog, int which) {
					 
					    	if (datasource.deleteExpense(selItem) && selItem.getAuxPos() >= 0) {
					    		valuesList.remove(selItem.getAuxPos());
						    	lAdapter.notifyDataSetChanged(); 
						    	updateResult(); 
						    	Toast.makeText(ctx,"Deleted", Toast.LENGTH_SHORT).show();
					    		hideExpenseOptionsMenu();
					    	}
					    	else {
					    		Toast.makeText(ctx,"ERROR: Trying to delete item...",Toast.LENGTH_SHORT).show();
					    	}
					    } 
					});
					
					dlgBuilder.setNegativeButton("No!", null);

					// Showing the dialog
					AlertDialog dlgDelete = dlgBuilder.create(); dlgDelete.show();
				}
			});
			btnMove.setTag(currentItem);
			btnMove.setOnClickListener(new View.OnClickListener()
			{
				TExpense selItem;
				String[] auxFinalList;
				
				DialogInterface.OnClickListener onClickTraspasosItem = new OnClickListener()
				{
					
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						if (auxFinalList == null || auxFinalList.length < (which+1)) 
							return;
						
						traspasarItem(selItem, auxFinalList[which]);
					}
				};
				
				@Override
				public void onClick(View v)
				{
					selItem = (TExpense) v.getTag();
					
					if (selItem == null)
						return;
					
					
					TTablesDAO tablesDS = new TTablesDAO(ctx);
					tablesDS.openReadable();
					List<TTable> auxTablesList = tablesDS.getAllRows();
					if (auxTablesList.size() <= 1) {
						Toast.makeText(ctx, "o_O You only have 1 TABLE!", Toast.LENGTH_SHORT).show();
						return;
					}
					auxFinalList = new String[auxTablesList.size()-1];
					int i=0;
					for (TTable tableItem : auxTablesList) {
						if (!tableItem.getName().equalsIgnoreCase(cTableName)) {
							auxFinalList[i] = tableItem.getName();
							i++;
						}
					}
					
					
					AlertDialog.Builder dlgMove = new AlertDialog.Builder(ctx);
					dlgMove.setTitle("Move item to...");
					dlgMove.setCancelable(true);
					dlgMove.setItems(auxFinalList, onClickTraspasosItem);
					dlgMove.show();
				}
			});
			
			imgAttachment.setTag(currentItem);
			imgAttachment.setOnClickListener(btnAttachmentEvent);

			lblDesc.setText(currentItem.getDescription());
			lblCost.setText(String.format("%.2f", currentItem.getCost()));

			calItem.setTimeInMillis(currentItem.getTimestamp());

			if (calItem.get(Calendar.YEAR) == calToday.get(Calendar.YEAR) && 
				calItem.get(Calendar.DAY_OF_YEAR) == calToday.get(Calendar.DAY_OF_YEAR))
			{
				lblTimestamp.setText("Hoy");
			}
			else if (calItem.get(Calendar.YEAR) == calYesterday.get(Calendar.YEAR) && 
					 calItem.get(Calendar.DAY_OF_YEAR) == calYesterday.get(Calendar.DAY_OF_YEAR)) 
			{
				lblTimestamp.setText("Ayer");
			}
			else {
				lblTimestamp.setText(sfd.format(calItem.getTime()));
			}

			imgAttachment.setImageResource(currentItem.hasAttachment() ? R.drawable.clip
																	   : R.drawable.camera);
			
			return mainLayout;
		}

	}

	private class compatatorTExpenses implements Comparator<TExpense>
	{
		@Override
		public int compare(TExpense lhs, TExpense rhs)
		{
			if (lhs.getTimestamp() < rhs.getTimestamp())
				return 1;
			else if (lhs.getTimestamp() == rhs.getTimestamp())
				return 0;
			else
				return -1;
		}
	}

	private View.OnClickListener btnAttachmentEvent = new View.OnClickListener()
	{

		@Override
		public void onClick(View v)
		{
			if (v.getTag() == null)
				return;

			TExpense item = (TExpense) v.getTag();
			selItem = item;
			if (item.hasAttachment())
				showAttachmentPhoto(item);
			else
				callTakePhoto(item.getId());
		}
	};

	private boolean deleteAttachedPicture(TExpense item) {
		boolean res=false;
		try {
			File faux = new File(item.getAttachment());
			
			if (faux != null && faux.exists() && faux.isFile() && faux.delete()) {
				Toast.makeText(ctx, "Photo deleted", Toast.LENGTH_SHORT).show();
				item.setAttachment(null);
				res = datasource.updateExpense(item);
				lAdapter.notifyDataSetChanged();
			}
		}
		catch (Exception e) { 
			Log.e("DAVID", "ERROR: Trying to delete the attached picture... -> " + e.getMessage());
		}
		return res;
	}
	
	private void traspasarItem(TExpense item, String destTableName) {
		// Open destination table
		TExpensesDAO destTable = new TExpensesDAO(this, destTableName);
		destTable.open();
		// Creating new item...
		destTable.createExpense(item.getTimestamp(), item.getDescription(), item.getCost(), item.getAttachment());
		destTable.close();
		// Deleting the item from the current table.
		if (datasource.deleteExpense(item,true))
		{
			valuesList.remove(item.getAuxPos());
	    	lAdapter.notifyDataSetChanged(); 
	    	updateResult(); 
	    	Toast.makeText(ctx,"Item moved to -> " + destTableName , Toast.LENGTH_SHORT).show();
    		hideExpenseOptionsMenu();
		}
		
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_expenses, menu);
		menuApp = menu;
		return true;
	}

	private void previousMonth()
	{
		currentMonth.add(Calendar.MONTH, -1);
//		lstView.setAdapter(null);
		valuesList.clear();
		valuesList.addAll(datasource.getAllRows(currentMonth));
//		lstView.setAdapter(lAdapter);
		if (lAdapter != null)
			lAdapter.notifyDataSetChanged();		
		updateAB();
		hideExpenseOptionsMenu();
	}

	private void nextMonth()
	{
		currentMonth.add(Calendar.MONTH, 1);
		// lstView.setAdapter(null);
		valuesList.clear();
		valuesList.addAll(datasource.getAllRows(currentMonth));
		// lstView.setAdapter(lAdapter);
		if (lAdapter != null)
			lAdapter.notifyDataSetChanged();
		updateAB();
		hideExpenseOptionsMenu();
	}

	private void go2currentMonth()
	{
		currentMonth = Calendar.getInstance();
		currentMonth.set(Calendar.DAY_OF_MONTH, 1);
		// lstView.setAdapter(null);
		valuesList.clear();
		valuesList.addAll(datasource.getAllRows());
		// lstView.setAdapter(lAdapter);
		if (lAdapter != null)
			lAdapter.notifyDataSetChanged();
		
		
		updateAB();
		hideExpenseOptionsMenu();
	}
	
	private void go2Month (int month)
	{
		if (month < 0 || month > 11)
			return;
		
		currentMonth.set(Calendar.MONTH, month);
		valuesList.clear();
		datasource.open();
		valuesList.addAll(datasource.getAllRows(currentMonth));
		if (lAdapter != null)
			lAdapter.notifyDataSetChanged();
		
		updateAB();
		hideExpenseOptionsMenu();
	}

	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		/*if (item.getItemId() == R.id.action_deleteAll)
		{
			/*
			 * AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(ctx);
			 * dlgBuilder.setTitle("DELETE ALL RECORDS");
			 * dlgBuilder.setMessage("Are you sure to delete ALL record?");
			 * dlgBuilder.setPositiveButton("Yes, I am sure", new
			 * OnClickListener() {
			 * 
			 * @Override public void onClick(DialogInterface dialog, int which)
			 * { if (datasource.deleteAll()) Toast.makeText(ctx,
			 * "All records deleted", Toast.LENGTH_SHORT).show(); else
			 * Toast.makeText(ctx, "No records found",
			 * Toast.LENGTH_SHORT).show(); } });
			 * 
			 * dlgBuilder.setNegativeButton("No!", null); AlertDialog dlgDelete
			 * = dlgBuilder.create(); dlgDelete.show(); return true;
			 */
		
		if (item.getItemId() == R.id.action_previousMonth)
		{
			previousMonth();
		}

		else if (item.getItemId() == R.id.action_currentMonth)
		{
			go2currentMonth();
		} else if (item.getItemId() == R.id.action_nextMonth)
		{
			nextMonth();
		} 
		
		/*else if (item.getItemId() == R.id.action_Refresh)
		{
			updateResult();
		}*/
		return super.onOptionsItemSelected(item);
	}

	@SuppressLint("DefaultLocale")
	private String getFilePictureNameFromId(long id)
	{
		return String.format("img_%s_%d.jpg", TExpenseSqlHelper.TABLE_EXPENSES,
				id);
	}

	private String getFullPathName(long id)
	{
		return (Environment.getExternalStorageDirectory()
				+ "/MyExpenses/.images/" + getFilePictureNameFromId(id));
	}

	private void showAttachmentPhoto(TExpense item)
	{
		if (item.getAttachment().trim().length() == 0)
			return;

		Intent iViewPhoto = new Intent(Intent.ACTION_VIEW);
		Uri hacked_uri = Uri.parse("file://" + item.getAttachment());
		iViewPhoto.setDataAndType(hacked_uri, "image/*");
		startActivity(iViewPhoto);
	}

	private void callTakePhoto(long id)
	{
		Intent intCallPhoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		File f = new File(getFullPathName(id));
		f.getParentFile().mkdirs();
		intCallPhoto.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
		startActivityForResult(intCallPhoto, REQ_CODE_TAKE_PICURE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == REQ_CODE_TAKE_PICURE
				&& resultCode == Activity.RESULT_OK)
		{
			Toast.makeText(ctx, "Foto hecha", Toast.LENGTH_SHORT).show(); // TODO:
																			// REMOVE
			selItem.setAttachment(getFullPathName(selItem.getId()));
			datasource.open();
			datasource.updateExpense(selItem);
			lAdapter.notifyDataSetChanged();
			// Resizing photo
			// attach the picture to the
		}
		else  if (requestCode == REQ_CODE_EDIT_EXPENSE && resultCode == Activity.RESULT_OK)
		{
			go2Month(currentMonth.get(Calendar.MONTH));
			hideExpenseOptionsMenu();
		}
	}
	
	private void hideExpenseOptionsMenu() {
		if (lastOptionsLayoutVisible != null && lastOptionsLayoutVisible.getVisibility() == View.VISIBLE) {
			lastOptionsLayoutVisible.setVisibility(View.GONE);
			lastOptionsLayoutVisible = null;
		}
	}
	
	
}
