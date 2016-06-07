package xeed.xposed.cbppmod;

import java.util.Iterator;
import java.util.ListIterator;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.*;
import android.content.DialogInterface.*;
import android.content.pm.PackageInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.SparseArray;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import xeed.library.ui.BaseSettings;
import xeed.library.ui.SimpleDialog;
import xeed.xposed.cbppmod.ui.*;

public final class PBMain extends AppCompatActivity
{
	public static final int REQ_SHCUT = 1028, REQ_INAPP = 1666;
	public static final SparseArray<String> keyDb = new SparseArray<String>();
    public static final StyleSpan bold = new StyleSpan(android.graphics.Typeface.BOLD);
	
	public static Resources r = null;
	
	public SharedPreferences sp = null;
	private String[] pl_v, not_v;
	private PagerAdapter spa = null;
	private ViewPager vp = null;
	private InAppMgmt pro = null;
	private int th = -1;
	
	@SuppressLint("WorldReadableFiles")
	@SuppressWarnings("deprecation")
	@Override
	protected final void onCreate(final Bundle b)
	{
		super.onCreate(b);
		r = getResources();
		pl_v = r.getStringArray(R.array.pl_items);
		not_v = r.getStringArray(R.array.not_items);
		sp = getSharedPreferences("pbmcsettings", MODE_WORLD_READABLE);
		BaseSettings.reloadThemes(sp);
		setTheme(th = BaseSettings.getActTh());
		setContentView(R.layout.main);
		vp = (ViewPager)findViewById(R.id.pager);
		vp.setOffscreenPageLimit(4);
		spa = new PagerAdapter(vp, getSupportFragmentManager());
		vp.setAdapter(spa);
	}
	
	@Override
	public final void onRequestPermissionsResult(final int req, final String[] perms, final int[] ress)
	{
	    // TODO?
	}
	
	private final InAppMgmt getInApp()
	{
		if (pro == null) 
		{
			pro = new InAppMgmt();
			pro.bindService(this);
		}
		return pro;
	}
	
	public final PagerAdapter getPager() { return spa; }
	
