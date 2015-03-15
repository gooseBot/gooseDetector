package com.time2go.goosedetector;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import org.opencv.core.Scalar;

public class Preferences extends PreferenceFragment
{
    View mOpenCVview;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.fragment_preference);
        //Preference pref = (Preference)this.findPreference("ROIright");
        //landmarkEditNameView = (EditText) textEntryView.findViewById(R.id.landmark_name_dialog_edit);
        //pref.setSummary(mOpenCVview.getWidth());
    }
}