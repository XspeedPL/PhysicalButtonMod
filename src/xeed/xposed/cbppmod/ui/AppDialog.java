package xeed.xposed.cbppmod.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.*;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.*;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.view.*;
import android.widget.*;
import xeed.xposed.cbppmod.R;

public final class AppDialog extends AlertDialog.Builder implements OnClickListener
{
	private final AppInfoAdapter aia;
	private final AppInfoListener ail;
	
	private AppDialog(final Context c, final AppInfoListener l, final Intent qry)
	{
		super(c);
		setTitle(R.string.diag_sel_act);
		setNegativeButton(R.string.diag_cancel, null);
		final ProgressBar pb = new ProgressBar(c, null, android.R.attr.progressBarStyleHorizontal);
		pb.setIndeterminate(false);
		setView(pb);
		ail = l;
		aia = new AppInfoAdapter(c.getPackageManager(), pb, qry);
		setAdapter(aia, this);
	}

	@Override
	public final void onClick(final DialogInterface di, final int pos)
	{
		ail.onDialogResult(aia.getInfo(pos));
	}
	
	public static final AlertDialog create(final Context c, final AppInfoListener l, final Intent qry)
	{
		return new AppDialog(c, l, qry).create();
	}
	
	public static final class AppInfo
	{
		final CharSequence name;
		final ResolveInfo ri;
		Drawable icon;
		
		public AppInfo(final CharSequence app, final ResolveInfo data)
		{
			name = app; ri = data; icon = null;
		}
		
		@Override
		public final int hashCode() { return name.hashCode(); }
	}
	
	public interface AppInfoListener
	{
		public void onDialogResult(final AppInfo info);
	}
	
	public static final class AppInfoAdapter extends BaseAdapter
	{
		private final ProgressBar pb;
		private final PackageManager pm;
		private final Intent in;
		
		private ArrayList<AppInfo> data = new ArrayList<AppInfo>(0);
		
		public final AppInfo getInfo(final int pos)
		{
			return data.get(pos);
		}
		
		protected final class Task extends AsyncTask<Void, Integer, ArrayList<AppInfo>>
		{
			@Override
			protected final ArrayList<AppInfo> doInBackground(final Void... params)
			{
				final ArrayList<AppInfo> res = new ArrayList<AppInfo>();
				final ArrayList<ResolveInfo> ris = new ArrayList<ResolveInfo>();
				final List<PackageInfo> pkgs = pm.getInstalledPackages(0);
				publishProgress(0, pkgs.size());
				for (int i = 0; i < pkgs.size(); ++i)
				{
					if (isCancelled()) break;
					in.setPackage(pkgs.get(i).packageName);
					final List<ResolveInfo> activityList = pm.queryIntentActivities(in, 0);
					for (final ResolveInfo ri : activityList) ris.add(ri);
					publishProgress(1, i + 1);
				}
				publishProgress(0, ris.size());
				Collections.sort(ris, new ResolveInfo.DisplayNameComparator(pm));
				for (int i = 0; i < ris.size(); ++i)
				{
					final ResolveInfo ri = ris.get(i);
					if (isCancelled()) break;
					CharSequence appName = ri.loadLabel(pm);
					AppInfo ai = new AppInfo(appName, ri);
					ai.icon = ri.loadIcon(pm);
					res.add(ai);
					publishProgress(1, i + 1);
				}
				return res;
			}

			@Override
			protected final void onProgressUpdate(final Integer... args)
			{
				if (args[0] == 0)
				{
					pb.setMax(args[1]);
					pb.setProgress(0);
				}
				else if (args[0] == 1) pb.setProgress(args[1]);
			}
			
			@Override
			protected final void onPostExecute(final ArrayList<AppInfo> res)
			{
				pb.setVisibility(View.GONE);
				((ViewGroup)pb.getParent()).setVisibility(View.GONE);
				((ViewGroup)pb.getParent().getParent()).setVisibility(View.GONE);
				data.clear();
				data = res;
				notifyDataSetChanged();
			}
		}
		
		protected AppInfoAdapter(final PackageManager pkg, final ProgressBar pbv, final Intent qry)
		{
			pb = pbv;
			pm = pkg;
			in = qry;
			new Task().execute(new Void[0]);
		}

		@Override
		public final int getCount() { return data.size(); }

		@Override
		public final Object getItem(final int pos) { return data.get(pos); }

		@Override
		public final long getItemId(final int pos) { return data.get(pos).hashCode(); }

		@Override
		public final View getView(final int pos, View v, final ViewGroup vg)
		{
			final LayoutInflater li = (LayoutInflater)vg.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final AppInfo ai = data.get(pos);
			if (v == null) v = li.inflate(R.layout.short_child, vg, false);
			final ImageView iv = (ImageView)v.findViewById(R.id.icon);
			iv.setImageDrawable(ai.icon);
			final TextView tv = (TextView)v.findViewById(R.id.name);
			tv.setText(ai.name);
			return v;
		}
	}
}
