package com.kld.myexpenses.utils;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TimePicker;

/***
 * This class improves the android DatePickerDialog supporting
 * cancel events and only calling the dateSetEvent if the user
 * not aborts the input.
 * 
 * @author KLD
 *
 */
public class TTimePickerDialog extends TimePickerDialog {

	private boolean canceled=false;
	public boolean isCanceled()  { return this.canceled; }
	
	OnClickListener userCancelEvent;
	OnTimeSetListener userTimeSetEvent;
	
	TTimePickerDialog me;
	static Context ctx;
	
	private static OnTimeSetListener onTimeSetted = new OnTimeSetListener()
	{
		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			
			
			if (view.getTag() == null) {
				return;
			}
			
			TTimePickerDialog mine=null;
			try {
				mine = (TTimePickerDialog)view.getTag();
			}
			catch (Exception e) { return; }
			finally {
				if (mine == null || mine.userTimeSetEvent == null) {
					return;
				}
			}
			
			if (!mine.isCanceled() && mine.userTimeSetEvent != null) {
				mine.userTimeSetEvent.onTimeSet(view, hourOfDay, minute);
			}
		}
	};
	
	
	private void locateInternalTimePicker (ViewGroup rootView) {
		
		View vaux;
		ViewGroup vgaux;
		
		for (int i =0; i < rootView.getChildCount(); i++)
		{
			vaux = rootView.getChildAt(i);
			
			if (vaux.getClass().equals(TimePicker.class)) {
				vaux.setTag(this);
				break;
			}
			try {  
				vgaux = ((ViewGroup)vaux);
				locateInternalTimePicker(vgaux);
			}
			catch (Exception e) { /* DO NOTHING */ }
		}
	}
	
	
	public TTimePickerDialog(Context context, OnTimeSetListener callBack, String OKTextButton, String CancelTextButton,
			int hourOfDay, int minute, boolean is24HourView)
	{
		super(context, onTimeSetted, hourOfDay, minute, is24HourView);
		
		// At this point we have the class instanciated.
		// Saving the real-user dateSetEvent...
		userTimeSetEvent = callBack;
		
		// Initializing context, tag to null...
		ctx = context;
		me = this;
		
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
		canceled = false;
		super.show();
		
		// After the show we try to locate the internal timePicker and set our instance in his tag object.
		locateInternalTimePicker((ViewGroup)getWindow().getDecorView());
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