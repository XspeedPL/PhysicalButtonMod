package xeed.xposed.cbppmod;

import java.util.ArrayList;
import java.util.Locale;

import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.DragSortListView.DragSortListener;
import android.annotation.SuppressLint;
import android.content.*;
import android.content.DialogInterface.*;
import android.content.pm.PackageInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.*;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.SparseArray;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar.OnSeekBarChangeListener;

public final class PBMain extends AppCompatActivity
{
	public static final SparseArray<String> keyDb = new SparseArray<String>();
	public static final StyleSpan bold = new StyleSpan(android.graphics.Typeface.BOLD);
	
	private static PBMain ma = null;
	private static SharedPreferences sp = null;
	private static BroadcastReceiver br = null;
	private static int lk = -1;
	private static AlertDialog d = null;
	private static Resources r = null;
	private static String[] au_v, md_v;
	
	private SettingsPagerAdapter spa = null;
	private Chain edit = null;
	
	@SuppressLint("WorldReadableFiles")
	@SuppressWarnings("deprecation")
	@Override
	protected final void onCreate(final Bundle b)
	{
		super.onCreate(b);
		r = getResources();
		au_v = r.getStringArray(R.array.au_items);
		md_v = r.getStringArray(R.array.md_items);
		ma = this;
		sp = getSharedPreferences("pbmcsettings", MODE_WORLD_READABLE);
		setContentView(R.layout.main);
		final ViewPager vp = (ViewPager)findViewById(R.id.pager);
		vp.setAdapter(spa = new SettingsPagerAdapter(getSupportFragmentManager()));
		br = new BroadcastReceiver()
		{
			@Override
			public final void onReceive(final Context c, final Intent i)
			{
				lk = i.getIntExtra("xeed.xposed.cbppmod.Key", -1);
				if (lk != -1 && d != null)
					((EditText)d.findViewById(R.id.key)).setText(key(lk));
			}
		};
	}

