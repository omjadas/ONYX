package com.example.onyx.onyx.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;

//For UI
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import com.example.onyx.onyx.R;

//For writing to files
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

//For reading files
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;


public class toggleFragment extends Fragment {

    public static final String ARG_TYPE = "type";
    public static final String TYPE_ALL = "type_all";

    //Create new instance of toggle fragment
    public static toggleFragment newInstance(String type) {
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        toggleFragment fragment = new toggleFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_toggle, container, false);
        return fragmentView;
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
        /* See if file already exists
           If it does, organise it to reflect the user's current settings
         */
        try {
            FileInputStream stream = getActivity().getApplicationContext().openFileInput("toggleMap");
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    if (!line.contains("poi.attraction"))
                        attractions.setChecked(true);
                    if (!line.contains("poi.government"))
                        government.setChecked(true);
                    if (!line.contains("poi.medical"))
                        medical.setChecked(true);
                    if (!line.contains("poi.park"))
                        park.setChecked(true);
                    if (!line.contains("poi.place_of_worship"))
                        worship.setChecked(true);
                    if (!line.contains("poi.school"))
                        school.setChecked(true);
                    if (!line.contains("poi.sports_complex"))
                        sports.setChecked(true);
                    if (!line.contains("transit"))
                        transit.setChecked(true);
                }
            } catch (IOException e) {

            }
        } catch (FileNotFoundException e) {

        }
        Button toggleButton = getView().findViewById(R.id.buttonToggle);
        Button cancelButton = getView().findViewById(R.id.buttonCancel);

        /*When send button is pressed, the user's preferences are saved in a hashmap and sent to be
          made into JSON format
         */
        toggleButton.setOnClickListener(view -> {
            Map<String, Boolean> toggleRequests = new HashMap<>();
            toggleRequests.put("poi.attraction", attractions.isChecked());
            toggleRequests.put("poi.government", government.isChecked());
            toggleRequests.put("poi.medical", medical.isChecked());
            toggleRequests.put("poi.park", park.isChecked());
            toggleRequests.put("poi.place_of_worship", worship.isChecked());
            toggleRequests.put("poi.school", school.isChecked());
            toggleRequests.put("poi.sports_complex", sports.isChecked());
            toggleRequests.put("transit", transit.isChecked());
            createJSON(toggleRequests);

        });

        cancelButton.setOnClickListener(view -> {
            /*getParentFragment().getChildFragmentManager().beginTransaction().
                    remove(toggleFragment.this).commit();*/
        });
    }

    public void createJSON(Map<String, Boolean> toggleRequests) {

        JSONArray stylers = new JSONArray();
        JSONObject style = new JSONObject();
        JSONArray outArray = new JSONArray();
        try {
            style.put("visibility", "off");
        } catch (JSONException e) {

        }

        stylers.put(style);
        /*
            If a box is not checked, the user wishes to remove the corresponding component
            Add each component to be removed to a JSON array
         */
        for (Map.Entry<String, Boolean> entry : toggleRequests.entrySet()) {
            if (!entry.getValue()) {
                try {
                    JSONObject POI = new JSONObject();
                    POI.put("featureType", entry.getKey());
                    POI.put("stylers", stylers);
                    outArray.put(POI);
                } catch (JSONException e) {

                }
            }
        }
        try {
            //Write JSON array as a string in a new file on device storage
            File toggleMap = new File(getActivity().getApplicationContext().getFilesDir(), "toggleMap");
            FileWriter writer = new FileWriter(toggleMap);
            writer.append(outArray.toString());
            writer.flush();
            writer.close();
        } catch (IOException e) {

        }
    }
}
