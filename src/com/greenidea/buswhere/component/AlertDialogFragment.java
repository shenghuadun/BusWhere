package com.greenidea.buswhere.component;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class AlertDialogFragment extends DialogFragment
{
	String msg;
	OnUserSelectListener listener; 
	Object obj;
	public AlertDialogFragment(String msg, OnUserSelectListener listener, Object obj)
	{
		this.msg = msg;
		this.listener = listener;
		this.obj = obj;
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(msg)
               .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   listener.onPositiveButtonClicked(obj);
                   }
               })
               .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   listener.onNegativeButtonClicked(obj);
                   }
               });
        return builder.create();
    }
	
	public interface OnUserSelectListener
	{
		void onPositiveButtonClicked(Object obj);
		void onNegativeButtonClicked(Object obj);
	}
}
