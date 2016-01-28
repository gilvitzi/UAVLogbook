package com.gilvitzi.uavlogbookpro.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.gilvitzi.uavlogbookpro.R;
import com.gilvitzi.uavlogbookpro.model.Aerodrome;

import java.util.ArrayList;
import java.util.Locale;

public class AerodromeNameListAdapter extends BaseAdapter implements Filterable{
    //Main data structure
    private ArrayList<Aerodrome> aerodromes;
    private ArrayList<Aerodrome> filteredAerodromes;
    private Context context;
    private AerodromesListAdapterFilter adapterFilter;
    
    public AerodromeNameListAdapter(ArrayList<Aerodrome> aerodromes, Context context) {
        this.aerodromes = aerodromes;
        this.context = context;
    }

    @Override
    public int getCount() {
        return filteredAerodromes.size();
    }

    @Override
    public Object getItem(int position) {
        return filteredAerodromes.get(position).getAerodromeName();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        View v = view;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.aerodrome_list_item_name, null);
        }

        TextView icaoView = (TextView) v
                .findViewById(R.id.list_item_icao);
        TextView aerodromeNameView = (TextView) v
                .findViewById(R.id.list_item_aerodrome_name);

        final Aerodrome aerodrome = filteredAerodromes.get(position);

        // Set ICAO
        icaoView.setText(aerodrome.getICAO());

        // Set Aerodrome Name
        aerodromeNameView.setText(aerodrome.getAerodromeName());

        return v;
    }
    
    @Override
    public Filter getFilter() {
        if (adapterFilter == null)
            adapterFilter = new AerodromesListAdapterFilter();
        return adapterFilter;
    }

    // Class enabling the filtering of this adapter
    private class AerodromesListAdapterFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if (constraint == null || constraint.length() == 0) {
                results.values = null;
                results.count = 0;
            } else {
                ArrayList<Aerodrome> filteredAerodromeList = new ArrayList<Aerodrome>();
                for (Aerodrome aerodrome : aerodromes) {
                    if (aerodrome.getAerodromeName()
                            .toLowerCase(Locale.getDefault())
                            .contains(
                                    constraint.toString().toLowerCase(
                                            Locale.getDefault()))) {
                        filteredAerodromeList.add(aerodrome);
                    }
                }
                results.values = filteredAerodromeList;
                results.count = filteredAerodromeList.size();
            }
            return results;
        }
            
        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results.count == 0)
                notifyDataSetInvalidated();
            else {
                filteredAerodromes = (ArrayList<Aerodrome>) results.values;
                notifyDataSetChanged();
            }
            
        }
    }
}
