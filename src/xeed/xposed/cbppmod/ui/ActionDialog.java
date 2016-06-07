package xeed.xposed.cbppmod.ui;

import java.util.*;

import android.annotation.SuppressLint;
import android.content.*;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnShowListener;
import android.content.pm.*;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import xeed.library.common.AppInfo;
import xeed.library.common.Utils;
import xeed.library.ui.AppDialog;
import xeed.library.ui.BaseSettings;
import xeed.library.ui.FilteredAdapter;
import xeed.library.view.AppListView;
import xeed.xposed.cbppmod.*;

public final class ActionDialog
{
	private final ChainEditFragment cef;
	private final Action a;
	private final int th;
	
	public ActionDialog(final ChainEditFragment editor, final Action act, final int theme) { cef = editor; a = act; th = theme; }
	
	public final void show() { createDialog(-2); }
	
	private final void createAppDialog(final int type)
	{
		final Intent in = new Intent(type == 2 ? Intent.ACTION_CREATE_SHORTCUT : Intent.ACTION_MAIN);
		if (type == 0) in.addCategory(Intent.CATEGORY_LAUNCHER);
		AppDialog.create(cef.getActivity(), new AppDialog.AppInfoCollector<ResolveInfo>()
        {
		    @Override
		    public final ArrayList<AppInfo<ResolveInfo>> collectAsync(final PackageManager pm, final AppDialog.ProgressListener task)
            {
                final ArrayList<AppInfo<ResolveInfo>> res = new ArrayList<AppInfo<ResolveInfo>>();
                final ArrayList<ResolveInfo> ris = new ArrayList<ResolveInfo>();
                final List<PackageInfo> pkgs = pm.getInstalledPackages(0);
                task.postProgress(0, pkgs.size());
                for (int i = 0; i < pkgs.size(); ++i)
                {
                    in.setPackage(pkgs.get(i).packageName);
                    final List<ResolveInfo> activityList = pm.queryIntentActivities(in, 0);
                    for (final ResolveInfo ri : activityList) ris.add(ri);
                    task.postProgress(1, i + 1);
                }
                in.setPackage(null);
                task.postProgress(0, ris.size());
                Collections.sort(ris, new ResolveInfo.DisplayNameComparator(pm));
                for (int i = 0; i < ris.size(); ++i)
                {
                    final ResolveInfo ri = ris.get(i);
                    AppInfo<ResolveInfo> ai = new AppInfo<ResolveInfo>(ri);
                    ai.label = ri.loadLabel(pm).toString();
                    ai.icon = ri.loadIcon(pm);
                    ai.desc = ri.activityInfo.name;
                    res.add(ai);
                    task.postProgress(1, i + 1);
                }
                return res;
            }
        }, new AppDialog.AppInfoListener<ResolveInfo>()
        {
            @Override
            public final void onDialogResult(final AppInfo<ResolveInfo> ai)
            {
                if (type == 2)
                {
                    in.setComponent(new ComponentName(ai.data.activityInfo.packageName, ai.data.activityInfo.name));
                    cef.getActivity().startActivityForResult(in, PBMain.REQ_SHCUT);
                }
                else
                {
                    in.setComponent(new ComponentName(ai.data.activityInfo.packageName, ai.data.activityInfo.name));
                    change(Action.ACTION_OTHER, 0, ai.label + Action.OTHER_SPLT + in.toUri(0));
                }
            }
        }, th).show();
	}
	
	private final void createEditDialog(final int title, final View v, final EditFinishedListener l)
	{
		final AlertDialog.Builder b = new AlertDialog.Builder(cef.getContext(), th);
		b.setNegativeButton(R.string.diag_cancel, null);
		b.setPositiveButton(R.string.diag_ok, null);
		b.setTitle(title);
		b.setView(v);
		final AlertDialog ad = b.create();
		ad.setOnShowListener(new DialogInterface.OnShowListener()
		{
			@Override
			public final void onShow(final DialogInterface di)
			{
				l.setDialogInterface(di);
		        final Button b = ad.getButton(AlertDialog.BUTTON_POSITIVE);
		        b.setOnClickListener(l);
			}
		});
		ad.show();
	}
	
	private static abstract class EditFinishedListener implements View.OnClickListener
	{
		private DialogInterface di = null;
		
		@Override
		public final void onClick(final View v)
		{
			onClick(di);
		}
		
		public abstract void onClick(final DialogInterface di);
		
		public final void setDialogInterface(final DialogInterface diag) { di = diag; }
	}
	
