package com.gilvitzi.uavlogbookpro.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.gilvitzi.uavlogbookpro.model.Aerodrome;
import com.gilvitzi.uavlogbookpro.database.AerodromesDataSource;
import com.gilvitzi.uavlogbookpro.database.LogbookSQLite;
import com.gilvitzi.uavlogbookpro.R;
import com.gilvitzi.uavlogbookpro.model.Session;
import com.gilvitzi.uavlogbookpro.util.AerodromeICAOListAdapter;
import com.gilvitzi.uavlogbookpro.util.AerodromeNameListAdapter;
import com.gilvitzi.uavlogbookpro.util.DateTimeConverter;
import com.gilvitzi.uavlogbookpro.util.Duration;
import com.gilvitzi.uavlogbookpro.util.NameValuePair;
import com.gilvitzi.uavlogbookpro.util.StringValuePair;
import com.gilvitzi.uavlogbookpro.util.TagsContainer;
import com.gilvitzi.uavlogbookpro.util.UIMessage;
import com.google.android.gms.analytics.HitBuilders;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@SuppressLint({
        "SimpleDateFormat", "DefaultLocale"
})
public class ActivityAddSession extends DatabaseActivity {
	private final String LOG_TAG ="ActivityAddSession";
	final static String screen_name = "AddSession";
	
	public FragmentActivity thisActivity;

	private FormPopulateTask mPopulateTask = null;
	private AddSessionTask mAddSessionTask = null;
	
	private long editSessionId;
	private long qs_duration;
	
	public Session lastSession;
	
	public boolean first_platform_spinner_selection = true;
	
	public Button submitButton;
	public ProgressDialog pDialog;
	
	protected String err_msg;
	
	private View mInputCustomTextWindow;
	private EditText mCustomTextInput;
	private Button mCustomTextSubmitButton;
	private View mInputCustomTextUpdateView;

	private DialogFragment datePickerFragment;
	private DialogFragment timePickerFragment;
	private DialogFragment durationPickerFragment;
	
	private LinearLayout platformCustomRow;
	private LinearLayout regAndTailCustomRow;
	private LinearLayout locationCustomRow;

	public List<String> platform_li;
	public List<String> location_li;
	
	public List<String> plat_type_li;
	public List<String> plat_variation_li;
	public List<String> reg_li;
	public List<String> tail_li;
	public List<String> reg_n_tail_li;
	public List<String> command_li;
	public List<String> seat_li;
	public List<String> flight_type_li;
	public List<String> remarks_li;
	
	public List<String> platform_li_ind;
	public List<String> location_li_icao_ind;
	public List<String> location_li_name_ind;
	
	public ArrayAdapter<String> platform_adp;
	public ArrayAdapter<String> reg_n_tail_adp;
	public ArrayAdapter<String> location_adp;
	public ArrayAdapter<String> command_adp;
	public ArrayAdapter<String> seat_adp;
	public ArrayAdapter<String> flight_type_adp;
	public ArrayAdapter<String> remarks_adp;
	
	public ArrayList<Aerodrome> aerodromes;
	public AerodromeICAOListAdapter aerodromes_icao_adp;
	public AerodromeNameListAdapter aerodromes_name_adp;
	
	public Spinner platform_spn;
	public Spinner reg_n_tail_spn;
	public Spinner location_spn;
	public Spinner command_spn;
	public Spinner seat_spn;
	public Spinner flight_type_spn;
	
	public EditText date_et;
	public EditText duration_et;
	
	public EditText plat_type;
	public EditText plat_variation;
	
	public EditText reg_no_et;
	public EditText tail_no_et;
	
	public AutoCompleteTextView loc_icao_et;
	public AutoCompleteTextView loc_name_et;
	
	public EditText takeoffs_et;
	public EditText landings_et;
	public EditText go_arounds_et;
	public AutoCompleteTextView remarks_et;

	protected RadioGroup sim_actual_radio;
	protected RadioButton sim_radio;
	protected RadioButton actual_radio;
	
	protected RadioGroup day_night_radio;
	protected RadioButton day_radio;
	protected RadioButton night_radio;
	
	public TagsContainer mTagsContainer;
	
	private AerodromesDataSource aerodromes_db;

