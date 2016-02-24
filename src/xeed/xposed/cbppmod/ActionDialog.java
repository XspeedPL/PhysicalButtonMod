package xeed.xposed.cbppmod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.database.DataSetObserver;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.Toast;
import xeed.xposed.cbppmod.PBMain.ChainEditFragment;

public final class ActionDialog
{
	private final ChainEditFragment cef;
	private final Action a;
	
	public ActionDialog(final ChainEditFragment editor, final Action act) { cef = editor; a = act; }
	
	public final void show() { createDialog(-2); }
	
	private final void createAppDialog(final int type)
	{
		final Intent i = new Intent(type == 2 ? Intent.ACTION_CREATE_SHORTCUT : Intent.ACTION_MAIN);
		final AppDialog.AppInfoListener l = new AppDialog.AppInfoListener()
		{
			@Override
			public final void onDialogResult(final AppDialog.AppInfo ai)
			{
				if (type == 2)
				{
					i.setComponent(new ComponentName(ai.ri.activityInfo.packageName, ai.ri.activityInfo.name));
					cef.startActivityForResult(i, 1028);
				}
				else
				{
					i.setComponent(new ComponentName(ai.ri.activityInfo.packageName, ai.ri.activityInfo.name));
					change(Action.ACTION_OTHER, 0, ai.name + " <-> " + i.toUri(0));
				}
			}
		};
		if (type == 0) i.addCategory(Intent.CATEGORY_LAUNCHER);
		AppDialog.create(cef.getActivity(), l, i).show();
	}
	
	private final void createEditDialog()
	{
		final AlertDialog.Builder b = new AlertDialog.Builder(cef.getContext());
		b.setTitle(R.string.diag_edt_itn);
		final EditText et = (EditText)View.inflate(cef.getContext(), R.layout.itneditor, null);
		if (a.type == Action.ACTION_OTHER) et.setText(a.ex_s.substring(a.ex_s.indexOf(" <-> ") + 5));
		b.setView(et);
		b.setNegativeButton(R.string.diag_cancel, null);
		b.setPositiveButton(R.string.diag_ok, null);
		final AlertDialog ad = b.create();
		ad.setOnShowListener(new DialogInterface.OnShowListener()
		{
		    @Override
		    public final void onShow(final DialogInterface di)
		    {
		        final Button b = ad.getButton(AlertDialog.BUTTON_POSITIVE);
		        b.setOnClickListener(new View.OnClickListener()
		        {
		            @Override
		            public void onClick(View view)
		            {
						try
						{
							final Intent i = Intent.parseUri(et.getText().toString(), 0);
							change(Action.ACTION_OTHER, 3, "URI <-> " + i.toUri(0));
							di.dismiss();
						}
		                catch (final Exception ex)
						{
		                	Toast.makeText(cef.getContext(), R.string.diag_itn_err, Toast.LENGTH_LONG).show();
						}
		            }
		        });
		    }
		});
		ad.show();
	}
	
	private final void createDialog(final int type)
	{
		final AlertDialog.Builder b = new AlertDialog.Builder(cef.getContext());
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
			KeyListAdapter.inst.mSelected = a.type == Action.ACTION_KEY ? KeyListAdapter.inst.mKeys.indexOf(a.ex_i) : -1;
			b.setSingleChoiceItems(KeyListAdapter.inst, KeyListAdapter.inst.mSelected, new OnClickListener()
			{
				@Override
				public final void onClick(final DialogInterface di, final int i)
				{
					di.dismiss();
					change(Action.ACTION_KEY, (int)KeyListAdapter.inst.getItemId(i), null);
				}
			});
		}
		else if (type == Action.ACTION_CODED)
		{
			b.setSingleChoiceItems(R.array.action_coded_k, a.type == Action.ACTION_CODED ? a.ex_i : -1, new OnClickListener()
			{
				@Override
				public final void onClick(final DialogInterface di, final int i)
				{
					di.dismiss();
					change(Action.ACTION_CODED, i, null);
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
					else createEditDialog();
				}
			});
		}
		b.setNegativeButton(R.string.diag_cancel, null);
		b.create().show();
	}
	
	private final void change(final int type, final int ex_i, final String ex_s)
	{
		a.type = type;
		a.ex_i = ex_i;
		a.ex_s = ex_s;
		cef.actionChanged();
	}
	
	private static final class KeyListAdapter implements ListAdapter, Comparator<Integer>
	{
		public static final KeyListAdapter inst = new KeyListAdapter();
		public int mSelected;
		private final ArrayList<Integer> mKeys = new ArrayList<Integer>(PBMain.keyDb.size());
		
		private KeyListAdapter()
		{
			for (int i = PBMain.keyDb.size() - 1; i >= 0; --i)
				mKeys.add(PBMain.keyDb.keyAt(i));
			Collections.sort(mKeys, this);
		}
		
		@Override
		public final void registerDataSetObserver(DataSetObserver dso) { }

		@Override
		public final void unregisterDataSetObserver(DataSetObserver dso) { }

		@Override
		public final int getCount()
		{
			return mKeys.size();
		}

		@Override
		public final Object getItem(final int pos)
		{
			return mKeys.get(pos);
		}

		@Override
		public final long getItemId(final int pos)
		{
			return mKeys.get(pos);
		}

		@Override
		public final boolean hasStableIds() { return true; }

		@Override
		public final View getView(int pos, View v, ViewGroup vg)
		{
			if (v == null)
			{
				final LayoutInflater li = LayoutInflater.from(vg.getContext());
				v = li.inflate(android.R.layout.simple_list_item_single_choice, vg, false);
			}
			final CheckedTextView ctv = (CheckedTextView)v;
			final int key = mKeys.get(pos);
			ctv.setText(PBMain.keyDb.get(key) + " (" + key + ")");
			ctv.setChecked(pos == mSelected);
			return v;
		}

		@Override
		public final int getItemViewType(int pos) { return 0; }

		@Override
		public final int getViewTypeCount() { return 1; }

		@Override
		public final boolean isEmpty() { return false; }

		@Override
		public final boolean areAllItemsEnabled() { return true; }

		@Override
		public final boolean isEnabled(int pos) { return true; }

		@Override
		public final int compare(final Integer lhs, final Integer rhs)
		{
			return PBMain.key(lhs).compareTo(PBMain.key(rhs));
		}
	}
}
