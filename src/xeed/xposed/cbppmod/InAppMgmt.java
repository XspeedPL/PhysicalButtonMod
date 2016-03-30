package xeed.xposed.cbppmod;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.vending.billing.IInAppBillingService;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.widget.Toast;

public final class InAppMgmt implements ServiceConnection
{
	static final String ITEM_EXCHAIN = "extraLenChains";
	
	private IInAppBillingService mService;

	@Override
	public final void onServiceDisconnected(final ComponentName cn)
	{
		mService = null;
	}

	@Override
	public final void onServiceConnected(final ComponentName cn, final IBinder b)
	{
		mService = IInAppBillingService.Stub.asInterface(b);
	}
	
	public final void bindService(final Context c)
	{
		final Intent it = new Intent("com.android.vending.billing.InAppBillingService.BIND");
		it.setPackage("com.android.vending");
		c.bindService(it, this, Context.BIND_AUTO_CREATE);
	}
	
	public final void unbindService(final Context c)
	{
		c.unbindService(this);
	}
	
	public final int getItemState(final String item)
	{
		try
		{
			final Bundle res = mService.getPurchases(3, "xeed.xposed.cbppmod", "inapp", null);
			final int resp = res.getInt("RESPONSE_CODE");
			if (resp == 0)
			{
				final ArrayList<String> items = res.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
				if (items.contains(item)) return 1;
			}
			return 0;
		}
		catch (final RemoteException ex)
		{
			return -1;
		}
	}
	
	public final int buyItem(final Activity a, final String item)
	{
		try
		{
			final Bundle res = mService.getBuyIntent(3, "xeed.xposed.cbppmod", item, "inapp", "ilikepotatoes");
			final int resp = res.getInt("RESPONSE_CODE");
			if (resp == 0)
			{
				final PendingIntent pendingIntent = res.getParcelable("BUY_INTENT");
				a.startIntentSenderForResult(pendingIntent.getIntentSender(), PBMain.REQ_INAPP, new Intent(), 0, 0, 0);
			}
			return 0;
		}
		catch (final Exception ex)
		{
			return -1;
		}
	}
	
	public final String buyResult(final Context c, final int resultCode, final Intent data)
	{
		final String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
		if (resultCode == Activity.RESULT_OK && data.getIntExtra("RESPONSE_CODE", 0) == 0)
		{
			try
			{
				final JSONObject jo = new JSONObject(purchaseData);
				return jo.getString("productId");
			}
			catch (final JSONException ex)
			{
				Toast.makeText(c, ex.getLocalizedMessage(), Toast.LENGTH_LONG).show();
				ex.printStackTrace();
			}
		}
		// TODO: Messages...
		else Toast.makeText(c, "FAIL", Toast.LENGTH_LONG).show();
		return null;
	}
}