	@SuppressLint("InflateParams")
    private final void createDialog(final int type)
	{
		final AlertDialog.Builder b = new AlertDialog.Builder(cef.getContext(), th);
		b.setTitle(R.string.diag_sel_act);
		if (type == -2)
		{
			b.setSingleChoiceItems(R.array.action_k, a.type + 1, new OnClickListener()
			{
				@Override
				public final void onClick(final DialogInterface di, final int i)
				{
					di.dismiss();
					if (i == 0) change(Action.ACTION_NONE, -1, null);
					else createDialog(i - 1);
				}
			});
		}
		else if (type == Action.ACTION_MEDIA)
		{
			int i = -1;
			if (a.type == Action.ACTION_MEDIA)
			{
				for (i = 0; i < Action.MEDIA.length; ++i)
					if (a.ex_i == Action.MEDIA[i])
						break;
			}
			b.setSingleChoiceItems(R.array.action_media_k, i, new OnClickListener()
			{
				@Override
				public final void onClick(final DialogInterface di, final int i)
				{
					di.dismiss();
					change(Action.ACTION_MEDIA, Action.MEDIA[i], null);
				}
			});
		}
		else if (type == Action.ACTION_KEY)
		{
			KeyListAdapter.inst.mSelected = a.type == Action.ACTION_KEY ? a.ex_i : -1;
			final View v = LayoutInflater.from(cef.getContext()).inflate(R.layout.srchdialog, null, false);
			v.findViewById(android.R.id.progress).setVisibility(View.GONE);
			final ListView lv = (ListView)v.findViewById(android.R.id.list);
			lv.setAdapter(KeyListAdapter.inst);
			lv.setSelection(KeyListAdapter.inst.mSelected);
			lv.setVisibility(View.VISIBLE);
			KeyListAdapter.inst.registerView((SearchView)v.findViewById(R.id.search));
			b.setView(v);
		}
		else if (type == Action.ACTION_CODED)
		{
			b.setSingleChoiceItems(R.array.action_coded_k, a.type == Action.ACTION_CODED ? a.ex_i : -1, new OnClickListener()
			{
				@Override
				public final void onClick(final DialogInterface di, final int i)
				{
					if (i == Action.CODED_PAPP && Build.VERSION.SDK_INT < 11) Toast.makeText(cef.getContext(), cef.getString(R.string.diag_and_req, "11 (Honeycomb)"), Toast.LENGTH_LONG).show();
					else if (i == Action.CODED_KFGD)
					{
					    di.dismiss();
					    final LinearLayout ll = new LinearLayout(cef.getContext());
					    ll.setOrientation(LinearLayout.VERTICAL);
					    final AppListView alv = new AppListView(cef.getContext());
					    ll.addView(alv);
					    final Button btn = new Button(cef.getContext());
					    btn.setText(R.string.btn_add_new);
					    btn.setOnClickListener(new View.OnClickListener()
					    {
                            @Override
                            public final void onClick(final View v)
                            {
                                AppDialog.create(cef.getContext(), new AppDialog.AppInfoCollector<String>()
                                {
                                    @Override
                                    public final ArrayList<AppInfo<String>> collectAsync(final PackageManager pm, final AppDialog.ProgressListener task)
                                    {
                                        final List<ApplicationInfo> pkgs = pm.getInstalledApplications(0);
                                        final ArrayList<AppInfo<String>> res = new ArrayList<AppInfo<String>>(pkgs.size());
                                        task.postProgress(0, pkgs.size());
                                        for (int i = 0; i < pkgs.size(); ++i)
                                        {
                                            final ApplicationInfo appi = pkgs.get(i);
                                            final AppInfo<String> ai = new AppInfo<String>(appi.packageName);
                                            ai.icon = appi.loadIcon(pm);
                                            ai.label = appi.loadLabel(pm).toString();
                                            res.add(ai);
                                            task.postProgress(1, i + 1);
                                        }
                                        return res;
                                    }
                                }, new AppDialog.AppInfoListener<String>()
                                {
                                    @Override
                                    public final void onDialogResult(final AppInfo<String> ai)
                                    {
                                        alv.addItem(ai);
                                    }
                                }, BaseSettings.getDiagTh()).show();
                            }
					    });
					    ll.addView(btn);
					    alv.setData(Utils.deserialize(a.type == Action.ACTION_CODED && a.ex_i == Action.CODED_KFGD ? a.ex_s : ""));
                        createEditDialog(R.string.diag_kill_opt, ll, new EditFinishedListener()
                        {
                            @Override
                            public final void onClick(final DialogInterface di)
                            {
                                change(Action.ACTION_CODED, Action.CODED_KFGD, Utils.serialize(alv.getData()));
                                di.dismiss();
                            }
                        });
					}
					else
					{
						di.dismiss();
						change(Action.ACTION_CODED, i, null);
					}
				}
			});
		}
		else if (type == Action.ACTION_OTHER)
		{
			b.setSingleChoiceItems(R.array.action_other_k, a.type == Action.ACTION_OTHER ? a.ex_i : -1, new OnClickListener()
			{
				@Override
				public final void onClick(final DialogInterface di, final int i)
				{
					di.dismiss();
					if (i < 3) createAppDialog(i);
					else
					{
						final EditText et = (EditText)View.inflate(cef.getContext(), R.layout.itneditor, null);
						if (a.type == Action.ACTION_OTHER) et.setText(a.getOtherIntent());
						createEditDialog(R.string.diag_edt_itn, et, new EditFinishedListener()
				        {
				            @Override
				            public final void onClick(final DialogInterface di)
				            {
								try
								{
									final Intent i = Intent.parseUri(et.getText().toString(), 0);
									change(Action.ACTION_OTHER, 3, "URI" + Action.OTHER_SPLT + i.toUri(0));
									di.dismiss();
								}
				                catch (final Exception ex)
								{
				                	Toast.makeText(cef.getContext(), R.string.diag_itn_err, Toast.LENGTH_LONG).show();
								}
				            }
				        });
					}
				}
			});
		}
		b.setNegativeButton(R.string.diag_cancel, null);
		final AlertDialog ad = b.create();
		if (type == Action.ACTION_KEY)
		{
		    ad.setOnShowListener(new OnShowListener()
		    {
                @Override
                public final void onShow(final DialogInterface di)
                {
                    ((ListView)ad.findViewById(android.R.id.list)).setOnItemClickListener(new OnItemClickListener()
                    {
                        @Override
                        public final void onItemClick(final AdapterView<?> av, final View v, final int i, final long id)
                        {
                            di.dismiss();
                            final CheckBox cb = new CheckBox(cef.getContext());
                            cb.setChecked(a.type == Action.ACTION_KEY && a.isKeyLong());
                            cb.setText(R.string.diag_prs_lng);
                            createEditDialog(R.string.diag_prs_opt, cb, new EditFinishedListener()
                            {
                                @Override
                                public final void onClick(final DialogInterface di)
                                {
                                    change(Action.ACTION_KEY, (int)id, cb.isChecked() ? "long" : "");
                                    di.dismiss();
                                }
                            });
                        }
                    });
                }
		    });
		}
		ad.show();
	}
	
