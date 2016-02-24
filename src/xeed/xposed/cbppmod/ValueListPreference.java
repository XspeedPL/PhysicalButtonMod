package xeed.xposed.cbppmod;

import android.app.AlertDialog.Builder;
import android.content.*;
import android.content.DialogInterface.OnClickListener;
import android.preference.ListPreference;
import android.util.AttributeSet;

public final class ValueListPreference extends ListPreference
{
	public ValueListPreference(final Context c)
	{
		super(c);
	}
	
	public ValueListPreference(final Context c, final AttributeSet as)
	{
		super(c, as);
	}
	
	@Override
	protected final void onPrepareDialogBuilder(final Builder b)
	{
		super.onPrepareDialogBuilder(b);
		final String s = getPersistedString("");
		int ix = 0;
		for (int i = getEntryValues().length - 1; i >= 0; --i)
			if (s.equals(getEntryValues()[i]))
			{
				ix = i;
				break;
			}
		b.setSingleChoiceItems(getEntries(), ix, new OnClickListener()
		{
			@Override
			public final void onClick(final DialogInterface di, final int pos)
			{
				if (shouldPersist()) persistString(getEntryValues()[pos].toString());
				di.dismiss();
			}
		});
	}
}
