package com.example.onyx.onyx.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.example.onyx.onyx.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
    Fragment that takes user input in the form of a set of checkboxes to determine what icons a user
    would like drawn to their screen and what to remove
 */

public class toggleFragment extends Fragment {

    public static final String ARG_TYPE = "type";
    public static final String TYPE_ALL = "type_all";
    private LocalBroadcastManager broadcaster;

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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_toggle, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        broadcaster = LocalBroadcastManager.getInstance(Objects.requireNonNull(this.getContext()));
        final CheckBox attractions = Objects.requireNonNull(getView()).findViewById(R.id.checkBoxAttractions);
        final CheckBox business = Objects.requireNonNull(getView()).findViewById(R.id.checkBoxBusiness);
        final CheckBox government = getView().findViewById(R.id.checkBoxGovernment);
        final CheckBox medical = getView().findViewById(R.id.checkBoxMedical);
        final CheckBox park = getView().findViewById(R.id.checkBoxParks);
        final CheckBox worship = getView().findViewById(R.id.checkBoxWorship);
        final CheckBox school = getView().findViewById(R.id.checkBoxSchools);
        final CheckBox sports = getView().findViewById(R.id.checkBoxSport);
        final CheckBox transit = getView().findViewById(R.id.checkBoxTransit);
        /*
            See if file already exists
            If it does, organise it to reflect the user's current settings
          */
        try {
            FileInputStream stream = Objects.requireNonNull(getActivity()).getApplicationContext().openFileInput("toggleMap");
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
                    if(!line.contains(("poi.business")))
                        business.setChecked(true);
                }
            } catch (IOException ignored) {

            }
        } catch (FileNotFoundException ignored) {

        }
        Button toggleButton = getView().findViewById(R.id.buttonToggle);

        /*
            When send button is pressed, the user's preferences are saved in a hashmap and sent to
            be made into JSON format
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
            toggleRequests.put("poi.business", business.isChecked());
            createJSON(toggleRequests);

            //Send data to maps fragment and notify user of changes
            Intent intent = new Intent("style");
            broadcaster.sendBroadcast(intent);
            Toast.makeText(getActivity(),"Map update sent!",Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Create a JSON file to be parsed into the map styling
     * @param toggleRequests
     */
    public void createJSON(Map<String, Boolean> toggleRequests) {
        //Section of the JSON array that holds stylers
        JSONArray stylers = new JSONArray();
        //We're only interested in the styler that controls visibility
        JSONObject style = new JSONObject();
        //Combined with data in this JSON array
        JSONArray outArray = new JSONArray();

        try {
            style.put("visibility", "off");
        } catch (JSONException ignored) {

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
                } catch (JSONException ignored) {

                }
            }
        }
        try {
            // Write JSON array as a string in a new file on device storage
            File toggleMap = new File(Objects.requireNonNull(getActivity()).getApplicationContext()
                    .getFilesDir(), "toggleMap");
            FileWriter writer = new FileWriter(toggleMap);
            writer.append(outArray.toString());
            writer.flush();
            writer.close();
        } catch (IOException ignored) {

        }
    }
}
