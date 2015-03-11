package edu.ucsd.calab.extrasensory.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import edu.ucsd.calab.extrasensory.ESApplication;
import edu.ucsd.calab.extrasensory.R;
import edu.ucsd.calab.extrasensory.data.ESLabelStrings;

/**
 * This class manages selecting labels from a list.
 * It provides additional features, as index by subjects, search and label suggestions.
 *
 * To use this activity:
 * 1) You must prepare a proper intent, containing the extra string key LIST_TYPE_KEY,
 * with int value of one of the LIST_TYPE_... constants defined here.
 *
 * 2) In addition, the intent can contain more extra keys:
 * PRESELECTED_LABELS_KEY - with value of string[] of the currently selected labels.
 *
 * 3) Start this activity with startActivityForResult() function, to signal that you are expecting a result.
 *
 * 4) When the user is done with this activity, it sets the result and finishes.
 * Then you can catch the result by implementing onActivityResult() and checking the requestCode is the one you started the activity with.
 * In onActivityResult() you'll get the "response" intent with the results from this activity.
 * Specifically, the result intent should contain a key SELECTED_LABELS_OUTPUT_KEY with a value of string[] with the selected labels.
 */
public class SelectionFromListActivity extends BaseActivity {

    private static final String LOG_TAG = "[SelectionFromListActivity]";

    public static final String LIST_TYPE_KEY = "edu.ucsd.calab.extrasensory.key.list_type";
    public static final String PRESELECTED_LABELS_KEY = "edu.ucsd.calab.extrasensory.key.preselected_labels";
    public static final String FREQUENTLY_USED_LABELS_KEY = "edu.ucsd.calab.extrasensory.key.frequently_used_labels";

    public static final String SELECTED_LABELS_OUTPUT_KEY = "edu.ucsd.calab.extrasensory.key.selected_labels";

    private static final int LIST_TYPE_MISSING = -1;
    public static final int LIST_TYPE_MAIN_ACTIVITY = 1;
    public static final int LIST_TYPE_SECONDARY_ACTIVITIES = 2;
    public static final int LIST_TYPE_MOODS = 3;


    private ListView _choicesListView;
    private LinearLayout _sideIndex;
    private String[] _labelChoices;
    private HashSet<String> _selectedLabels;
    private Map<String,String[]> _labelsPerSubject;
    private List<String> _frequentlyUsedLabels;
    private boolean _allowMultiSelection = false;
    private boolean _useIndex = false;
    private View.OnClickListener _onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            TextView textView = (TextView)view.findViewById(R.id.text_label_name_in_selection_choice);
            String clickedLabel = textView.getText().toString();
            if (_selectedLabels.contains(clickedLabel)) {
                // Then this click was to de-select this label:
                _selectedLabels.remove(clickedLabel);
            }
            else {
                // Then this click was to select this label:
                if (!_allowMultiSelection) {
                    // First empty other selected labels:
                    _selectedLabels.clear();
                }
                // Add the selected label:
                _selectedLabels.add(clickedLabel);
            }

