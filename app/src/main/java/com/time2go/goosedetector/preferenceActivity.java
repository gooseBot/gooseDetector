package com.time2go.goosedetector;

import android.preference.PreferenceActivity;

import java.util.List;

public class preferenceActivity extends PreferenceActivity
{
    @Override
    public void onBuildHeaders(List<Header> target)
    {
        loadHeadersFromResource(R.xml.headers_preference, target);
    }

    @Override
    protected boolean isValidFragment(String fragmentName)
    {
        return preferences.class.getName().equals(fragmentName);
    }
}
