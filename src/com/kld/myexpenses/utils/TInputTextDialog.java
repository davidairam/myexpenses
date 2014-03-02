package com.kld.myexpenses.utils;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;

public class TInputTextDialog
{
	AlertDialog.Builder builder;
	OnInputTextDialogDone userCallBack;
	EditText editText;
	Context ctx;
	
	
	DialogInterface.OnClickListener OKEvent = new DialogInterface.OnClickListener()
	{
		
		@Override
		public void onClick(DialogInterface dialog, int which)
		{
			userCallBack.OnInputTextDone(editText.getText().toString());
		}
	};
	
	public interface OnInputTextDialogDone {
		public void OnInputTextDone (String text);
	}
	
	public TInputTextDialog(Context context, String title, String subtitle, String hint, int inputType, OnInputTextDialogDone callback)
	{
		ctx = context;
		userCallBack = callback;
		builder = new Builder(ctx);
		editText = new EditText(ctx);
		editText.setInputType(inputType);
		editText.setHint(hint);
		builder.setView(editText);
		builder.setTitle(title);
		builder.setMessage(subtitle);
		builder.setPositiveButton("Aceptar", OKEvent);
		builder.setNegativeButton("Cancelar",null);
		
	}
	
	public void show()
	{
		builder.show();
		editText.requestFocus();
	}

}