	@Override
	public final void onStart()
	{
		super.onStart();
		registerReceiver(br, new IntentFilter("xeed.xposed.cbppmod.Key"), null, null);
		final PackageInfo pi = getVerInfo();
		if (pi.versionCode != getActiveVerCode())
		{
			final AlertDialog.Builder b = new AlertDialog.Builder(ma);
			if (getActiveVerCode() == 0) b.setMessage(R.string.diag_reboot);
			else b.setMessage(r.getString(R.string.diag_update, pi.versionName + " (" + pi.versionCode + ')', getActiveVerName() + " (" + getActiveVerCode() + ')'));
			b.setPositiveButton(R.string.diag_ok, new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(final DialogInterface di, final int which)
				{
					di.dismiss();
				}
			});
			final AlertDialog ad = b.create();
			if (getActiveVerCode() == 0)
				ad.setOnDismissListener(new OnDismissListener()
				{
					@Override
					public final void onDismiss(final DialogInterface di)
					{
						finish();
					}
				});
			ad.show();
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
			startActivity(new Intent(ma, PBSettings.class));
			return true;
		}
		else if (mi.getItemId() == R.id.newch)
		{
			spa.clf.chs.add(new Chain(sp, ""));
			spa.clf.changed(false);
			spa.clf.onItemClick(null, null, spa.clf.chs.size() - 1, 0);
			return true;
		}
		else if (mi.getItemId() == R.id.save)
		{
			final ViewPager vp = (ViewPager)findViewById(R.id.pager);
			if (spa.getCount() > 1) spa.cef.onFocusChange(spa.cef.getView().findViewById(R.id.ch_nm), false);
			spa.clf.save();
			if (vp.getCurrentItem() == 1) Toast.makeText(this, R.string.diag_sav, Toast.LENGTH_SHORT).show();
			return true;
		}
		return super.onOptionsItemSelected(mi);
	}

	@Override
	public final void onStop()
	{
		unregisterReceiver(br);
		super.onPause();
	}
	
	@Override
	public final void onBackPressed()
	{
		final ViewPager vp = (ViewPager)findViewById(R.id.pager);
		if (vp.getCurrentItem() == 1) vp.setCurrentItem(0);
		else super.onBackPressed();
	}

	static final String key(final int code)
	{
		if (code == -1)
			return r.getString(R.string.diag_no_key);
		return keyDb.get(code, String.valueOf(code));
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

    private static final String action(final Action a)
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
    		ret = arr[2] + " (" + key(a.ex_i) + ')';
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
			ret = arr[4] + " (" + a.ex_s.substring(0, a.ex_s.indexOf(" <-> ")) + ')';
    	}
        return ret == null ? arr[0] : ret;
    }
    
    private static final String mode(final int md)
    {
    	String ret = "";
    	if ((md & 7) == 7) ret = "+++" + r.getString(R.string.diag_alw);
    	else
    	{
    		if ((md & 1) == 1) ret += " + " + md_v[0];
    		if ((md & 2) == 2) ret += " + " + md_v[1];
    		if ((md & 4) == 4) ret += " + " + md_v[2];
    	}
    	if (ret.equals("")) ret = "---" + r.getString(R.string.diag_nev);
    	return ret.substring(3);
    }
	
    private static final String audio(final int au)
    {
    	String ret = "";
    	if ((au & 7) == 7) ret = "+++" + r.getString(R.string.diag_alw);
    	else
    	{
    		if ((au & 1) == 1) ret += " + " + au_v[0];
    		if ((au & 2) == 2) ret += " + " + au_v[1];
    		if ((au & 4) == 4) ret += " + " + au_v[2];
    	}
    	if (ret.equals("")) ret = "---" + r.getString(R.string.diag_nev);
    	return ret.substring(3);
    }
    
	private static final PackageInfo getVerInfo()
	{
		try { return ma.getPackageManager().getPackageInfo(ma.getPackageName(), 0); }
		catch (final Exception ex) { return null; }
	}

	public static final String getActiveVerName() { return "pre-4.0"; }

	public static final int getActiveVerCode() { return 0; }

	public final class SettingsPagerAdapter extends FragmentPagerAdapter
	{
		final ChainListFragment clf;
		final ChainEditFragment cef;
		
		public SettingsPagerAdapter(final FragmentManager fm)
		{
			super(fm);
			clf = new ChainListFragment();
			cef = new ChainEditFragment();
		}

		@Override
		public final Fragment getItem(final int i)
		{
			return i == 0 ? clf : cef;
		}

		@Override
		public final int getCount()
		{
			return edit == null ? 1 : 2;
		}
		
		@Override
		public final CharSequence getPageTitle(final int i)
		{
			return i == 0 ? r.getString(R.string.tab_chns) : r.getString(R.string.tab_edit) + " " + edit.nm;
		}
	}

	public final class ChainListFragment extends Fragment implements DragSortListener, OnItemClickListener
	{
		private final ArrayList<Chain> chs = new ArrayList<Chain>();
		private final ChainAdapter ca = new ChainAdapter();
		
		@Override
		public final void onCreate(final Bundle b)
		{
			super.onCreate(b);
			final int n = sp.getInt("chainlist.count", 0);
	        for (int i = 0; i < n; ++i)
	        {
	        	final String name = sp.getString("chainlist." + i, "");
	        	if (name.length() > 0) chs.add(new Chain(sp, name));
	        }
	        if (chs.size() == 0 && getActiveVerCode() != 0)
	        {
	        	final AlertDialog.Builder bu = new AlertDialog.Builder(ma);
				bu.setMessage("It seems you don't have any key chains set yet. Would you like to add some basic ones?");
				bu.setNegativeButton(R.string.diag_no, null);
				bu.setPositiveButton(R.string.diag_yes, new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(final DialogInterface di, final int which)
					{
			        	Chain ch = new Chain(sp, "Original camera");
			        	ch.act = new Action(Action.ACTION_KEY, KeyEvent.KEYCODE_CAMERA);
			        	ch.vib = 0;
			        	ch.ks.clear();
			        	ch.ks.add(new Key(KeyEvent.KEYCODE_CAMERA, true, 3000));
			        	chs.add(ch);
			        	
			        	ch = new Chain(sp, "Play pause");
			        	ch.act = new Action(Action.ACTION_MEDIA, Action.MEDIA_PLAY);
			        	ch.ks.clear();
			        	ch.ks.add(new Key(KeyEvent.KEYCODE_CAMERA, true, -1));
			        	ch.ks.add(new Key(KeyEvent.KEYCODE_CAMERA, false, -1));
			        	chs.add(ch);
			        	
			        	ch = new Chain(sp, "Next track");
			        	ch.act = new Action(Action.ACTION_MEDIA, Action.MEDIA_NEXT);
			        	ch.ks.clear();
			        	ch.ks.add(new Key(KeyEvent.KEYCODE_VOLUME_UP, true, 500));
			        	chs.add(ch);
			        	
			        	ch = new Chain(sp, "Original vol-up");
			        	ch.act = new Action(Action.ACTION_KEY, KeyEvent.KEYCODE_VOLUME_UP);
			        	ch.vib = 0;
			        	ch.ks.clear();
			        	ch.ks.add(new Key(KeyEvent.KEYCODE_VOLUME_UP, true, -1));
			        	ch.ks.add(new Key(KeyEvent.KEYCODE_VOLUME_UP, false, -1));
			        	chs.add(ch);
			        	
			        	ch = new Chain(sp, "Previous track");
			        	ch.act = new Action(Action.ACTION_MEDIA, Action.MEDIA_PREV);
			        	ch.ks.clear();
			        	ch.ks.add(new Key(KeyEvent.KEYCODE_VOLUME_DOWN, true, 500));
			        	chs.add(ch);
			        	
			        	ch = new Chain(sp, "Original vol-dn");
			        	ch.act = new Action(Action.ACTION_KEY, KeyEvent.KEYCODE_VOLUME_DOWN);
			        	ch.vib = 0;
			        	ch.ks.clear();
			        	ch.ks.add(new Key(KeyEvent.KEYCODE_VOLUME_DOWN, true, -1));
			        	ch.ks.add(new Key(KeyEvent.KEYCODE_VOLUME_DOWN, false, -1));
			        	chs.add(ch);
			        	
			        	spa.clf.changed(false);
					}
				});
				bu.create().show();
	        }
		}

		public final View onCreateView(final LayoutInflater li, final ViewGroup vg, final Bundle b)
		{
			final LinearLayout ll = (LinearLayout)li.inflate(R.layout.chainlist, vg, false);
			((TextView)ll.findViewById(R.id.status)).setText(r.getString(R.string.diag_sts) + ": " + r.getString(R.string.diag_sav));
			final DragSortListView dlv = (DragSortListView)ll.findViewById(R.id.chainlist);
			dlv.setAdapter(ca);
			dlv.setDragSortListener(this);
			dlv.setOnItemClickListener(this);
			return ll;
		}
		
		@Override
		public final void drop(final int from, final int to)
		{
			if (from != to)
			{
				Chain ch = chs.remove(from);
				chs.add(to, ch);
				changed(false);
			}
		}
		
		private final void save()
		{
			final SharedPreferences.Editor e = sp.edit();
			e.putInt("chainlist.count", chs.size());
			for (int i = chs.size() - 1; i >= 0; --i)
			{
				final Chain ch = chs.get(i);
				e.putString("chainlist." + i, ch.nm);
				if (ch.nm.length() > 0) ch.save(e);
			}
			e.commit();
			final Intent i = new Intent("xeed.xposed.cbppmod.Update");
            i.putExtra("xeed.xposed.cbppmod.Chains", true);
            sendBroadcast(i);
			changed(true);
		}
		
		final void changed(final boolean save)
		{
			ca.notifyDataSetChanged();
			final LinearLayout ll = (LinearLayout)getView();
			final TextView tv = (TextView)ll.findViewById(R.id.status);
			tv.setText(r.getString(R.string.diag_sts) + ": " + r.getString(save ? R.string.diag_sav : R.string.diag_nsav));
			tv.setTextColor(save ? 0xff00ff00 : 0xffff0000);
		}
		
		private final class ChainAdapter extends BaseAdapter
		{
			@Override
			public final int getCount() { return chs.size(); }

			@Override
			public final Object getItem(int i) { return chs.get(i); }

			@Override
			public final long getItemId(int i) { return chs.get(i).hashCode(); }

			@Override
			public final View getView(final int pos, final View reuse, final ViewGroup vg)
			{
				final LayoutInflater li = LayoutInflater.from(ma);
				final Chain ch = chs.get(pos);
				final LinearLayout ll;
				if (reuse != null) ll = (LinearLayout)reuse;
				else ll = (LinearLayout)li.inflate(R.layout.chainitem, vg, false);
				final ViewGroup keys = (ViewGroup)ll.findViewById(R.id.keys);
				keys.removeAllViews();
				for (final Key k : ch.ks)
				{
					final Button b = (Button)li.inflate(R.layout.chainkey, keys, false);
					final String key = key(k.code);
					final SpannableString ss = new SpannableString(key + "\n" + r.getString(k.dn ? R.string.key_pressed : R.string.key_released) + "\n" + (k.dl == -1 ? "" : r.getString(R.string.diag_dl) + ": " + (k.dl/1000F) + "s"));
					ss.setSpan(bold, 0, key.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
					b.setText(ss);
					keys.addView(b);
				}
				((TextView)ll.findViewById(R.id.name)).setText(ch.nm);
				((TextView)ll.findViewById(R.id.desc)).setText(r.getString(R.string.diag_md) + ": " + mode(ch.md) + ", " + r.getString(R.string.diag_au) + ": " + audio(ch.au));
				return ll;
			}
		}

		@Override
		public final void onItemClick(final AdapterView<?> av, final View v, final int pos, final long id)
		{
			edit = chs.get(pos);
			spa.notifyDataSetChanged();
			final ViewPager vp = (ViewPager)findViewById(R.id.pager);
			vp.setCurrentItem(1);
			spa.cef.updateEdited();
		}

		@Override
		public void drag(int from, int to) { }

		@Override
		public final void remove(final int pos)
		{
			final AlertDialog.Builder b = new AlertDialog.Builder(getContext());
			b.setTitle(R.string.diag_cnf_rmv);
			b.setNegativeButton(R.string.diag_no, null);
			b.setPositiveButton(R.string.diag_yes, new DialogInterface.OnClickListener()
			{
				@Override
				public final void onClick(final DialogInterface di, final int i)
				{
					if (chs.get(pos) == edit)
					{
						edit = null;
						spa.notifyDataSetChanged();
					}
					chs.remove(pos);
					changed(false);
				}
			});
			final AlertDialog ad = b.create();
			ad.setOnDismissListener(new OnDismissListener()
			{
				@Override
				public final void onDismiss(final DialogInterface di)
				{
					ca.notifyDataSetChanged();
				}
			});
			ad.show();
		}
	}

	public final class ChainEditFragment extends Fragment implements OnFocusChangeListener, OnCheckedChangeListener, OnClickListener, OnSeekBarChangeListener
	{
		private Chain ch = null;

		public final View onCreateView(final LayoutInflater li, final ViewGroup vg, final Bundle b)
		{
			ch = edit;
			final ViewGroup ret = (ViewGroup)li.inflate(R.layout.editor, vg, false);
			addKeys(li, ret);
			final AutoCompleteTextView actv = (AutoCompleteTextView)ret.findViewById(R.id.ch_nm);
			actv.setText(ch.nm);
			actv.setOnFocusChangeListener(this);
			Button bt = (Button)ret.findViewById(R.id.ch_act);
			bt.setText(action(ch.act));
			bt.setOnClickListener(this);
			bt = (Button)ret.findViewById(R.id.ch_md);
			bt.setText(mode(ch.md));
			bt.setOnClickListener(this);
			bt = (Button)ret.findViewById(R.id.ch_au);
			bt.setText(audio(ch.au));
			bt.setOnClickListener(this);
			final CheckBox cb = (CheckBox)ret.findViewById(R.id.ch_ccl);
			cb.setChecked(ch.ccl);
			cb.setOnCheckedChangeListener(this);
			final SeekBar sb = (SeekBar)ret.findViewById(R.id.ch_vib);
			sb.setProgress(ch.vib / 50);
			sb.setOnSeekBarChangeListener(this);
			return ret;
		}
		
		private final void addKeys(final LayoutInflater li, final ViewGroup vg)
		{
			final ViewGroup keys = (ViewGroup)vg.findViewById(R.id.keys);
			keys.removeAllViews();
			for (final Key k : ch.ks)
			{
				final Button bt = (Button)li.inflate(R.layout.chainkey, keys, false);
				final String key = key(k.code);
				final SpannableString ss = new SpannableString(key + "\n" + r.getString(k.dn ? R.string.key_pressed : R.string.key_released) + "\n" + (k.dl == -1 ? "" : r.getString(R.string.diag_dl) + ": " + (k.dl/1000F) + "s"));
				ss.setSpan(bold, 0, key.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
				bt.setText(ss);
				bt.setTag(k);
				bt.setOnClickListener(this);
				keys.addView(bt);
			}
			final Button bt = (Button)li.inflate(R.layout.chainkey, keys, false);
			bt.setId(R.id.add_key);
			bt.getBackground().setAlpha(127);
			bt.setText("\n" + r.getString(R.string.diag_add_key) + "\n");
			bt.setOnClickListener(this);
			keys.addView(bt);
		}
		
		final void actionChanged()
		{
			((Button)getView().findViewById(R.id.ch_act)).setText(action(ch.act));
			spa.clf.changed(false);
		}
		
		@Override
		public final void onActivityResult(final int req, final int res, final Intent i)
		{
			super.onActivityResult(req, res, i);
			if (req == 1028)
			{
				if (res == RESULT_OK)
				{
					final String name = i.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
					final Intent main = i.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
					ch.act.type = Action.ACTION_OTHER;
					ch.act.ex_i = 1;
					ch.act.ex_s = name + " <-> " + main.toUri(0);
					actionChanged();
				}
			}
		}
		
		@Override
		public final void onClick(final View v)
		{
			final boolean md = v.getId() == R.id.ch_md;
			if (v.getId() == R.id.ch_act)
			{
				new ActionDialog(this, edit.act).show();
			}
			else if (md || v.getId() == R.id.ch_au)
			{
				final AlertDialog.Builder b = new AlertDialog.Builder(ma);
				b.setTitle(md ? R.string.diag_sel_md : R.string.diag_sel_au);
				final boolean[] arr = new boolean[3];
				for (int x = md ? ch.md : ch.au, i = 0; i < 3; ++i, x >>= 1)
					arr[i] = (x & 1) == 1;
				b.setPositiveButton(R.string.diag_ok, new DialogInterface.OnClickListener()
				{
					@Override
					public final void onClick(final DialogInterface di, final int pos)
					{
						int x = 0;
						for (int i = 2; i >= 0; --i)
						    x = (x << 1) | (arr[i] ? 1 : 0);
						if (md)
						{
							ch.md = x;
							((Button)v).setText(mode(ch.md));
						}
						else
						{
							ch.au = x;
							((Button)v).setText(audio(ch.au));
						}
						spa.clf.changed(false);
					}
				});
				b.setNegativeButton(R.string.diag_cancel, null);
				b.setMultiChoiceItems(md ? R.array.md_items : R.array.au_items, arr, new OnMultiChoiceClickListener()
				{
					@Override
					public final void onClick(final DialogInterface di, final int pos, final boolean f)
					{
						arr[pos] = f;
					}
				});
				b.create().show();
			}
			else if (v.getId() == R.id.add_key) keyDialog(new Key(0, false, -1), true);
			else if (v.getTag() != null) keyDialog((Key)v.getTag(), false);
		}
		
		private final void keyDialog(final Key k, final boolean isnew)
		{
			final AlertDialog.Builder b = new AlertDialog.Builder(ma);
			b.setView(R.layout.keyeditor);
			b.setPositiveButton(R.string.diag_ok, new DialogInterface.OnClickListener()
			{
				@Override
				public final void onClick(final DialogInterface di, final int i)
				{
					k.code = lk;
					k.dn = ((CheckBox)d.findViewById(R.id.key_dn)).isChecked();
					k.dl = ((SeekBar)d.findViewById(R.id.key_dl)).getProgress() * 100;
					if (k.dl == 0) k.dl = -1;
					if (isnew) ch.ks.add(k);
					addKeys(LayoutInflater.from(ma), (ViewGroup)getView());
					spa.clf.changed(false);
				}
			});
			b.setNegativeButton(R.string.diag_cancel, null);
			if (!isnew)
				b.setNeutralButton(R.string.diag_rmv, new DialogInterface.OnClickListener()
				{
					@Override
					public final void onClick(final DialogInterface di, final int i)
					{
						edit.ks.remove(k);
						addKeys(LayoutInflater.from(ma), (ViewGroup)getView());
						spa.clf.changed(false);
					}
				});
			d = b.create();
			d.setOnShowListener(new OnShowListener()
			{
				@Override
				public final void onShow(final DialogInterface di)
				{
					((EditText)d.findViewById(R.id.key)).setText(key(lk = k.code));
					((CheckBox)d.findViewById(R.id.key_dn)).setChecked(k.dn);
					((SeekBar)d.findViewById(R.id.key_dl)).setProgress(k.dl == -1 ? 0 : k.dl / 100);
					final ToggleButton tb = (ToggleButton)d.findViewById(R.id.tgl_itc);
					tb.setOnCheckedChangeListener(new OnCheckedChangeListener()
					{
						@Override
						public final void onCheckedChanged(final CompoundButton cb, final boolean f)
						{
							requestIntercept(f);
						}
					});
				}
			});
			d.setOnDismissListener(new OnDismissListener()
			{
				@Override
				public final void onDismiss(final DialogInterface di)
				{
					requestIntercept(false);
				}
			});
			d.show();
		}
		
		private final void requestIntercept(final boolean state)
		{
            final Intent i = new Intent("xeed.xposed.cbppmod.Update");
            i.putExtra("xeed.xposed.cbppmod.Send", state);
            sendBroadcast(i);
		}
		
		public final void updateEdited()
		{
			ch = edit;
			final ViewGroup vg = (ViewGroup)getView();
			if (vg == null) return;
			((AutoCompleteTextView)vg.findViewById(R.id.ch_nm)).setText(ch.nm);
			final LayoutInflater li = LayoutInflater.from(ma);
			addKeys(li, vg);
			((Button)vg.findViewById(R.id.ch_act)).setText(action(ch.act));
			((Button)vg.findViewById(R.id.ch_md)).setText(mode(ch.md));
			((Button)vg.findViewById(R.id.ch_au)).setText(audio(ch.au));
			((CheckBox)vg.findViewById(R.id.ch_ccl)).setChecked(ch.ccl);
			((SeekBar)vg.findViewById(R.id.ch_vib)).setProgress(ch.vib / 50);
		}

		@Override
		public final void onFocusChange(final View v, final boolean f)
		{
			final TextView tv = (TextView)v;
			if (!f && !ch.nm.equals(tv.getText().toString()))
			{
				ch.nm = tv.getText().toString();
				spa.notifyDataSetChanged();
				spa.clf.changed(false);
			}
		}

		@Override
		public final void onCheckedChanged(final CompoundButton cb, final boolean c)
		{
			ch.ccl = c;
			spa.clf.changed(false);
		}

		@Override
		public final void onProgressChanged(final SeekBar sb, final int v, final boolean u)
		{
			ch.vib = v * 50;
			spa.clf.changed(false);
		}

		@Override
		public final void onStartTrackingTouch(final SeekBar seekBar) { }

		@Override
		public final void onStopTrackingTouch(final SeekBar sb) { }
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