	private final void change(final int type, final int ex_i, final String ex_s)
	{
		a.type = type;
		a.ex_i = ex_i;
		a.ex_s = ex_s;
		cef.actionChanged();
	}
	
	private static final class KeyListAdapter extends FilteredAdapter<Integer> implements Comparator<Integer>
	{
	    static final KeyListAdapter inst = new KeyListAdapter();
	    
		public int mSelected;
		
		private KeyListAdapter()
		{
		    mData.ensureCapacity(PBMain.keyDb.size());
			for (int i = PBMain.keyDb.size() - 1; i >= 0; --i)
				mData.add(PBMain.keyDb.keyAt(i));
			Collections.sort(mData, this);
			getFilter().filter("");
		}

		protected final String getItemDescLower(final Integer item)
		{
		    return PBMain.key(item).toLowerCase(Locale.getDefault());
		}
		
		protected final boolean isDataReady() { return true; }
		
		@Override
		public final long getItemId(final int pos)
		{
			return mFiltered.get(pos);
		}

		@Override
		public final View getView(int pos, View v, ViewGroup vg)
		{
			if (v == null)
			{
				final LayoutInflater li = LayoutInflater.from(vg.getContext());
				v = li.inflate(android.R.layout.simple_list_item_single_choice, vg, false);
			}
			final CheckedTextView ctv = (CheckedTextView)v;
			final int key = mFiltered.get(pos);
			ctv.setText(PBMain.keyDb.get(key) + " (" + key + ")");
			ctv.setChecked(key == mSelected);
			return v;
		}

		@Override
		public final int compare(final Integer lhs, final Integer rhs)
		{
			return PBMain.key(lhs).compareTo(PBMain.key(rhs));
		}
	}
}
