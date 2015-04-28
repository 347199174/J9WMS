
package com.google.zxing.client.android;
import com.example.admin.j9wms.R;

import android.app.AlertDialog;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

public final class PreferencesFragment 
    extends PreferenceFragment  {

  private CheckBoxPreference[] checkBoxPrefs;
  
  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    addPreferencesFromResource(R.xml.preferences);
    
    PreferenceScreen preferences = getPreferenceScreen();

    EditTextPreference customProductSearch = (EditTextPreference)
        preferences.findPreference(PreferencesActivity.KEY_CUSTOM_PRODUCT_SEARCH);
  }



}
