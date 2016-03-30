package xeed.xposed.cbppmod;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.*;
import android.content.SharedPreferences.*;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.preference.ListPreference;
import android.support.v4.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import xeed.xposed.cbppmod.prf.FlagListPreference;

@SuppressLint("NewApi")
public final class PBSettings extends AppCompatActivity
{
	private static PBSettings ma = null;
	
	public final void onCreate(final Bundle b)
	{
		super.onCreate(b);
		ma = this;
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		if (b == null) getSupportFragmentManager().beginTransaction().add(android.R.id.content, new MainFragment()).commit();
	}
	
    static final List<ResolveInfo> getMediaReceivers(final PackageManager pm)
    {
        final Intent i = new Intent(Intent.ACTION_MEDIA_BUTTON);
        return pm.queryBroadcastReceivers(i, PackageManager.GET_INTENT_FILTERS | PackageManager.GET_RESOLVED_FILTER);
    }
	
	public static final class MainFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener
	{
		private boolean changes;
	    
		@SuppressLint("WorldReadableFiles")
		@SuppressWarnings("deprecation")
		@Override
		public final void onCreate(final Bundle b)
	    {
	        super.onCreate(b);
	        changes = false;
	        getPreferenceManager().setSharedPreferencesName("pbmcsettings");
	        getPreferenceManager().setSharedPreferencesMode(MODE_WORLD_READABLE);
	        addPreferencesFromResource(R.xml.settings);
	        final SharedPreferences sp = ma.getSharedPreferences("pbmcsettings", MODE_WORLD_READABLE);
	        final FlagListPreference flp = (FlagListPreference)findPreference("usePlugIn");
	        final CharSequence[] old = flp.getEntries();
	        final CharSequence[] cs = new CharSequence[old.length - 1];
	        System.arraycopy(old, 1, cs, 0, cs.length);
	        flp.setEntries(cs);
	        findPreference("hideApp").setSummary(getResources().getString(R.string.pref_hideapp_s, getResources().getString(R.string.title_pbmain)));
	        final ListPreference lp = (ListPreference)findPreference("setPlayer");
	        final List<ResolveInfo> ris = getMediaReceivers(ma.getPackageManager());
	        final ArrayList<CharSequence> i = new ArrayList<CharSequence>(ris.size() + 1), v = new ArrayList<CharSequence>(ris.size() + 1);
	        i.add(getResources().getString(R.string.pref_setplayer_v1)); v.add("system");
	        i.add(getResources().getString(R.string.pref_setplayer_v2)); v.add("recent");
	        for (final ResolveInfo ri : ris)
	        {
	            final String val = ri.activityInfo.packageName + '\t' + ri.activityInfo.name;
	        	final CharSequence lab = ri.activityInfo.applicationInfo.loadLabel(ma.getPackageManager());
	            i.add(lab + " (" + ri.activityInfo.name + ")");
	            v.add(val);
	        }
	        lp.setEntries(i.toArray(new CharSequence[i.size()]));
	        lp.setEntryValues(v.toArray(new CharSequence[v.size()]));
	        findPreference("setPlrMode").setEnabled(sp.getString("setPlayer", "system").indexOf('\t') > 0);
	        if (sp.getInt("usePlugOnly", 0) == 0) sp.edit().putInt("usePlugOnly", 7).apply();
	        final DialogPreference idp = (DialogPreference)findPreference("authors");
	        idp.setDialogMessage(idp.getDialogMessage() + "\n DragSortListView:\n bauerca\n FlowLayout:\n ApmeM");
	    }
	    
	    @Override
	    public final void onResume()
	    {
	        super.onResume();
	        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	    }
	    
	    @Override
	    public final void onPause()
	    {
	        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	        if (changes)
	        {
	        	ma.sendBroadcast(new Intent("xeed.xposed.cbppmod.Update"));
	        	changes = false;
	        }
	        super.onPause();
	    }
	    
		@Override
	    public final void onSharedPreferenceChanged(final SharedPreferences sp, final String key)
	    {
	        if (key.equals("setPlayer")) findPreference("setPlrMode").setEnabled(sp.getString(key, "system").indexOf('\t') > 0);
	        if (key.equals("usePlugOnly") && sp.getInt("usePlugOnly", 0) == 0) sp.edit().putInt("usePlugOnly", 7).apply();
	        if (key.equals("hideApp"))
	        {
	        	final ComponentName cn = new ComponentName(ma, "xeed.xposed.cbppmod.Launcher");
	        	ma.getPackageManager().setComponentEnabledSetting(cn, sp.getBoolean("hideApp", false) ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED : PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
	        }
	        else changes = true;
	    }
	}
}
