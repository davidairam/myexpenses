package com.kld.myexpenses;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kld.myexpenses.database.TExpensesDAO;
import com.kld.myexpenses.database.TTable;
import com.kld.myexpenses.database.TTablesDAO;
import com.kld.myexpenses.utils.TInputTextDialog;
import com.kld.myexpenses.utils.Utils;

public class MainActivity extends Activity
{
	private static final String TAG = "TablesList-MainActivity";
	
	
	Context ctx;
	GridView gridViewMain;
	Button btnAux;
	List<TTable> list_tables;
	TablesAdapter lsAdapter;
	TTablesDAO ds;
	
	String defaultTable;
	boolean returningFromExpenses=false;;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_main);
		ctx = this;
		
		 ds = new TTablesDAO(this);
		 ds.open();
		 
		 // TODO: REVIWE THIS FUCKING LINES. above and below.
		 list_tables = ds.getAllRows();
		 ds.getAllSysTables();
		 defaultTable = getDefaultTableName();
		 lsAdapter = new TablesAdapter(ctx, R.layout.item_table_gridview, list_tables);
		 mappingViews();
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		
		if (!returningFromExpenses && defaultTable != null)
		{
			Intent intLaunchExpensesActivity = new Intent(ctx, ExpensesActivity.class);
			intLaunchExpensesActivity.putExtra("PARAM_TABLE_NAME", defaultTable);
			startActivityForResult(intLaunchExpensesActivity, 200);
		}

		returningFromExpenses = false;
	}
	
	private void refreshTables() {
		list_tables = ds.getAllRows();		
		lsAdapter.notifyDataSetChanged();
	}
	
	private void mappingViews()
	{
		gridViewMain = (GridView) findViewById(R.id.gridViewTables);		
		gridViewMain.setAdapter(lsAdapter);
		lsAdapter.notifyDataSetChanged();
		
		 gridViewMain.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int pos,
					long arg3)
			{
				Intent intLaunchExpensesActivity = new Intent(ctx, ExpensesActivity.class);
				intLaunchExpensesActivity.putExtra("PARAM_TABLE_NAME", list_tables.get(pos).getName());
				startActivityForResult(intLaunchExpensesActivity, 200);
			}
			 
		});
		 
		 registerForContextMenu(gridViewMain);
	}
	
	private class TablesAdapter extends ArrayAdapter<TTable> 
	{

		Context context;
		LayoutInflater li;
		int resId;
		
		public TablesAdapter(Context context, int resource, List<TTable> objects)
		{
			super(context, resource, objects);
			this.context = context;
			this.resId = resource;
			this.li = getLayoutInflater();
		}


		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View gridview;
			
			if (convertView == null)
			{
				gridview = new View(context);
				gridview = li.inflate(resId, null);
			}
			else 
				gridview = (View) convertView;
			
			TextView lblTable = (TextView) gridview.findViewById(R.id.lblTablename);
			TextView lblExtra = (TextView) gridview.findViewById(R.id.lblExtra);
			ImageView imgStarred = (ImageView) gridview.findViewById(R.id.imgStarredTable);
			TTable itemTable = (TTable) getItem(position);
			
			lblTable.setText(itemTable.getName());
			lblExtra.setText(itemTable.getDescription());
			imgStarred.setVisibility(View.GONE);
			
			if (defaultTable != null && defaultTable.equalsIgnoreCase(itemTable.getName())) {
				imgStarred.setVisibility(View.VISIBLE);
			}
			return gridview;
		}
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == 100)
		{
			if (resultCode == Activity.RESULT_OK && data != null)
			{
				
				String tName = data.getStringExtra("TABLE_NAME");
				String tDescription = data.getStringExtra("TABLE_DESCRIPTION");
				String tPassword = data.getStringExtra("TABLE_PASSWORD");
				String tLimit = data.getStringExtra("TABLE_LIMIT");
				int iLimit;
				try { 
					iLimit= Integer.parseInt(tLimit);
				}
				catch (Exception e) { iLimit = 0; }
				
				TTable  eltoAdded = ds.add(tName, iLimit, tDescription, tPassword);
				if (eltoAdded == null)
				{
					Toast.makeText(ctx, "La tabla YA EXISTE!", Toast.LENGTH_SHORT).show();
					return;
				}
				Toast.makeText(ctx, "added", Toast.LENGTH_SHORT).show();
				list_tables.add(eltoAdded);
				lsAdapter.notifyDataSetChanged();
			}
				
		}
		else if (requestCode == 200) {
			returningFromExpenses = true;
		}
	}
	public void addTableOnClick (MenuItem item) 
	{
		 
		Intent launchNewTableIntent = new Intent(ctx, AddTableActivity.class);
		startActivityForResult(launchNewTableIntent, 100);
	}
	
	public void refreshOnClick (MenuItem item) 
	{
		 
		refreshTables();
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		
		menu.setHeaderTitle("Table Options");
		menu.add(0, 1, 0, "Set as Default");
		menu.add(0, 2, 0, "Rename");
		menu.add(0, 3, 0, "Delete");
		
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		AdapterContextMenuInfo adrMenu = (AdapterContextMenuInfo)item.getMenuInfo();
		if (adrMenu.position < 0)
			return true;
		
		TTable auxTable = list_tables.get(adrMenu.position);
		
		if (auxTable == null)
			return true;
		
		switch (item.getItemId())
		{
		case 1:
			setAsDefault(auxTable);
			break;
		case 2:
			renameTable(auxTable);
			break;

		case 3:
			deleteTable(auxTable);
			break;
		}
		
		return super.onContextItemSelected(item);
	}
	
	private String getDefaultTableName() {
		SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(ctx);
		return prefs.getString(Utils.KEY_DEFAULT_TABLE, null);
	}
	private void setAsDefault(TTable tabla) {
		SharedPreferences.Editor prefsEdit = PreferenceManager.getDefaultSharedPreferences(ctx).edit();
		
		// Remove any table as default...
		if (getDefaultTableName() != null && getDefaultTableName().equalsIgnoreCase(tabla.getName())) {
			prefsEdit.remove(Utils.KEY_DEFAULT_TABLE);
			prefsEdit.commit();
			defaultTable = null;
			lsAdapter.notifyDataSetChanged();
			return;
		}

		// Setting as default...
		prefsEdit.putString(Utils.KEY_DEFAULT_TABLE, tabla.getName());
		
		if (prefsEdit.commit()) {
			defaultTable = tabla.getName();
			lsAdapter.notifyDataSetChanged();
		}
	}
	
	private void deleteTable(final TTable table) {
		
		TExpensesDAO dsExpensesAux = new TExpensesDAO(ctx, table.getName());
		
		try  {
			dsExpensesAux.open(true);
			
			if (dsExpensesAux.getAllRows().size() >= 10) {
				Toast.makeText(ctx, "You can NOT DELETE a table with 10 items or more.", Toast.LENGTH_SHORT).show();
				return;
			}
		}
		catch (Exception e) {
			Log.e(TAG, "ERROR: deleteTable() Trying to check the table items size. -> " + e.getMessage());
			e.printStackTrace();
		}
		
		AlertDialog.Builder dlgConfirmDeleteTable = new AlertDialog.Builder(ctx);
		
		dlgConfirmDeleteTable.setTitle("Are you sure to delete the table " + table.getName() + "?");
		dlgConfirmDeleteTable.setCancelable(true);
		dlgConfirmDeleteTable.setNegativeButton("No!", null);
		dlgConfirmDeleteTable.setPositiveButton("Yes,  I am sure.", new DialogInterface.OnClickListener()
		{
			
			@Override
			public void onClick(DialogInterface dialog, int which)
			{

				if (ds.deleteTable(table)) {
					lsAdapter.remove(table);
					Toast.makeText(ctx, "Deleted.", Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		dlgConfirmDeleteTable.show();
	}
	
	
	private void renameTable(final TTable table) {
		
		TInputTextDialog.OnInputTextDialogDone okevent = new TInputTextDialog.OnInputTextDialogDone()
		{
			@Override
			public void OnInputTextDone(String text)
			{
				if (text.trim().length() == 0)
					return;
				
				if (TTablesDAO.existsTable(ctx, text)) {
						Toast.makeText(ctx, "ERROR: Ya existe una tabla con ese nombre.", Toast.LENGTH_SHORT).show();
						return;
				}
				
				if (ds.rename(table, text)) {
					refreshTables();
				}
				else {
					Toast.makeText(ctx, "ERROR: No se pudo renombrar la tabla. Vuelva a intentarlo.", Toast.LENGTH_SHORT).show();
				}
			}
		};
		
		TInputTextDialog input = new TInputTextDialog(ctx, "Rename Table", "Introduzca el nuevonombre:", table.getName(), InputType.TYPE_CLASS_TEXT, okevent);
		input.show();
		 
		 
	}
}

