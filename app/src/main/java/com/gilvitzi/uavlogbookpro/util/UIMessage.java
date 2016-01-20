package com.gilvitzi.uavlogbookpro.util;



import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class UIMessage {
	private final static String LOGTAG = "UI Message";
	
	public static void makeToast(Context context,String text){
		int duration = Toast.LENGTH_LONG;
		try{
			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		}catch(Exception e){
			Log.e(LOGTAG,"Error: " + e);
		}
	}
	
	public static void makeToast(Context context,int stringResourceId){
		String text = context.getResources().getString(stringResourceId);
		int duration = Toast.LENGTH_LONG;
		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
	}

	public void showAlertDialog(Context context,String message){
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        switch (which){
		        case DialogInterface.BUTTON_POSITIVE:
		            //Yes button clicked
		            break;

		        case DialogInterface.BUTTON_NEGATIVE:
		            //No button clicked
		            break;
		        }
		    }
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(message).setPositiveButton("Yes", dialogClickListener)
		    .setNegativeButton("No", dialogClickListener).show();
	}
	
	
	/**
	 * This Method gets all child views which has the tag as specified in variable 'tag'
	 * @param root (ViewGroup) the Root Parent View from which children to search
	 * @param tag  (String) the method will return all children with this tag
	 * @return  (ArrayList<View>) - a list of child Views which has the tag variable value in their tag attribute
	 * 
	 */
	public static ArrayList<View> getViewsByTag(ViewGroup root, String tag){
	    ArrayList<View> views = new ArrayList<View>();
	    final int childCount = root.getChildCount();
	    for (int i = 0; i < childCount; i++) {
	        final View child = root.getChildAt(i);
	        if (child instanceof ViewGroup) {
	            views.addAll(getViewsByTag((ViewGroup) child, tag));
	        } 

	        final Object tagObj = child.getTag();
	        if (tagObj != null && tagObj.equals(tag)) {
	            views.add(child);
	        }

	    }
	    return views;
	}
}