package com.example.onyx.onyx.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import com.example.onyx.onyx.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class toggleFragment extends Fragment {

    onToggleSetListener mOnToggleSetListener;

    public interface onToggleSetListener {
        void onToggleSet(JSONArray style);
    }

    public static toggleFragment newInstance (){
        return new toggleFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_toggle, container, false);
        return fragmentView;
    }

    public void onAttachToParentFragment (Fragment frag) {
        try{
            mOnToggleSetListener = (onToggleSetListener) frag;
        }
        catch (ClassCastException e) {

        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onAttachToParentFragment(getParentFragment());
    }

    @Override
    public void onStart() {
        super.onStart();
        final CheckBox attractions = getView().findViewById(R.id.checkBoxAttractions);
        final CheckBox government = getView().findViewById(R.id.checkBoxGovernment);
        final CheckBox medical = getView().findViewById(R.id.checkBoxMedical);
        final CheckBox park = getView().findViewById(R.id.checkBoxParks);
        final CheckBox worship = getView().findViewById(R.id.checkBoxWorship);
        final CheckBox school = getView().findViewById(R.id.checkBoxSchools);
        final CheckBox sports = getView().findViewById(R.id.checkBoxSport);
        final CheckBox transit = getView().findViewById(R.id.checkBoxTransit);
        Button toggleButton = getView().findViewById(R.id.buttonToggle);
        Button cancelButton = getView().findViewById(R.id.buttonCancel);

        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, Boolean> toggleRequests = new HashMap<>();
                toggleRequests.put("poi.attractions",attractions.isChecked());
                toggleRequests.put("poi.government",government.isChecked());
                toggleRequests.put("poi.medical",medical.isChecked());
                toggleRequests.put("poi.park",park.isChecked());
                toggleRequests.put("poi.place_of_worship",worship.isChecked());
                toggleRequests.put("poi.school",school.isChecked());
                toggleRequests.put("poi.sports_complex",sports.isChecked());
                toggleRequests.put("poi.transit",transit.isChecked());
                createJSON(toggleRequests);
                getParentFragment().getChildFragmentManager().beginTransaction().
                        remove(toggleFragment.this).commit();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getParentFragment().getChildFragmentManager().beginTransaction().
                        remove(toggleFragment.this).commit();
            }
        });
    }

    public void createJSON(Map<String, Boolean> toggleRequests) {

        JSONObject POI = new JSONObject();
        JSONArray stylers = new JSONArray();
        JSONObject style = new JSONObject();
        JSONArray outArray = new JSONArray();
        try{
            style.put("visibility", "off");
        }
        catch (JSONException e){

        }

        stylers.put(style);
        for (Map.Entry<String,Boolean> entry : toggleRequests.entrySet()){
            if(entry.getValue()){
                try{
                    POI.put("featureType",entry.getKey());
                    POI.put("stylers",stylers);
                    outArray.put(POI);
                    POI.remove("featureType");
                    POI.remove("stylers");
                }
                catch (JSONException e){

                }
            }
        }

        mOnToggleSetListener.onToggleSet(outArray);
    }
}