	@Override
	protected void onDestroy() {
	    super.onDestroy();
	    if (aerodromes_db != null) {
	    	aerodromes_db.close();
	    }
	}

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        //TODO: save all user data state here
    }

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_session);

        refreshSavedInstanceStateIntoControls(savedInstanceState);
		
		thisActivity = this;

        initAerodromesDatasource();
	    
	    editSessionId = 0;
        qs_duration = 0;
	    submitButton = (Button) findViewById(R.id.add_session_button);

        getPopUpWindowsReference();
		
		showProgress(true, thisActivity);
		

        getFormControlsReferences();
		
        checkAddOrEditMode();
		
		
		mPopulateTask = new FormPopulateTask();
	    mPopulateTask.execute();

        setControlsListeners();

        List<Spinner> spn_list = getSpinnersList();

        preventSpinnersFromFiringUpEventFirstTime(spn_list);

        mInputCustomTextWindow.setVisibility(View.GONE); // hide CustomTextWindow
	}

    private void refreshSavedInstanceStateIntoControls(Bundle savedInstanceState) {
        //TODO: refresh all saved instance state and set back controls to selections.
    }


    @NonNull
    private List<Spinner> getSpinnersList() {
        List<Spinner> spn_list = new ArrayList<Spinner>();
        spn_list.add(platform_spn);
        spn_list.add(reg_n_tail_spn);
        spn_list.add(location_spn);
        spn_list.add(command_spn);
        spn_list.add(seat_spn);
        spn_list.add(flight_type_spn);
        return spn_list;
    }

    private void preventSpinnersFromFiringUpEventFirstTime(List<Spinner> spn_list) {
        //prevent Spinner From firing up on a newly instantiated spinner -> causing a CustomTextInput Window to Open
        for (Spinner spn : spn_list){
	        spn.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parentView, View spn,
						int pos, long id) {
					Object item = parentView.getItemAtPosition(pos);
					String custom_string = getResources().getString(R.string.input_add_custom);
					if (item.toString().toLowerCase().contains("add new")||item.toString().equals(custom_string)){
						if (parentView.getId()==location_spn.getId()){ //Location
							locationCustomRow.setVisibility(View.VISIBLE);
						}else if(parentView.getId()==platform_spn.getId()){ //platform
							platformCustomRow.setVisibility(View.VISIBLE);
						}else if(parentView.getId()==reg_n_tail_spn.getId()){ //Reg and Tail
							regAndTailCustomRow.setVisibility(View.VISIBLE);
						}else{
							//For any other Plain Text Field - popup text input form
							mInputCustomTextWindow.setVisibility(View.VISIBLE);
							mCustomTextInput.requestFocus();
							mInputCustomTextUpdateView =  parentView;
						}

					}else{
						if (parentView.getId()==location_spn.getId()){
							locationCustomRow.setVisibility(View.GONE);
						}else if(parentView.getId()==platform_spn.getId()){
							platformCustomRow.setVisibility(View.GONE);
						}else if(parentView.getId()==reg_n_tail_spn.getId()){
							regAndTailCustomRow.setVisibility(View.GONE);
						}else{
							//For any other Plain Text Field
						}
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> paretnView) {}
	        });
        }
    }

    private void setControlsListeners() {
        //Date Field onTouch Listener - Opens DatePicker
        date_et.setOnTouchListener(new OnTouchListener() {
		@SuppressLint("ClickableViewAccessibility")
		@Override
			public boolean onTouch(View v, MotionEvent event) {
				showDatePickerDialog(v);
				return false;
			}
        });


        //Duration Field onTouch Listener - Opens TimePicker
        duration_et.setOnTouchListener(new OnTouchListener() {
		@SuppressLint("ClickableViewAccessibility")
		@Override
			public boolean onTouch(View v, MotionEvent event) {
				showDurationPickerDialog(v);
				Log.v(LOG_TAG, "Duration - OnTouchEvent");
				return false;
			}
        });

        //Custom Input Field
        mCustomTextSubmitButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				ArrayAdapter<String> adapter =(ArrayAdapter<String>) ((Spinner) mInputCustomTextUpdateView).getAdapter();
				String item = mCustomTextInput.getText().toString();
				Log.i(LOG_TAG,"Custom Item Added - " + item);
				adapter.add(item);
				adapter.notifyDataSetChanged();
				((Spinner) mInputCustomTextUpdateView).setAdapter(adapter);

				//Set Item as Spinner Selected Item
				((Spinner) mInputCustomTextUpdateView).setSelection(adapter.getPosition(item));

				//Hide Input window
				mInputCustomTextWindow.setVisibility(View.GONE);
				mCustomTextInput.setText(null);
			}
        });

        loc_icao_et.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ActivityAddSession.this.selectedLocICAO();
            }
        });

        loc_name_et.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick (AdapterView<?> parent, View view, int position, long id) {
                ActivityAddSession.this.selectedLocName();
            }
        });

        //Special Platform Spinner OnITem SElected Even Listener
        platform_spn.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View spn,
                                       int pos, long id) {


                String custom_string = getResources().getString(R.string.input_add_custom);
                Object item = parentView.getItemAtPosition(pos);
                if (item.toString().toLowerCase().contains("add new") || item.toString().equals(custom_string)) {
                    platformCustomRow.setVisibility(View.VISIBLE);
                } else {
                    platformCustomRow.setVisibility(View.GONE);
                }
                updateRegAadTail();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
    }

    private void checkAddOrEditMode() {
        try{
            Bundle extras = getIntent().getExtras();

            if (extras != null) {
                //Try to get Session ID
                editSessionId = extras.getInt("SessionID");
                Log.i("AddSession", "Editing Record ID: " + editSessionId);

                //Try to get Quick Start Button Duration Value
                qs_duration = extras.getLong("QS_DurationMillis");
                Log.i("AddSession","Quick Start Duration Received: " + qs_duration);
            }

        }catch(Exception e){
            e.printStackTrace();
        }

        if (isEditMode()){
            setTitle("Edit Session");
            submitButton.setText("Save Changes");
            submitButton.setBackgroundResource(R.drawable.button_yellow);
        }else{				//Add Mode
            setTitle("Add Session");
            submitButton.setText("Add Session");
            submitButton.setBackgroundResource(R.drawable.button_green);
        }
    }

    private void getFormControlsReferences() {
        platform_li = new ArrayList<String>();
        plat_type_li = new ArrayList<String>();
        plat_variation_li = new ArrayList<String>();
        reg_li = new ArrayList<String>();
        tail_li = new ArrayList<String>();
        reg_n_tail_li = new ArrayList<String>();
        location_li = new ArrayList<String>();
        command_li = new ArrayList<String>();
        seat_li = new ArrayList<String>();
        flight_type_li = new ArrayList<String>();
        remarks_li = new ArrayList<String>();
        platform_li_ind = new ArrayList<String>();
        location_li_icao_ind = new ArrayList<String>();
        location_li_name_ind = new ArrayList<String>();


        platform_adp = new ArrayAdapter<String>(getBaseContext(),android.R.layout.simple_spinner_item,platform_li);
        reg_n_tail_adp = new ArrayAdapter<String>(getBaseContext(),android.R.layout.simple_spinner_item,reg_n_tail_li);
        location_adp = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,location_li);
        command_adp = new ArrayAdapter<String>(getBaseContext(),android.R.layout.simple_spinner_item,command_li);
        seat_adp = new ArrayAdapter<String>(getBaseContext(),android.R.layout.simple_spinner_item,seat_li);
        flight_type_adp = new ArrayAdapter<String>(getBaseContext(),android.R.layout.simple_spinner_item,flight_type_li);
        remarks_adp  = new ArrayAdapter<String>(getBaseContext(),android.R.layout.simple_spinner_item,remarks_li);

        platform_adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        reg_n_tail_adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        location_adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        command_adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        seat_adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        flight_type_adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        remarks_adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        platform_spn = (Spinner) findViewById(R.id.field_platform);
        reg_n_tail_spn = (Spinner) findViewById(R.id.field_reg_n_tail);
        location_spn = (Spinner) findViewById(R.id.field_location);
        command_spn = (Spinner) findViewById(R.id.field_command);
        seat_spn = (Spinner) findViewById(R.id.field_seat);
        flight_type_spn = (Spinner) findViewById(R.id.field_flight_type);


        //EditText and other field reference
        //Date & Time EditText View (& duration)
        date_et = (EditText) findViewById(R.id.field_date);
        duration_et = (EditText) findViewById(R.id.field_duration);

        plat_type = (EditText) findViewById(R.id.field_plat_type);
        plat_variation = (EditText) findViewById(R.id.field_plat_variation);

        reg_no_et = (EditText) findViewById(R.id.field_reg_no);
        tail_no_et = (EditText) findViewById(R.id.field_tail_no);

        loc_icao_et = (AutoCompleteTextView) findViewById(R.id.field_icao);
        loc_name_et = (AutoCompleteTextView) findViewById(R.id.field_location_name);
        takeoffs_et = (EditText) findViewById(R.id.field_takeoffs);
        landings_et = (EditText) findViewById(R.id.field_landings);
        go_arounds_et = (EditText) findViewById(R.id.field_go_arounds);
        remarks_et = (AutoCompleteTextView) findViewById(R.id.field_remarks);

        sim_actual_radio = (RadioGroup) findViewById(R.id.field_sim_actual);
        sim_radio = (RadioButton) findViewById(R.id.radio_sim);
        actual_radio = (RadioButton) findViewById(R.id.radio_actual);

        day_night_radio = (RadioGroup) findViewById(R.id.field_day_night);
        day_radio = (RadioButton) findViewById(R.id.radio_day);
        night_radio = (RadioButton) findViewById(R.id.radio_night);

        mTagsContainer = (TagsContainer) findViewById(R.id.field_tags);

        //Location Invisble Custom Row:
        platformCustomRow = (LinearLayout) findViewById(R.id.platform_custom_row);
        regAndTailCustomRow = (LinearLayout) findViewById(R.id.reg_n_tail_custom_row);
        locationCustomRow = (LinearLayout) findViewById(R.id.location_custom_row);
    }

    private void getPopUpWindowsReference() {
        mInputCustomTextWindow = findViewById(R.id.custom_text_window);
        mCustomTextInput = (EditText) findViewById(R.id.custom_text_input_field);
        mCustomTextSubmitButton = (Button) findViewById(R.id.custom_text_submit_button);
    }

    private void initAerodromesDatasource() {
        aerodromes_db = new AerodromesDataSource(thisActivity);
        aerodromes_db.close();
    }


    private void selectedLocICAO(){
		String value = loc_icao_et.getText().toString();
		for (Aerodrome aerodrome : aerodromes){
		    if(aerodrome.getICAO().toLowerCase().equals(value.toLowerCase())){
		        loc_name_et.setText(aerodrome.getAerodromeName());
		    }
		}
	}
	

	private void selectedLocName(){
		String value = loc_name_et.getText().toString();
		for (Aerodrome aerodrome : aerodromes){
            if(aerodrome.getAerodromeName().toLowerCase().equals(value.toLowerCase())){
                loc_icao_et.setText(aerodrome.getICAO());
            }
        }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_session, menu);
		return true;
	}

	private boolean isEditMode() {
		return editSessionId != 0;
	}


	public class FormPopulateTask extends AsyncTask<String, String, Boolean> {

		private final String LOG_TAG = "PopulateFormTask";

		@Override
		protected Boolean doInBackground(String... params_list) {

			if (isEditMode()){	//Edit Mode
				lastSession = getDatasource().getSessionById(editSessionId);
			}else{				//Add Mode
				lastSession = getDatasource().getLastSession();
			}

			getPlatformTypeAndVariation();
			getAircraftRegistrationAndTailNum();
			getAerodromeNameAndIcaoCode();

            aerodromes = aerodromes_db.getAllAerodromes(); //fill List with values from db

			getCommandSeatAndFlightTypeDefaultValuesFromValuesArray();
			getCommandSeatAndFlightTypeFromDB();

			getComments();

			addCustomItemToDropDown();
			return true;
		}

        private void getComments() {
			remarks_li = datasource.distinctValues(LogbookSQLite.COLUMN_COMMENTS);
		}

		private void addCustomItemToDropDown() {
			String item = getResources().getString(R.string.input_add_custom);
			platform_li.add(item);
			location_li.add(item);
			reg_n_tail_li.add(item);
			command_li.add(item);
			seat_li.add(item);
			flight_type_li.add(item);
		}

		private void getCommandSeatAndFlightTypeFromDB() {
			//Commnad
			ArrayList<String> dbValues = new ArrayList<String>(datasource.distinctValues(LogbookSQLite.COLUMN_COMMAND));
			for (String item : dbValues){
				if ((item!=null)&&(!command_li.contains(item))){
					command_li.add(item);
				}
			}

			//Seat
			dbValues = new ArrayList<String>(datasource.distinctValues(LogbookSQLite.COLUMN_SEAT));
			for (String item : dbValues){
				if ((item!=null)&&(!seat_li.contains(item))){
					seat_li.add(item);
				}
			}

			//Flight Type
			dbValues = new ArrayList<String>(datasource.distinctValues(LogbookSQLite.COLUMN_FLIGHT_TYPE));
			for (String item : dbValues){
				if ((item!=null)&&(!flight_type_li.contains(item))){
					flight_type_li.add(item);
				}
			}
		}

		private void getCommandSeatAndFlightTypeDefaultValuesFromValuesArray() {
			//Command
			String[] defaultValues = getResources().getStringArray(R.array.field_command_items);
			for (String item : defaultValues){
				command_li.add(item);
			}

			//Seat
			defaultValues = getResources().getStringArray(R.array.field_seat_items);
			for (String item : defaultValues){
				seat_li.add(item);
			}

			//Flight Type
			defaultValues = getResources().getStringArray(R.array.field_flight_type_items);
			for (String item : defaultValues){
				flight_type_li.add(item);
			}
		}

		private void getAerodromeNameAndIcaoCode() {
			//Aerodrome Name And ICAO Code
			ArrayList<NameValuePair> icao_name_pairs = datasource.distinct2ValuesOrdered(LogbookSQLite.COLUMN_ICAO,LogbookSQLite.COLUMN_AERODROME_NAME);
			if (!icao_name_pairs.isEmpty()){
				for (NameValuePair entry : icao_name_pairs){
					String icao = entry.getName();
					String name = entry.getValue();
					String icao_n_name = icao + " " + name;
					if (!icao_n_name.trim().isEmpty()){
						location_li.add(icao_n_name);
						location_li_icao_ind.add(icao);
						location_li_name_ind.add(name);
					}
				}
			}
		}

		private void getAircraftRegistrationAndTailNum() {
			//Aircraft Registration and Tail Number
			ArrayList<NameValuePair> reg_n_tail_pairs = datasource.distinct2ValuesOrdered(LogbookSQLite.COLUMN_REGISTRATION,LogbookSQLite.COLUMN_TAIL_NUMBER);
			if (!reg_n_tail_pairs.isEmpty()){
				for (NameValuePair entry : reg_n_tail_pairs){
					String reg = entry.getName();
					String tail_no = entry.getValue();
					String reg_n_tail = reg + " " + tail_no;
					if (!reg_n_tail.trim().isEmpty()){
						reg_n_tail_li.add(reg_n_tail);
						reg_li.add(reg);
						tail_li.add(tail_no);
					}
				}
			}
		}

		private void getPlatformTypeAndVariation() {
			//Platform Type & Variation
			ArrayList<NameValuePair> type_variation_pairs = datasource.distinct2ValuesOrdered(LogbookSQLite.COLUMN_PLATFORM_TYPE,LogbookSQLite.COLUMN_PLATFORM_VARIATION);
			if (!type_variation_pairs.isEmpty()){
				for (NameValuePair entry : type_variation_pairs){
					String type = entry.getName();
					String variation = entry.getValue();
					String type_n_variation = type + " " + variation;
					if (!type_n_variation.trim().isEmpty()){
						platform_li.add(type_n_variation);
						plat_type_li.add(type);
						plat_variation_li.add(variation);
					}
				}
			}
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			//show the results from doInBackground()
			mPopulateTask = null;
			showProgress(false,thisActivity);

			if (success) {
				try{

					platform_adp = new ArrayAdapter<String>(getBaseContext(),android.R.layout.simple_spinner_item,platform_li);
					reg_n_tail_adp = new ArrayAdapter<String>(getBaseContext(),android.R.layout.simple_spinner_item,reg_n_tail_li);
					location_adp = new ArrayAdapter<String>(getBaseContext(),android.R.layout.simple_spinner_item,location_li);
					command_adp = new ArrayAdapter<String>(getBaseContext(),android.R.layout.simple_spinner_item,command_li);
					seat_adp = new ArrayAdapter<String>(getBaseContext(),android.R.layout.simple_spinner_item,seat_li);
					flight_type_adp = new ArrayAdapter<String>(getBaseContext(),android.R.layout.simple_spinner_item,flight_type_li);
					remarks_adp  = new ArrayAdapter<String>(getBaseContext(),android.R.layout.simple_spinner_item,remarks_li);

					platform_adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					reg_n_tail_adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					location_adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					command_adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					seat_adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					flight_type_adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					remarks_adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

					//Add AutoComplete Values:
					aerodromes_icao_adp = new AerodromeICAOListAdapter(aerodromes, thisActivity);
					aerodromes_name_adp = new AerodromeNameListAdapter(aerodromes, thisActivity);


					loc_icao_et.setThreshold(1);
					loc_icao_et.setAdapter(aerodromes_icao_adp);

					loc_name_et.setThreshold(1);
					loc_name_et.setAdapter(aerodromes_name_adp);

					remarks_et.setThreshold(1);
					remarks_et.setAdapter(remarks_adp);

					//Spinners
					platform_spn.setAdapter(platform_adp);
					reg_n_tail_spn.setAdapter(reg_n_tail_adp);
					location_spn.setAdapter(location_adp);
					command_spn.setAdapter(command_adp);
					seat_spn.setAdapter(seat_adp);
					flight_type_spn.setAdapter(flight_type_adp);

					if (lastSession!=null){

						//set Date for Today
						setTodaysDate();

                        if (isEditMode()) {
                            // This Code will set the date from last session
                            String date = null;
                            date = DateTimeConverter.format(lastSession.getDate(),
                                    DateTimeConverter.ISO8601,
                                    DateTimeConverter.DATE_SLASHED);
                            date_et.setText(date);
                        }

						// if received duration from Quick Start Button
						if (qs_duration != 0){
							duration_et.setText(durationToHM(qs_duration));
						}else{
							Duration d = new Duration();
							d.setISO8601(lastSession.getDuration());
							String duration = d.getString();
							duration_et.setText(duration);
						}

						//Platform
						String platform_str = lastSession.getPlatformType() + " " + lastSession.getPlatformVariation();
						int platform_index = platform_li.indexOf(platform_str);
						if (platform_index != -1){
							platform_spn.setSelection(platform_index);
						}else{
							platform_spn.setSelection(platform_li.size()-1);
						}
						first_platform_spinner_selection = true;

						//Registration and Tail Number
						String reg_no = lastSession.getRegistration();
						String tail_no = lastSession.getTailNumber();
						String reg_n_tail = reg_no + " " + tail_no;
						if (reg_n_tail_li.indexOf(reg_n_tail)!=-1){
							reg_n_tail_spn.setSelection(reg_n_tail_li.indexOf(reg_n_tail));
						}else{
							reg_n_tail_spn.setSelection(reg_n_tail_li.size()-1);
						}

						//set Location Selection
						String icao = lastSession.getICAO();
						String loc_name = lastSession.getAerodromeName();
						String icao_n_name = icao + " " + loc_name;
						int icao_n_name_index = location_li.indexOf(icao_n_name);
						if (icao_n_name_index != -1){
							location_spn.setSelection(location_li.indexOf(icao_n_name));
						}else{
							location_spn.setSelection(location_li.size()-1);
						}

						command_spn.setSelection(command_li.indexOf(lastSession.getCommand()));
						seat_spn.setSelection(seat_li.indexOf(lastSession.getSeat()));
						flight_type_spn.setSelection(flight_type_li.indexOf(lastSession.getFlightType()));


						//set Sim / Actual
						String[] sim_actual = getResources().getStringArray(R.array.field_sim_actual_items);

						String sim_actual_val = lastSession.getSimActual();
						if (sim_actual_val.equals(sim_actual[0])){
							sim_radio.setChecked(true);
						}else if (sim_actual_val.equals(sim_actual[1])){
							actual_radio.setChecked(true);
						}

						String[] day_night = getResources().getStringArray(R.array.field_day_night_items);

						String day_night_val = lastSession.getDayNight();
						if (day_night_val.equals(day_night[0])){
							day_radio.setChecked(true);
						}else if (day_night_val.equals(day_night[1])){
							night_radio.setChecked(true);
						}

						//TAGS
						mTagsContainer.setAutoCompleteValues(datasource.getDistinctTags());
						mTagsContainer.setTagsFromString(lastSession.getTags());

						//set Counted Activities
						String takeoffs_count = String.valueOf(lastSession.getTakeoffs());
						if (takeoffs_count.equals("0")) {
							takeoffs_count = "";
						}
						takeoffs_et.setText(takeoffs_count);

						String landings_count = String.valueOf(lastSession.getLandings());
						if (landings_count.equals("0")) {
							landings_count = "";
						}
						landings_et.setText(landings_count);

						String go_arounds_count = String.valueOf(lastSession.getGoArounds());
						if (go_arounds_count.equals("0")) {
							go_arounds_count = "";
						}
						go_arounds_et.setText(go_arounds_count);


						//set remarks
						remarks_et.setText(lastSession.getComments());


					}else{
						//put default values
						// if received duration from Quick Start Button
						if (qs_duration != 0){
							duration_et.setText(durationToHM(qs_duration));
						}else{
							Calendar cal = Calendar.getInstance();
							SimpleDateFormat sdf = new SimpleDateFormat(DateTimeConverter.DATE_SLASHED);
							String todayAsString = sdf.format(cal.getTime());
							date_et.setText(todayAsString);
							duration_et.setText("01:00");
						}
					}
				}catch(Exception e){
					Log.e("DB","Error: " + e);
				}
			} else {
				Log.w(LOG_TAG,"Populate Form doInBackground has returned false.");
			}
		}

		@Override
		protected void onCancelled() {
			mPopulateTask = null;
			showProgress(false,thisActivity);
		}
	}

	public void customTextInputSubmit(View view){
			mInputCustomTextWindow.setVisibility(View.GONE);
		}
	
	// TimePickerDialog Class
	public static class TimePickerFragment extends DialogFragment
							implements TimePickerDialog.OnTimeSetListener {

		private ActivityAddSession appState;
		private String title = "Start Time:";
		private EditText editTextObject;
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			//get Global Fragment Object Refernce
			 appState = ((ActivityAddSession)getActivity());
			 Log.v("ADD SESSION","Time - Fragment onCreate");

			 editTextObject = null; //appState.time_et;

			 int hour;
			 int minute;
			 String string_time = editTextObject.getText().toString();
			 //if no value in text box
			 if ((string_time == "")||(string_time == null)){
				// Use the current time as the default values for the picker
				final Calendar c = Calendar.getInstance();
				hour = c.get(Calendar.HOUR_OF_DAY);
				minute = c.get(Calendar.MINUTE);
			 }else{ //text box has value
				//extract date from string
				Log.v("ADD SESSION","Time - Parsing Value");
				Calendar cal = Calendar.getInstance();
				SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
				try {
					cal.setTime(sdf.parse(string_time));
					//set hour and minute
					hour = cal.get(Calendar.HOUR_OF_DAY);
					minute = cal.get(Calendar.MINUTE);
				} catch (java.text.ParseException e) {
					//time parse error - set hour and minute to current.
					e.printStackTrace();
					Log.e("ADD SESSION","Time Parse Error: " + e);
					final Calendar c = Calendar.getInstance();
					hour = c.get(Calendar.HOUR_OF_DAY);
					minute = c.get(Calendar.MINUTE);
				}

			 }

			// Create a new instance of TimePickerDialog and return it
			return new TimePickerDialog(getActivity(), this, hour, minute,
			true);

		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			// Set title for this dialog
			getDialog().setTitle(this.title);
			return container;
		}

		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			// Do something with the time chosen by the user
			Log.v("ADD SESSION","Time - Fragment onTimeSet");
			String str_minute;
			if (minute < 10) {str_minute = "0" + minute;}
			else {str_minute = "" + minute;}
			editTextObject.setText(hourOfDay + ":" + str_minute);
			appState.timePickerFragment = null;
			this.dismiss();
		}
	}

    public static class DatePickerFragment extends DialogFragment
                                implements DatePickerDialog.OnDateSetListener {
			private ActivityAddSession appState;
			private String title = "Date:";
			
			EditText editTextObject; 
			
			@Override
			public Dialog onCreateDialog(Bundle savedInstanceState) {
				//get Global Fragment Object Refernce	 
				 appState = ((ActivityAddSession)getActivity());
				 editTextObject = appState.date_et;
				// Use the current date as the default date in the picker
				 Log.v("ADD SESSION","Date - Fragment onCreate");
				
				int year;
				int month;
				int day;
					
				 String string_date = editTextObject.getText().toString();
				 //if no value in text box
				 if ((string_date == "")||(string_date == null)){
					// Use the current time as the default values for the picker
					 final Calendar c = Calendar.getInstance();
						year = c.get(Calendar.YEAR);
						month = c.get(Calendar.MONTH);
						day = c.get(Calendar.DAY_OF_MONTH);
				 }else{ //text box has value
					//extract date from string
					Calendar cal = Calendar.getInstance();
				    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
				    try {
				    	//set date to the value from text box
				    	cal.setTime(sdf.parse(string_date));
					    	year = cal.get(Calendar.YEAR);
							month = cal.get(Calendar.MONTH);
							day = cal.get(Calendar.DAY_OF_MONTH);
					} catch (java.text.ParseException e) {
						//time parse error - set hour and minute to current.
						e.printStackTrace();
						Log.w("ADD SESSION","Date Parse Error: " + e);
						final Calendar c = Calendar.getInstance();
							year = c.get(Calendar.YEAR);
							month = c.get(Calendar.MONTH);
							day = c.get(Calendar.DAY_OF_MONTH);
					}
				 }
				// Create a new instance of DatePickerDialog and return it
				return new DatePickerDialog(getActivity(), this, year, month, day);
			}
			
			@Override
		    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		        // Set title for this dialog
		        getDialog().setTitle(this.title);
				return container;
		    }
			
			public void onDateSet(DatePicker view, int year, int month, int day) {
				// Do something with the date chosen by the user
				editTextObject.setText(day + "/" + (month+1) + "/" + year);
				appState.datePickerFragment = null;
				this.dismiss();
			}
		}
		
    // TimePickerDialog Class
    public static class DurationPickerFragment extends DialogFragment
                            implements TimePickerDialog.OnTimeSetListener {

        private ActivityAddSession appState;
        private String title = "Duration:";
        private EditText editTextObject;
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            //get Global Fragment Object Refernce
             appState = ((ActivityAddSession)getActivity());
             Log.v("ADD SESSION","Duration - Fragment onCreate");
             editTextObject = appState.duration_et;

             int hour;
             int minute;
             String string_duration = editTextObject.getText().toString();
             //if no value in text box
             if (string_duration.length() < 0||(string_duration == null)){
                // put default values:
                hour = 1;
                minute = 0;
                Log.v("ADD SESSION","Duration (Time): default inputs loaded");
             }else{ //text box has value
                //extract date from string
                 Log.w("ADD SESSION","Time - Parsing Value");
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                try {
                    cal.setTime(sdf.parse(string_duration));
                    //set hour and minute
                    hour = cal.get(Calendar.HOUR_OF_DAY);
                    minute = cal.get(Calendar.MINUTE);
                } catch (java.text.ParseException e) {
                    //time parse error - set hour and minute to current.
                    e.printStackTrace();
                    Log.e("ADD SESSION","Duration (Time) Parse Error: " + e);
                    final Calendar c = Calendar.getInstance();
                    hour = c.get(Calendar.HOUR_OF_DAY);
                    minute = c.get(Calendar.MINUTE);
                }

             }

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
            true);

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            // Set title for this dialog
            getDialog().setTitle(this.title);
            return container;
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Do something with the time chosen by the user
            Log.i("ADD SESSION","Duraton - onTimeSet");
            String str_minute;
            if (minute < 10) {str_minute = "0" + minute;}
            else {str_minute = "" + minute;}
            editTextObject.setText(hourOfDay + ":" + str_minute);
            appState.durationPickerFragment = null;
            this.dismiss();
        }
    }
    public void showTimePickerDialog(View v) {
        if (timePickerFragment == null){
            timePickerFragment = new TimePickerFragment();
            timePickerFragment.show(getSupportFragmentManager(), "timePicker");
        }
    }

    public void showDurationPickerDialog(View v) {
        if (durationPickerFragment == null){
            Log.v("ADD SESSION","Duration - showDurationPickerDialog");
            durationPickerFragment = new DurationPickerFragment();
            durationPickerFragment.show(getSupportFragmentManager(), "timePicker");
        }
    }

    public void showDatePickerDialog(View v) {
        if (datePickerFragment == null){
            datePickerFragment = new DatePickerFragment();
            datePickerFragment.show(getSupportFragmentManager(), "datePicker");
        }
    }

    //Invoked on Submit button click
    public void formSubmit(View v){

            //Hide SoftKeybard
            InputMethodManager imm = (InputMethodManager)getSystemService(
                      Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(this.date_et.getWindowToken(), 0);

            //Validate Fields And Post Data to Server

            //Check if a pending Tag is not submitted
            AutoCompleteTextView addTagField = (AutoCompleteTextView) UIMessage.getViewsByTag(mTagsContainer,"addTagField").get(0);
            String unsubmittedTag = addTagField.getText().toString();
            if (unsubmittedTag.length() != 0){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Unsubmitted Tag")
                .setMessage("The Tag '" + unsubmittedTag + "' was entered but not added. \n Do you want to add it as a tag to this session?")

                .setNegativeButton("Close",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //Add the Tag to the Container
                        AutoCompleteTextView addTagField = (AutoCompleteTextView) UIMessage.getViewsByTag(mTagsContainer, "addTagField").get(0);
                        mTagsContainer.addTag(addTagField.getText().toString());
                        attemptSubmit();
                    }
                });

                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                      public void onClick(DialogInterface dialog, int whichButton) {
                        // Do Nothing ( The Tag will not be Added)
                          attemptSubmit();
                      }
                    });

                AlertDialog alert = builder.create();
                alert.show();
            }else{
                //There is no Unsubmitted Tag Pending.
                //So Just go On with Adding a Session
                attemptSubmit();
            }


        }

    @SuppressWarnings("unchecked")
    public void attemptSubmit() {
            if (mAddSessionTask != null) {
                return;
            }



        boolean inputs_ok = check_inputs();
        if (inputs_ok){
            // Show a progress spinner, and kick off a background task to
            // perform the ADD SESSION TASK attempt.
            showProgress(true,thisActivity);

            mAddSessionTask = new AddSessionTask(this);
            mAddSessionTask.execute();


        }else{
            //focusView.requestFocus();
        }

    }

    /**
     * This Method Validates the form Inputs
     * and displays message to user if error found
     * @return true if they are correct format, false otherwise
     *
     */
    private boolean check_inputs(){
        //set up toast values
        int toast_duration = Toast.LENGTH_SHORT;
        Context context = getApplicationContext();
        Toast toast = null;
        CharSequence message = null;

        boolean validate = true;


        // get fields values


        String date = date_et.getText().toString();
        String duration = duration_et.getText().toString();

        int sim_actual_selected_id = sim_actual_radio.getCheckedRadioButtonId();
        int day_night_selected_id = day_night_radio.getCheckedRadioButtonId();

        String takeoffs = takeoffs_et.getText().toString();
        String landings = landings_et.getText().toString();
        String go_arounds = go_arounds_et.getText().toString();


        //set up regex and formats
        final String REGEX_INPUT_DATE = "(0?[1-9]|[12][0-9]|3[01])/(0?[1-9]|1[012])/((19|20)\\d\\d)";
        final String REGEX_INPUT_TIME = "([01]?[0-9]|2[0-3]):[0-5][0-9]";
        final String REGEX_ONLY_NUMS = "^[0-9]*$";

        Pattern pattern_date = Pattern.compile(REGEX_INPUT_DATE);
        Pattern pattern_time = Pattern.compile(REGEX_INPUT_TIME);
        Pattern pattern_numbers = Pattern.compile(REGEX_ONLY_NUMS);

        Matcher date_matcher = pattern_date.matcher(date);
        Matcher duration_matcher = pattern_time.matcher(duration);

        Matcher takeoffs_matcher = pattern_numbers.matcher(takeoffs);
        Matcher landings_matcher = pattern_numbers.matcher(landings);
        Matcher go_arounds_matcher = pattern_numbers.matcher(go_arounds);

        //Check inputs and show errors
        if (date.length() == 0){ //no date input
            message = getResources().getString(R.string.error_date_not_specified);
            date_et.setError(message);
            date_et.requestFocus();
            validate = false;
        }else if (!date_matcher.matches()){//date in wrong format
            message = getResources().getString(R.string.error_date_wrong_format);
            date_et.setError(message);
            date_et.requestFocus();
            validate = false;
        }else if (duration.length() == 0){//no duration input
            message = getResources().getString(R.string.error_duration_not_specified);
            duration_et.setError(message);
            duration_et.requestFocus();
            validate = false;
        }else if (!duration_matcher.matches()){//duration wrong format
            message = getResources().getString(R.string.error_duration_wrong_format);
            duration_et.setError(message);
            duration_et.requestFocus();
            validate = false;
        }else if (sim_actual_selected_id == -1){//No Selection was made for Sim / Actual Field
            message = getResources().getString(R.string.error_sim_actual_not_selected);
            sim_radio.requestFocus();
            validate = false;
        }else if (day_night_selected_id == -1){//No Selection was made for Sim / Actual Field
            message = getResources().getString(R.string.error_day_night_not_selected);
            day_radio.requestFocus();
            validate = false;
        }else if (!takeoffs_matcher.matches()&&(takeoffs.length() > 0)){ //takeoffs not a number (and not empty)
            message = getResources().getString(R.string.error_takeoffs_input_not_a_number);
            takeoffs_et.setError(getResources().getString(R.string.error_only_numbers_allowed));
            takeoffs_et.requestFocus();
            validate = false;
        }else if (!landings_matcher.matches()&&(landings.length() > 0)){ // landings not a number (and not empty)
            message = getResources().getString(R.string.error_landings_input_not_a_number);
            landings_et.setError(getResources().getString(R.string.error_only_numbers_allowed));
            landings_et.requestFocus();
            validate = false;
        }else if (!go_arounds_matcher.matches()&&(go_arounds.length() > 0)){ // go arounds not a number (and not empty)
            message = getResources().getString(R.string.error_go_arounds_input_not_a_number);
            go_arounds_et.setError(getResources().getString(R.string.error_only_numbers_allowed));
            go_arounds_et.requestFocus();
            validate = false;
        }

        if (validate){
            return true;
        }else{
            Log.e(LOG_TAG,"Form validation failed: " + message);
            toast = Toast.makeText(context, message, toast_duration);
            toast.show();
            return false;
        }
    }

    private class AddSessionTask extends AsyncTask<String, String, Boolean> {
        private final String LOG_TAG = "AddSessionTask";
        private Context context;
        private Session session;
        public AddSessionTask(Context context){
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(String... params_list) {

            session = new Session();


            session.setDate(getDate());
            session.setDuration(getDuration());
            session.setDayNight(getDayNight());

            StringValuePair platformTypeAndVariation = getPlatformTypeAndVariationPair();
            session.setPlatformType(platformTypeAndVariation.getFirst());
            session.setPlatformVariation(platformTypeAndVariation.getSecond());

            StringValuePair regAndTailNumber = getRegAndTailNumberPair();
            session.setRegistration(regAndTailNumber.getFirst());
            session.setTailNumber(regAndTailNumber.getSecond());

            StringValuePair icaoAndName = getIcaoAndNamePair();
            session.setICAO(icaoAndName.getFirst());
            session.setAerodromeName(icaoAndName.getSecond());


            session.setCommand(getCommand());
            session.setSeat(getSeat());
            session.setSimActual(getSimOrActual());
            session.setFlightType(getFlightType());
            session.setTags(getTags());
            session.setTakeoffs(getTakeoffs());
            session.setLandings(getLandings());
            session.setGoArounds(getGoArounds());
            session.setComments(getRemarks());


            if (isEditMode())
                return attemptUpdateSession();
            else
                return attemptAddSession();


        }

        @NonNull
        private Boolean attemptAddSession() {
            Log.i(LOG_TAG, "User Attempt Add Session");
            session.setId(datasource.addSession(session));
            return true;
        }

        private boolean attemptUpdateSession() {
            Log.i(LOG_TAG, "User Attempt Edit Session (id=" + editSessionId + ")");
            session.setId(editSessionId);
            return datasource.updateSession(session);
        }

        @NonNull
        private String getRemarks() {
            return remarks_et.getText().toString();
        }

        private long getTakeoffs() {
            String takeoffs = takeoffs_et.getText().toString();
            if (takeoffs.isEmpty()){
                takeoffs = "0";
            }
            return Long.parseLong(takeoffs);
        }

        private long getLandings() {
            String landings = landings_et.getText().toString();
            if (landings.isEmpty()){
                landings = "0";
            }
            return Long.parseLong(landings);
        }

        private long getGoArounds() {
            String goArounds = go_arounds_et.getText().toString();
            if (goArounds.isEmpty()){
                goArounds = "0";
            }
            return Long.parseLong(goArounds);
        }

        private String getTags() {
            return mTagsContainer.getTagsString();
        }

        private String getFlightType() {
            return flight_type_spn.getSelectedItem().toString();
        }

        private String getSimOrActual() {
            int sim_actual_selected_id = sim_actual_radio.getCheckedRadioButtonId();
            RadioButton sim_actual_sel_btn = (RadioButton) findViewById(sim_actual_selected_id);

            String sim_actual = null;
            if (sim_actual_sel_btn != null){
                return sim_actual_sel_btn.getText().toString();
            }else{
                Log.e(LOG_TAG, "No input for Sim / Actual");
                return "";
            }
        }

        private String getSeat() {
            return seat_spn.getSelectedItem().toString();
        }

        private String getCommand() {
            return command_spn.getSelectedItem().toString();
        }

        private StringValuePair getIcaoAndNamePair() {
            //Get Custom Location
            String loc_icao = "";
            String loc_name = "";

            int loc_ind = location_spn.getSelectedItemPosition();
            if(loc_ind==location_spn.getCount()-1){
                //New Location Entered
                loc_icao = loc_icao_et.getText().toString();
                loc_name = loc_name_et.getText().toString();
            }else{
                //Preset Location Selected
                loc_icao = location_li_icao_ind.get(loc_ind);
                loc_name = location_li_name_ind.get(loc_ind);
            }
            return new StringValuePair(loc_icao,loc_name);
        }

        private StringValuePair getRegAndTailNumberPair() {
            String reg_no = "";
            String tail_no = "";

            int reg_n_tail_ind = reg_n_tail_spn.getSelectedItemPosition();
            if(reg_n_tail_ind == reg_n_tail_spn.getCount()-1){
                //New Registration Entered
                reg_no = reg_no_et.getText().toString();
                tail_no = tail_no_et.getText().toString();
            }else{
                reg_no = reg_li.get(reg_n_tail_ind);
                tail_no = tail_li.get(reg_n_tail_ind);
            }
            return new StringValuePair(reg_no, tail_no);
        }


        private StringValuePair getPlatformTypeAndVariationPair(){
            String platformType = "";
            String platformVariation = "";

            int platfomrSelectedIndex = platform_spn.getSelectedItemPosition();

            if(isNewPlatformEntered()) {
                platformType = plat_type.getText().toString();
                platformVariation = plat_variation.getText().toString();
            }else {
                platformType = plat_type_li.get(platfomrSelectedIndex);
                platformVariation = plat_variation_li.get(platfomrSelectedIndex);
            }

            return new StringValuePair(platformType, platformVariation);

        }

        private boolean isNewPlatformEntered() {
            return platform_spn.getSelectedItemPosition() == platform_spn.getCount()-1;
        }

        private String getDayNight() {
            int day_night_selected_id = day_night_radio.getCheckedRadioButtonId();
            RadioButton day_night_sel_btn = (RadioButton) findViewById(day_night_selected_id);

            String day_night = null;
            if (day_night_sel_btn != null){
                return day_night_sel_btn.getText().toString();
            }else{
                Log.e(LOG_TAG, "No input for Day / Night");
                return "";
            }
        }

        @NonNull
        private String getDuration() {
            String durationString = duration_et.getText().toString();

            Duration d = new Duration();
            d.setString(durationString);
            return d.getISO8601();
        }

        private String getDate() {
            String date = "";

            try{
                date = DateTimeConverter.format(date_et.getText().toString(), DateTimeConverter.DATE_SLASHED, DateTimeConverter.ISO8601);
            }catch(Exception e){
                Log.e(LOG_TAG, "Date parsing failed: " + e);
            }
            return date;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAddSessionTask = null;
            showProgress(false, thisActivity);

            if (success) {
                mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Sessions")
                .setAction("Add")
                .build());
                thisActivity.finish();
            } else {

                Log.e(LOG_TAG,"Error: " + err_msg);
                UIMessage.makeToast(context, getResources().getString(R.string.error_unknown));
                mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Sessions")
                .setAction("Add Failed")
                .build());
                thisActivity.finish();
            }
        }

        @Override
        protected void onCancelled() {
            mAddSessionTask = null;
            showProgress(false,thisActivity);
        }
    }

    /**
     * Shows the progress UI and hides the register form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show, Context context) {
        if (show){
            pDialog = ProgressDialog.show(context, "", getResources().getString(R.string.please_wait_progress), true);
        }else{
            pDialog.dismiss();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void updateRegAadTail(){
		    //Disable Filter Method if this is the first 
		    //platform selection since it's made by the populate Task
		    if (first_platform_spinner_selection){
		        first_platform_spinner_selection = false;
		        return;
	        }
		    
		    String platform_type;
		    String platform_variation;
		    
		    int plt_ind = platform_spn.getSelectedItemPosition();
            if(plt_ind == platform_spn.getCount()-1){
                //New Platform Entered
                platform_type = plat_type.getText().toString();
                platform_variation = plat_variation.getText().toString();
            }else{
                platform_type = plat_type_li.get(plt_ind);
                platform_variation = plat_variation_li.get(plt_ind);
            }
            
            
		    //Clear Lists
		    reg_n_tail_li.clear();
		    reg_li.clear();
		    tail_li.clear();

		    
		    
		  //Aircraft Registration and Tail Number		    
            ArrayList<NameValuePair> reg_n_tail_pairs = datasource.getTailAndRegForPlatform(platform_type, platform_variation);
            if (!reg_n_tail_pairs.isEmpty()){
                for (NameValuePair entry : reg_n_tail_pairs){
                    String reg = entry.getName();
                    String tail_no = entry.getValue();
                    String reg_n_tail = reg + " " + tail_no;
                    if ((!reg_n_tail.trim().isEmpty())&&(!reg_n_tail_li.contains(reg_n_tail))){
                        reg_n_tail_li.add(reg_n_tail);
                        reg_li.add(reg);
                        tail_li.add(tail_no);
                    }
                }
            }
            
            //add custom Text option
            String item = getResources().getString(R.string.input_add_custom);
            reg_n_tail_li.add(item);
            
            //refresh Reg And Tail DropDown List
            reg_n_tail_spn.setAdapter(reg_n_tail_adp);
		}
		
	private String durationToHM(long millis)
    {
    	long hours =  TimeUnit.MILLISECONDS.toHours(millis);
		long minutes =  TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1);
		
		return String.format("%02d:%02d",hours , minutes );
    }
	 private void setTodaysDate()
	 {
		 Calendar cal = Calendar.getInstance(); 
		    SimpleDateFormat sdf = new SimpleDateFormat(DateTimeConverter.DATE_SLASHED);
		    String todayAsString = sdf.format(cal.getTime());
	        date_et.setText(todayAsString);
	 }

}