            // After re-arranging the selected labels, refresh the list:
            refreshListContent();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection_from_list);

        Intent inputParameters = getIntent();
        if (!inputParameters.hasExtra(LIST_TYPE_KEY)) {
            Log.e(LOG_TAG,"Selection from list was started without specifying type of list");
            finish();
            return;
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Set the view according to the parameters:
        int listType = inputParameters.getIntExtra(LIST_TYPE_KEY,LIST_TYPE_MISSING);
        switch (listType) {
            case LIST_TYPE_MAIN_ACTIVITY:
                _labelChoices = ESLabelStrings.getMainActivities();
                _allowMultiSelection = false;
                _useIndex = false;
                break;
            case LIST_TYPE_SECONDARY_ACTIVITIES:
                _labelChoices = ESLabelStrings.getSecondaryActivities();
                _labelsPerSubject = ESLabelStrings.getSecondaryActivitiesPerSubject();
                _allowMultiSelection = true;
                _useIndex = true;
                break;
            case LIST_TYPE_MOODS:
                _labelChoices = ESLabelStrings.getMoods();
                _allowMultiSelection = true;
                _useIndex = true;
                break;
            default:
                Log.e(LOG_TAG,"Unsupported list type received: " + listType);
                finish();
                return;
        }

        if (inputParameters.hasExtra(PRESELECTED_LABELS_KEY)) {
            String[] preselected = inputParameters.getStringArrayExtra(PRESELECTED_LABELS_KEY);
            _selectedLabels = new HashSet<>(preselected.length);
            for (int i=0; i < preselected.length; i ++) {
                _selectedLabels.add(preselected[i]);
            }
        }
        else {
            _selectedLabels = new HashSet<>(10);
        }

        _choicesListView = (ListView)findViewById(R.id.listview_selection_choices_list);
        _sideIndex = (LinearLayout)findViewById(R.id.linearlayout_selection_side_index);

        if (!_useIndex) {
            ViewGroup.LayoutParams params = _choicesListView.getLayoutParams();
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            _choicesListView.setLayoutParams(params);
        }


        refreshListContent();
    }

    private void refreshListContent() {

        _sideIndex.removeAllViews();

        ArrayList<ChoiceItem> itemsList = new ArrayList<>(10);
        //TODO: if needed add selected section and frequently used section (make sure it's precalculated)
        if (_useIndex && !_selectedLabels.isEmpty()) {
            //TODO: add section (with header) of selected plus index item
        }
        if (_useIndex && _frequentlyUsedLabels != null && !_frequentlyUsedLabels.isEmpty()) {
            //TODO: add frequent section + index
        }

        if (_labelsPerSubject != null) {
            for (String subject : _labelsPerSubject.keySet()) {
                final int nextRowInd = itemsList.size();
                // Add subject header:
                itemsList.add(new ChoiceItem(subject, true));
                if (_useIndex) {
                    TextView indexItem = new TextView(this);
                    indexItem.setText(subject);
                    indexItem.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            jumpToRow(nextRowInd);
                        }
                    });
                    indexItem.setPadding(0,10,0,10);
                    _sideIndex.addView(indexItem);
                }

                // Add the subject's labels:
                for (String label : (String[])_labelsPerSubject.get(subject)) {
                    itemsList.add(new ChoiceItem(label));
                }
            }
        }
        else {
            for (int i=0; i < _labelChoices.length; i ++) {
                itemsList.add(new ChoiceItem(_labelChoices[i]));
            }
        }

        setAdapterChoices(itemsList);
    }

    private void jumpToRow(int row) {
        ListView choicesListView = (ListView)findViewById(R.id.listview_selection_choices_list);
        choicesListView.setSelection(row);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_selection_from_list, menu);
        _optionsMenu = menu;
        checkRecordingStateAndSetRedLight();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_done_selecting_from_list:
                returnSelectedLabels();
                break;
            default://do nothing
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Call this function when user presses "done" button.
     * This function will return the selected items list to whoever called this activity.
     */
    private void returnSelectedLabels() {
        Intent selectedLabelsIntent = new Intent();
        String[] returnedSelectedLabels = new String[_selectedLabels.size()];
        returnedSelectedLabels = _selectedLabels.toArray(returnedSelectedLabels);
        selectedLabelsIntent.putExtra(SELECTED_LABELS_OUTPUT_KEY,returnedSelectedLabels);

        setResult(Activity.RESULT_OK,selectedLabelsIntent);
        finish();
    }



    private static class ChoiceItem {
        public String _label;
        public boolean _isSectionHeader;
        public ChoiceItem(String label,boolean isSectionHeader) {
            _label = label;
            _isSectionHeader = isSectionHeader;
        }
        public ChoiceItem(String label) {
            this(label,false);
        }

        @Override
        public String toString() {
            return _label;
        }
    }

    private void setAdapterChoices(ArrayList<ChoiceItem> itemsList) {
        ChoicesListAdapter adapter = (ChoicesListAdapter)_choicesListView.getAdapter();
        if (adapter == null) {
            // Then initialize the list's adapter:
            adapter = new ChoicesListAdapter(itemsList,this);
            _choicesListView.setAdapter(adapter);
        }
        else {
            // Simply update the existing adapter's values:
            adapter.resetChoiceItems(itemsList);
        }
    }

    private static class ChoicesListAdapter extends ArrayAdapter<ChoiceItem> {

        private SelectionFromListActivity _handler;
        private List<ChoiceItem> _items;

        public ChoicesListAdapter(List<ChoiceItem> items,SelectionFromListActivity handler) {
            super(ESApplication.getTheAppContext(), R.layout.row_in_selection_from_list, R.id.text_label_name_in_selection_choice, items);
            _handler = handler;
            _items = items;
        }

        public void resetChoiceItems(List<ChoiceItem> items) {
            _items.clear();
            _items.addAll(items);
            notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView =  super.getView(position,convertView,parent);
            ChoiceItem item = getItem(position);
            ImageView imageView = (ImageView)rowView.findViewById(R.id.image_mark_for_selection_choice);
            if (item._isSectionHeader) {
                rowView.setBackgroundColor(Color.BLUE);
                imageView.setImageBitmap(null);
                rowView.setEnabled(false);
                rowView.setOnClickListener(null);
                return rowView;
            }

            rowView.setBackgroundColor(Color.WHITE);
            rowView.setEnabled(true);
            rowView.setOnClickListener(_handler._onClickListener);

            if (_handler._selectedLabels.contains(item._label)) {
                imageView.setImageResource(R.drawable.checkmark_in_circle);
            }
            else {
                imageView.setImageBitmap(null);
            }

            return rowView;
        }
    }
}