	public static final void populateKeys(final ViewGroup view, final LayoutInflater li, final Chain ch, final OnClickListener l)
	{
	    view.removeAllViews();
        if (ch.isEz())
        {
            final Key k = ch.ks.getLast();
            final String[] ezt = PBMain.r.getStringArray(R.array.ez_types);
            final Button bt = (Button)li.inflate(R.layout.chainkey, view, false);
            bt.setId(R.id.ch_ez);
            final String title = ezt[ch.getEz()] + " (" + PBMain.key(k.code) + ")";
            final SpannableString ss = new SpannableString(title + "\n" + r.getString(R.string.ez_delay) + " " + (k.dl / 1000F) + "s");
            ss.setSpan(PBMain.bold, 0, title.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
            bt.setText(ss);
            bt.setLines(2);
            if (l != null) bt.setOnClickListener(l);
            view.addView(bt);
        }
        else
        {
            final Iterator<Key> it = ch.ks.iterator();
            while (it.hasNext())
            {
                final Key k = it.next();
                final Button bt = (Button)li.inflate(R.layout.chainkey, view, false);
                final String key = PBMain.key(k.code);
                final SpannableString ss = new SpannableString(key + "\n" + r.getString(k.dn ? R.string.key_pressed : R.string.key_released) + "\n" + (k.dl < 1 ? "" : r.getString(R.string.diag_dl) + ": " + (k.dl/1000F) + "s"));
                ss.setSpan(PBMain.bold, 0, key.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
                bt.setText(ss);
                bt.setTag(k);
                if (l != null) bt.setOnClickListener(l);
                view.addView(bt);
                if (l != null || it.hasNext()) view.addView(li.inflate(R.layout.arrow, view, false));
            }
            if (l != null)
            {
                final Button bt = (Button)li.inflate(R.layout.chainkey, view, false);
                bt.setId(R.id.add_key);
                bt.getBackground().setAlpha(127);
                bt.setText("\n" + r.getString(R.string.diag_add_key) + "\n");
                bt.setOnClickListener(l);
                view.addView(bt);
            }
        }
	}
	
	@Override
	public final void onActivityResult(final int req, final int res, final Intent i)
	{
		if (req == REQ_SHCUT)
		{
			if (res == RESULT_OK)
			{
				final ChainEditFragment cef = spa.getEdit();
				final String name = i.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
				final Intent main = i.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
				final Action a = spa.edit.act;
				a.type = Action.ACTION_OTHER;
				a.ex_i = 1;
				a.ex_s = name + Action.OTHER_SPLT + main.toUri(0);
				cef.actionChanged();
			}
		}
		else if (req == REQ_INAPP)
		{
			final String item = getInApp().buyResult(this, res, i);
			if (item != null) sp.edit().putBoolean("pro." + item, true).commit();
		}
		else super.onActivityResult(req, res, i);
	}
	
	@Override
	protected final void onDestroy()
	{
		if (pro != null)
		{
			pro.unbindService(this);
			pro = null;
		}
		super.onDestroy();
	}
	
	@Override
	protected final void onPause()
	{
		requestIntercept(false);
		super.onPause();
	}
	
	@Override
	protected final void onResume()
	{
	    super.onResume();
	    if (th != BaseSettings.getActTh()) finish();
	}

	@Override
	public final void onStart()
	{
		super.onStart();
		final PackageInfo pi = getVerInfo();
		if (pi.versionCode != getActiveVerCode())
		{
		    final CharSequence msg = getString(getActiveVerCode() == 0 ? R.string.diag_reboot : R.string.diag_update, pi.versionCode, getActiveVerCode());
		    SimpleDialog.create(this, BaseSettings.getDiagTh(), R.string.diag_ok, R.string.app_name, msg, getActiveVerCode() == 0 ? new OnDismissListener()
            {
                @Override
                public final void onDismiss(final DialogInterface di)
                {
                    finish();
                }
            } : null).show();
		}
	}

	@Override
	public final boolean onCreateOptionsMenu(final Menu m)
	{
		getMenuInflater().inflate(R.menu.main, m);
		return super.onCreateOptionsMenu(m);
	}

	@Override
	public final boolean onOptionsItemSelected(final MenuItem mi)
	{
		if (mi.getItemId() == R.id.setts)
		{
			startActivity(new Intent(this, PBSettings.class));
			return true;
		}
		else if (mi.getItemId() == R.id.basch || mi.getItemId() == R.id.advch)
		{
			final ListIterator<Chain> li = spa.getIterator();
			while (li.hasNext())
				if (li.next().nm.equals(""))
				{
					li.remove();
					break;
				}
			final Chain ch = new Chain(sp, "");
			if (mi.getItemId() == R.id.basch) ch.setEz(0, KeyEvent.KEYCODE_UNKNOWN, 0);
			spa.addChain(ch);
			spa.setEdit(ch);
			return true;
		}
		else if (mi.getItemId() == R.id.save)
		{
			if (spa.edit != null) spa.getEdit().onFocusChange(spa.getEdit().getView().findViewById(R.id.ch_nm), false);
	        final SharedPreferences.Editor e = sp.edit();
	        int n = 0;
	        final ListIterator<Chain> li = spa.getIterator();
	        while (li.hasNext())
	        {
	            final Chain ch = li.next();
	            e.putString("chainlist." + n, ch.nm);
                if (ch.nm.length() < 1) Toast.makeText(this, R.string.diag_nm_err, Toast.LENGTH_LONG).show();
                else if (ch.ks.size() < 1) Toast.makeText(this, R.string.diag_ks_err, Toast.LENGTH_LONG).show();
                else ch.save(e);
	            ++n;
	        }
	        e.putInt("chainlist.count", n);
	        e.commit();
	        spa.chainsSaved();
			if (vp.getCurrentItem() != 0) Toast.makeText(this, R.string.diag_sav, Toast.LENGTH_SHORT).show();
			return true;
		}
		return super.onOptionsItemSelected(mi);
	}
	
	public static final boolean isKeySafe(final int key)
	{
	    return key != KeyEvent.KEYCODE_POWER && key != KeyEvent.KEYCODE_HOME;
	}
	
	@Override
	public final void onBackPressed()
	{
		if (vp.getCurrentItem() != 0) vp.setCurrentItem(0);
		else super.onBackPressed();
	}

	public static final String key(final int code, final int scan)
	{
		final String s = keyDb.get(code);
		if (s == null) return keyDb.get(0) + (scan == 0 ? "" : " (SC " + Integer.toHexString(scan) + ")");
		else return s;
	}
	
	public static final String key(final int code)
	{
		final String s = keyDb.get(code);
		if (s == null) return keyDb.get(0);
		else return s;
	}

	private static final String format(final String s)
	{
		final char[] c = s.toLowerCase(Locale.getDefault()).toCharArray();
		boolean f = true;
		for (int i = 0; i < c.length; ++i)
		{
			if (Character.isWhitespace(c[i])) f = true;
			else if (f)
			{
				c[i] = Character.toUpperCase(c[i]);
				f = false;
			}
		}
		return new String(c);
	}

    public static final String action(final Action a)
    {
    	String ret = null;
    	String[] arr = r.getStringArray(R.array.action_k);
    	if (a.type == Action.ACTION_CODED)
    	{
    		arr = r.getStringArray(R.array.action_coded_k);
    		ret = arr[a.ex_i];
    	}
    	else if (a.type == Action.ACTION_KEY)
    	{
    		ret = (a.isKeyLong() ? r.getString(R.string.diag_prs_lng) : arr[2]) + " (" + key(a.ex_i) + ')';
    	}
    	else if (a.type == Action.ACTION_MEDIA)
    	{
    		arr = r.getStringArray(R.array.action_media_k);
    		for (int i = 0; i < Action.MEDIA.length; ++i)
    			if (a.ex_i == Action.MEDIA[i])
    			{
    				ret = arr[i];
    				break;
    			}
    	}
    	else if (a.type == Action.ACTION_OTHER)
    	{
			ret = arr[4] + " (" + a.getOtherName() + ')';
    	}
        return ret == null ? arr[0] : ret;
    }
    
    public final String btnTxt(final int arrId, final int flags)
    {
    	String ret = "";
    	if ((flags & 7) == 7) ret = "+++" + getString(R.string.diag_alw);
    	else
    	{
    	    final String[] arr = r.getStringArray(arrId);
    		if ((flags & 1) == 1) ret += " + " + arr[0];
    		if ((flags & 2) == 2) ret += " + " + arr[1];
    		if ((flags & 4) == 4) ret += " + " + arr[2];
    	}
    	if (ret.equals("")) ret = "---" + getString(R.string.diag_nev);
    	return ret.substring(3);
    }
    
    public final String music(final int pl)
    {
        String ret = "";
        if ((pl & 3) == 3) ret = "+++" + getString(R.string.diag_alw);
        else
        {
            if ((pl & 1) == 1) ret += " + " + pl_v[0];
            if ((pl & 2) == 2) ret += " + " + pl_v[1];
        }
        if (ret.equals("")) ret = "---" + getString(R.string.diag_nev);
        return ret.substring(3);
    }
    
    public final String notify(final int not)
    {
        String ret = "";
        if ((not & 7) == 7) ret = "+++" + getString(R.string.diag_all);
        else
        {
            if ((not & 1) == 1) ret += " + " + not_v[0];
            if ((not & 2) == 2) ret += " + " + not_v[1];
            if ((not & 4) == 4) ret += " + " + not_v[2];
        }
        if (ret.equals("")) ret = "---" + getString(R.string.diag_no);
        return ret.substring(3);
    }
    
	private final PackageInfo getVerInfo()
	{
		try { return getPackageManager().getPackageInfo(getPackageName(), 0); }
		catch (final Exception ex) { return null; }
	}

	@Deprecated
	public static final String getActiveVerName() { return ""; }

	public static final int getActiveVerCode() { return 0; }

	public final void requestIntercept(final boolean state)
	{
        final Intent i = new Intent("xeed.xposed.cbppmod.Send");
        i.putExtra("xeed.xposed.cbppmod.Send", state);
        sendBroadcast(i);
	}
	
	static
	{
		for (final java.lang.reflect.Field f : KeyEvent.class.getDeclaredFields())
			try
			{
				if (f.getName().startsWith("KEYCODE_"))
				{
					f.setAccessible(true);
					keyDb.put(f.getInt(null), format(f.getName().substring(8).replace('_', ' ')));
				}
			}
			catch (final Exception ex) { }
	}
}
