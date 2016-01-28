package com.gilvitzi.uavlogbookpro.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.gilvitzi.uavlogbookpro.R;

import java.util.ArrayList;
import java.util.List;

public class TagsContainer extends LinearLayout {

	ArrayList<Tag> tags;
	AutoCompleteTextView addTagField;
	Context context;
	
	//Styling Resources Reference
	protected int tagResourceID = R.drawable.tag;
	protected int addNewTagFieldResourceID = R.drawable.tag_add_new;
	protected int deleteButtonResourceID = R.drawable.tag_delete_button;
	
	public TagsContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
        constructView(context);
	}

    private void constructView(Context context) {
        init(context);
        createAddTagButton(context);
        setAddTagFieldListeners();
        this.addView(addTagField);
    }

    private void init(Context context) {
        this.context = context;
        tags = new ArrayList<Tag>();
        this.setOrientation(LinearLayout.VERTICAL);
    }

    private void setAddTagFieldListeners() {
        addTagField.setOnEditorActionListener(new TagsContainerOnEditorActionListener());
        addTagField.setOnKeyListener(new TagsContainerOnKeyListener());
        addTagField.setOnFocusChangeListener(new TagsContainerOnFocusChangeListener());

    }

    private void createAddTagButton(Context context) {
        addTagField = new AutoCompleteTextView(context);

        setAddTagFieldLayoutParams();

        addTagField.setTag("addTagField");
        addTagField.setHint(context.getResources().getString(R.string.hint_add_new_tag));
        addTagField.setBackgroundResource(addNewTagFieldResourceID);
        addTagField.setTextColor(getResources().getColor(R.color.black));
        addTagField.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        addTagField.setImeOptions(EditorInfo.IME_ACTION_GO);
        addTagField.setImeActionLabel("Add Tag", KeyEvent.KEYCODE_ENTER);
    }

    private void setAddTagFieldLayoutParams() {
        LayoutParams params = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.topMargin = 5;
        params.bottomMargin = 5;
        params.gravity= Gravity.RIGHT;

        addTagField.setLayoutParams(params);
    }




    private void addNewInputAsTag() {
        this.addTag(addTagField.getText().toString());
        addTagField.setText("");
    }

    private boolean isInputFieldEmpty() {
        return addTagField.getText().toString().matches("");
    }

    public void setAutoCompleteValues(List<String> li){
        ArrayAdapter<String> adp = new ArrayAdapter<String>(context,R.layout.autocomplete_dropdown,li);
        		adp.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        		addTagField.setThreshold(1);
        		addTagField.setAdapter(adp);
	}
	
	public void addTag(String value){
		Tag tag = new Tag(context,value);
		tags.add(tag);
		this.addView(tag);
	}
	
	public void remove(Tag tag){
		tags.remove(tag);
		TagsContainer.this.removeView(tag);
	}
	
	public void clearAllTags(){
		for (Tag tag : tags){
			tags.remove(tag.getValue());
			TagsContainer.this.removeView(tag);
		}
	}

	public ArrayList<Tag> getTags(){
		return this.tags;
	}
	
	public String getTagsString(){
		String str = "";
		for (Tag item : tags){
			str += item.getValue() + ";";
		}
		return str;
	}
	
	public void setTagsFromString(String str){
		String[] tagsArray = str.split(";");
		clearAllTags();
		for (int i = 0;i<tagsArray.length;i++){
			if (tagsArray[i]!=""){
				this.addTag(tagsArray[i]);
			}
		}
	}
	
	private class Tag extends RelativeLayout {
		LinearLayout.LayoutParams params;
		TextView textView;
		LinearLayout tagLayout;
		
		public Tag(Context context) {
			super(context);
			setParams();
		}
		
		public Tag(Context context, String value) {
			super(context);
			
            initLayout(context);
            addDeleteButton(context);
            addTextView(context, value);

			setParams();
		}

        private void addTextView(Context context, String value) {
            params = new LinearLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.topMargin = 0;
            params.bottomMargin = 0;
            params.leftMargin = 5;
            params.gravity = Gravity.LEFT;
            textView = new TextView(context);
            textView.setText(value);
            textView.setLayoutParams(params);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            this.addView(textView);
        }

        private void addDeleteButton(Context context) {
            Button btnRemove = new Button(context);
            LayoutParams btn_params = getDeleteButtonLayout();
            setDeleteButtonSize(btn_params);
            btnRemove.setLayoutParams(btn_params);
            setDeleteButtonStyle(btnRemove);
            setDeleteButtonOnClickListener(btnRemove);

            this.addView(btnRemove);
        }

        private void setDeleteButtonOnClickListener(Button btnRemove) {
            btnRemove.setOnClickListener( new OnClickListener() {
                @Override
                public void onClick(View view) {
                    Tag tag = (Tag)view.getParent();
                    TagsContainer container = (TagsContainer)tag.getParent();
                    container.remove((Tag)tag);
                }
            });
        }

        private void setDeleteButtonStyle(Button btnRemove) {
            btnRemove.setBackgroundResource(deleteButtonResourceID);
            btnRemove.setText("x");
            btnRemove.setPadding(0, 0, 0, 0);
        }

        @NonNull
        private LayoutParams getDeleteButtonLayout() {
            LayoutParams btn_params = new LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            btn_params.topMargin = 0;
            btn_params.bottomMargin = 0;
            btn_params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            return btn_params;
        }

        private void setDeleteButtonSize(LayoutParams btn_params) {
            int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    (float) 32, getResources().getDisplayMetrics());
            btn_params.width = size;
            btn_params.height = size;
        }

        private void initLayout(Context context) {
            params = new LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            params.topMargin = 5;
            params.bottomMargin = 5;
            tagLayout = new LinearLayout(context);
            tagLayout.setLayoutParams(params);
        }

        private void setParams(){
			this.setBackgroundResource(tagResourceID);
			params = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
			params.topMargin = 5;
			params.bottomMargin = 5;
	        params.gravity=Gravity.TOP;
	        this.setLayoutParams(params);
	        
		}
		
		@SuppressWarnings("unused")
		public void setValue(String value){
			this.textView.setText(value);
		}
		
		public String getValue(){
			return this.textView.getText().toString();
		}
		
	}

    private class TagsContainerOnEditorActionListener implements OnEditorActionListener {
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (isActionGoOrEnter(actionId)) {
                if (!isInputFieldEmpty())
                    addNewInputAsTag();

                return true;
            }
            return false;
        }

        private boolean isActionGoOrEnter(int actionId)
        {
            return  keyboardGoPressed(actionId)|| enterPressed(actionId);
        }

        private boolean enterPressed(int actionId) {
            return actionId == KeyEvent.KEYCODE_ENTER;
        }

        private boolean keyboardGoPressed(int actionId) {
            return actionId == EditorInfo.IME_ACTION_GO;
        }
    }

    private class TagsContainerOnKeyListener implements OnKeyListener {
        public boolean onKey(View v, int keyCode, KeyEvent event){
            if (event.getAction() == KeyEvent.ACTION_DOWN
            &&  isDPADCenterOrEnter(keyCode)){
                 if (!isInputFieldEmpty())
                    addNewInputAsTag();

                return true;
            }
            return false;
        }

        private boolean isDPADCenterOrEnter(int keyCode)
        {
            return (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER);
        }
    }

    private class TagsContainerOnFocusChangeListener implements OnFocusChangeListener {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (!isInputFieldEmpty()){
                addNewInputAsTag();
            }
        }
    }
}
