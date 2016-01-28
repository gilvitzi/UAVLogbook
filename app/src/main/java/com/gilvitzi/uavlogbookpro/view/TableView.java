package com.gilvitzi.uavlogbookpro.view;


import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextMenu;
import android.widget.TableLayout;

import com.gilvitzi.uavlogbookpro.util.NameValuePair;

import java.util.ArrayList;
import java.util.List;


@SuppressWarnings("unused")
public class TableView extends TableLayout {

	private Context context;
	ContextMenu contextMenu;
	
	private SQLiteDatabase db;
	private String query;
	private String[] querySelectionArgs;
	
	//Attributes:
	private boolean showCheckBoxColumn = true;
	private boolean showHeader = true;
	private boolean showFooter = true;
	private boolean showContextMenuOptions = true;
	private boolean showLineNumbers = true;
	//Styles
	private int headerStyleResource;
	private int footerStyleResource;
	private int rowStyleResource;
	private int oddRowStyleResource;
	private int evenRowStyleResource;
	private int selectedRowStyleResource;
	
	
	private TableLayout mTableLayout;
	private int viewID;
	private int row_count;
	private List<Object> records; // parameratize List<TPYE>?
	public ArrayList<Integer> selectedRows;
	public PopulateTableViewTask mPopulateTableViewTask;
	public  ProgressDialog progressDialog;
	
	 /*
     * Constructor with ContextMenu
     */
	public TableView(Context context) {
	    super(context);
	}
	
	public TableView(Context context, AttributeSet attrs) {
	    super(context, attrs);
	}
	
	public TableView(Context context, AttributeSet attrs,ContextMenu contextMenu,int viewID) {
		super(context, attrs);
		this.context = context;
		this.contextMenu = contextMenu;
		this.viewID = viewID;
		this.initTable();
		mPopulateTableViewTask = new PopulateTableViewTask();
	}
	
	private void initTable(){
	    this.mTableLayout = (TableLayout) findViewById(this.viewID);
	    
	}
		
	/*
	 * Sets the String SQLite Query
	 */
	public void setQuery(String query,String[] selectionArgs){
		this.query = query;
		this.querySelectionArgs = selectionArgs;
	}
	
	public String getQuery(){
		return this.query;
	}
	
	public String[] getSelectionArgs(){
	    return this.querySelectionArgs;
	}
	
	@SuppressWarnings("unchecked")
    public void refresh(){
	    //if populate task is not already running
	    //invoke a new populate task
	    
		if (this.mPopulateTableViewTask.getStatus() != AsyncTask.Status.RUNNING){
		    mPopulateTableViewTask.execute();
		}
	}
	
	
	
	private class PopulateTableViewTask extends AsyncTask<List<NameValuePair>, String, Boolean> {

		@Override
		protected Boolean doInBackground(List<NameValuePair>... params) {
			try{  //Query Table and Save All Records
				Cursor cursor = db.rawQuery(query, querySelectionArgs);
				// get all columns
				
				//get all data
			}catch (Exception e){
				Log.e("TableView","PopulateTableViewTask rawQuery Error: " + e);
				return false;
			}
			return true;
		}
		
