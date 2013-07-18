package name.matthewwilkes.auntiecricket;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    	Intent RegularUpdate = new Intent(this, DownloadCricketService.class);
    	PendingIntent pending = PendingIntent.getService(getApplicationContext(), 0, RegularUpdate, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        System.err.println("Updating..");
        if (sharedPreferences.getBoolean("pref_sync", true)) {
            System.err.println("Scheduling update");
        	alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime()-500, (1000*60), pending);
        } else {
            System.err.println("cancelling update");
        	alarmManager.cancel(pending);        
        }
    }
}