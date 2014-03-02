package com.kld.myexpenses.utils;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.DatePicker;

/***
 * This class improves the android DatePickerDialog supporting
 * cancel events and only calling the dateSetEvent if the user
 * not aborts the input.
 * 
 * @author KLD
 *
 */
public class TDatePickerDialog extends DatePickerDialog {

	private boolean canceled=false;
	public boolean isCanceled()  { return this.canceled; }
	
	OnClickListener userCancelEvent;
	OnDateSetListener userDateSetEvent;
	
	TDatePickerDialog me;
	static Context ctx;
	
	private static OnDateSetListener onDateSetted = new OnDateSetListener()
	{
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth)
		{
			
			if (view.getTag() == null) {
				return;
			}
			
			TDatePickerDialog mine=null;
			try {
				mine = (TDatePickerDialog)view.getTag();
			}
			catch (Exception e) { return; }
			finally {
				if (mine == null || mine.userDateSetEvent == null) {
					return;
				}
			}
			
			if (!mine.isCanceled() && mine.userDateSetEvent != null) {
				mine.userDateSetEvent.onDateSet(view, year, monthOfYear, dayOfMonth);
			}
		}
	};
	
	public TDatePickerDialog(Context context, OnDateSetListener callBack, String OKTextButton, String CancelTextButton,
			int year, int monthOfYear, int dayOfMonth)
	{
		super(context, onDateSetted, year, monthOfYear, dayOfMonth);
		
		// At this point we have the class instanciated.
		// Saving the real-user dateSetEvent...
		userDateSetEvent = callBack;
		
		// Initializing context, tag to null...
		ctx = context;
		me = this;
		
		// In the datepicker getTag we set our class.
		super.getDatePicker().setTag(this);
		
		
		if (OKTextButton != null && OKTextButton.trim().length() > 0) {
			setButton(DatePickerDialog.BUTTON_POSITIVE, OKTextButton, onClickButtonDoNothing);
		}
		
		if (CancelTextButton != null && CancelTextButton.trim().length() > 0) {
			setButton(DatePickerDialog.BUTTON_NEGATIVE,CancelTextButton, onClickButtonDoNothing);
		}
	}
	

	private DialogInterface.OnClickListener onClickButtonDoNothing = new OnClickListener()
	{
		@Override
		public void onClick(DialogInterface dialog, int which)
		{
			/* DO NOTHING */
		}
	}; 
	
	@Override
	public void show()
	{
		// due to security reasons
		super.getDatePicker().setTag(this);
		canceled = false;
		super.show();
	}
	
	@Override
	public void onBackPressed()
	{
		canceled = true;
		super.onBackPressed();
		
	}
	
	private OnClickListener onClickCancelButton = new OnClickListener()
	{
		@Override
		public void onClick(DialogInterface dialog, int which)
		{
			canceled=true;
			if (userCancelEvent != null)
				userCancelEvent.onClick(dialog, which);
		}
	};
	
	
	@Override
	public void setButton(int whichButton, CharSequence text,
			OnClickListener listener)
	{
		// If a cancel button was setted we listen for the press of this button.
		if (whichButton == DatePickerDialog.BUTTON_NEGATIVE) {
			userCancelEvent = listener;
			super.setButton(whichButton, text, onClickCancelButton);
		}
		else {
			super.setButton(whichButton, text, listener);
		}
	}
}