		protected void onPostExecute(final Boolean success) {
			/*
				TableLayout tl = (TableLayout) findViewById(R.id.tbl_all_sessions);
		    	
		    	//Add Header:
		    	@SuppressWarnings("unused")
				LayoutInflater inflater = 
	    	              (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    		TableRow tr = (TableRow) View.inflate(context, R.layout.table_row_header_session, null);
	    		tr.setTag("header");
	    		tl.addView(tr);
	    		CheckBox cb = (CheckBox) tr.findViewWithTag("check");

	    		cb.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						//get Checked state:
						Boolean isChecked = ((CheckBox)v).isChecked();
						ViewGroup tableLayout = (ViewGroup) v.getParent().getParent();	    
		    		    View row; //Table Row
		    		    for (int i = 0;i < tableLayout.getChildCount();i++){
		    		    	row = tableLayout.getChildAt(i);
		    			   	CheckBox checkbox = (CheckBox)row.findViewWithTag("check");
		    			   	checkbox.setChecked(isChecked);
		    		    }
					}
	    		});
	    		
		    	for (int i = 0;i < records.size();i++){
		    		Object record  = records.get(i);
		    				    		
		    		row_count++; //add to row counter
		    		
		    		//Create New TableRow
		    		tr = (TableRow) View.inflate(context, R.layout.table_row_session, null);
		    		tl.addView(tr); //Append to TableLayout
		    		
		    		// Set Dynamic Parameters
		    	    tr.setTag(session.getId()); //row index will be the session index.
		    	    			    	    
		    	    //set Background color according to odd / even
		    	    if (i%2==1){
		    	    	tr.setBackgroundColor(getResources().getColor(R.color.table_row_odd));
		    	    }else{
		    	    	tr.setBackgroundColor(getResources().getColor(R.color.table_row_even));
		    	    }
		    	    
		    	    
		    	    
		    	    //Populate Row Views:
		    	    
		    	    //Date
		    	    TextView tv_date = (TextView) tr.findViewWithTag("date");
		    	    tv_date.setText(session.getDateString());
		    	    			    	    
		    	    TextView tv_duration = (TextView) tr.findViewWithTag("duration");
		    	    tv_duration.setText(session.getDuration());
		    	    
		    	    TextView tv_platform = (TextView) tr.findViewWithTag("platform");
		    	    tv_platform.setText(session.getPlatformType() + " " + session.getPlatformVariation());

		    	    TextView tv_reg = (TextView) tr.findViewWithTag("reg_no");
		    	    tv_reg.setText(session.getRegistration());
		    	    
		    	    TextView tv_icao = (TextView) tr.findViewWithTag("icao");
		    	    tv_icao.setText(session.getICAO());
		    	    
		    	    cb = (CheckBox) tr.findViewWithTag("check");
    				
		    		cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
		    	    	   @Override
		    	    	   public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
		    	    		   int row_id = Integer.parseInt(((View) buttonView.getParent()).getTag().toString());
		    	    		   if(isChecked){
		    	    			   selectedRows.add(row_id);
		    	    		   }else{
		    	    			   selectedRows.remove((Object) row_id);
		    	    		   }
		    	    		   
		    	    		// Set Header Checkbox to UNCHECHEKED
    	    				   TableLayout tl = (TableLayout) buttonView.getParent().getParent();
    	    				   TableRow row_hdr = (TableRow) tl.findViewWithTag("header");
    	    				   CheckBox cb_hdr = (CheckBox) row_hdr.findViewWithTag("check");
		    	    		   if (selectedRows.size() == row_count){
	    	    				   cb_hdr.setChecked(true);
		    	    		   }else{
		    	    			   cb_hdr.setChecked(false);
		    	    		   }
		    	    		   //Update Menu Actions Availability
		    	    		   if (selectedRows.size()>1){ 
		    	    			   //More than 1 row selected  
		    	    			   contextMenu.getItem(0).setVisible(false); //Edit unavailable
		    	    			   contextMenu.getItem(1).setVisible(true);	//Delete available
		    	    			   
		    	    		   }else if(selectedRows.size()==1){
	    	    				   //1 row selected
		    	    			   contextMenu.getItem(0).setVisible(true); //Edit available
		    	    			   contextMenu.getItem(1).setVisible(true);	//Delete available
			    	    			   
	    	    			   }else{
	    	    				   //No selected rows
	    	    				   
	    	    				   //Set Context Menu items
	    	    				   contextMenu.getItem(0).setVisible(false); //Edit unavailable
		    	    			   contextMenu.getItem(1).setVisible(false);	//Delete unavailable
	    	    			   }
		    	    	   }
	    	    	});//END OF onCheckedChanged (Listener)
		    	}
		    	progressDialog.dismiss();
		    	 */
		    }
			
		}
	
}
