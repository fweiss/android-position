package com.uttama.android.samples;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Boilerplate for creating preferences dialog from resource.
 * @author frankw
 *
 */
public class GlobalPositionPreferences
extends PreferenceActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
}
