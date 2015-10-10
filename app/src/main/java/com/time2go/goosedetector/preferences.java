package com.time2go.goosedetector;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.View;

public class Preferences extends PreferenceFragment
{
    View mOpenCVview;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.fragment_preference);
    }
}