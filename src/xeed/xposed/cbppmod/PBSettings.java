package xeed.xposed.cbppmod;

import java.util.ArrayList;
import java.util.List;

import android.content.*;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.preference.DialogPreference;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import xeed.library.ui.BaseSettings;
import xeed.xposed.cbppmod.prf.FlagListPreference;

public final class PBSettings extends BaseSettings
{
    static final List<ResolveInfo> getMediaReceivers(final PackageManager pm)
    {
        final Intent i = new Intent(Intent.ACTION_MEDIA_BUTTON);
        return pm.queryBroadcastReceivers(i, PackageManager.GET_INTENT_FILTERS | PackageManager.GET_RESOLVED_FILTER);
    }

    @Override
	protected final void onCreatePreferences(final PreferenceManager mgr)
	{
	    addPreferencesToCategory(R.xml.settings, Category.general);
        final SharedPreferences sp = mgr.getSharedPreferences();
        final FlagListPreference flp = (FlagListPreference)mgr.findPreference("usePlugIn");
        final CharSequence[] old = flp.getEntries();
        final CharSequence[] cs = new CharSequence[old.length - 1];
        System.arraycopy(old, 1, cs, 0, cs.length);
        flp.setEntries(cs);
        final ListPreference lp = (ListPreference)mgr.findPreference("setPlayer");
        final List<ResolveInfo> ris = getMediaReceivers(getPackageManager());
        final ArrayList<CharSequence> i = new ArrayList<CharSequence>(ris.size() + 1), v = new ArrayList<CharSequence>(ris.size() + 1);
        i.add(getString(R.string.pref_setplayer_v1)); v.add("system");
        i.add(getString(R.string.pref_setplayer_v2)); v.add("recent");
        for (final ResolveInfo ri : ris)
        {
            final String val = ri.activityInfo.packageName + '\t' + ri.activityInfo.name;
            final CharSequence lab = ri.activityInfo.applicationInfo.loadLabel(getPackageManager());
            i.add(lab + " (" + ri.activityInfo.name + ")");
            v.add(val);
        }
        lp.setEntries(i.toArray(new CharSequence[i.size()]));
        lp.setEntryValues(v.toArray(new CharSequence[v.size()]));
        mgr.findPreference("setPlrMode").setEnabled(sp.getString("setPlayer", "system").indexOf('\t') > 0);
        if (sp.getInt("usePlugOnly", 0) == 0) sp.edit().putInt("usePlugOnly", 7).apply();
        final DialogPreference idp = (DialogPreference)mgr.findPreference("authors");
        idp.setDialogMessage(idp.getDialogMessage() + "\n DragSortListView:\n  bauerca & kelsos\n FlowLayout:\n  ApmeM");
        mgr.findPreference("soundURI").setOnPreferenceChangeListener(this);
	}
	
	@Override
	protected final void onPreferenceChanged(final PreferenceManager mgr, final SharedPreferences prefs, final String key)
	{
	    if (key.equals("setPlayer")) mgr.findPreference("setPlrMode").setEnabled(prefs.getString(key, "system").indexOf('\t') > 0);
        if (key.equals("usePlugOnly") && prefs.getInt("usePlugOnly", 0) == 0) prefs.edit().putInt("usePlugOnly", 7).apply();
	}
	
    @Override
    protected final String getPrefsName() { return "pbmcsettings"; }
}
