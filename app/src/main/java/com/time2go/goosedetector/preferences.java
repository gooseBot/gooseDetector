package com.time2go.goosedetector;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class preferences extends PreferenceFragment
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.fragment_preference);
    }
}