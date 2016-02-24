package xeed.xposed.cbppmod;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;

@SuppressWarnings("deprecation")
public final class Coded
{
	public static final String IACT_TOGGLE_LIGHT = "xeed.xposed.cbppmod.TOGGLE_LIGHT";
	
	public static final class AppBridge extends BroadcastReceiver
	{
		@Override
		public final void onReceive(final Context c, final Intent i)
		{
			if (i != null && i.getAction() == IACT_TOGGLE_LIGHT)
			{
				final Intent ni = new Intent(c, LightService.class);
		        ni.setAction(IACT_TOGGLE_LIGHT);
		        c.startService(ni);
			}
		}
	}
	
	public static final class LightService extends Service
	{
		private Camera cam = null;
		private Notification mTorchNotif;
	    private PendingIntent mPendingIntent;
		
		@Override
		public final IBinder onBind(final Intent i) { return null; }

		@Override
		public final void onCreate()
		{
			super.onCreate();
			final NotificationCompat.Builder b = new NotificationCompat.Builder(this);
			b.setContentTitle(getResources().getStringArray(R.array.action_coded_k)[Action.CODED_FLHT]);
			b.setSmallIcon(android.R.drawable.ic_menu_camera);
			mPendingIntent = PendingIntent.getService(this, 0, new Intent(this, LightService.class), 0);
			b.setContentIntent(mPendingIntent);
			mTorchNotif = b.build();
		}

		@Override
		public final int onStartCommand(final Intent i, final int f, final int id)
		{
			if (i != null && i.getAction() == IACT_TOGGLE_LIGHT)
			{
				return toggleFlash(true) ? START_REDELIVER_INTENT : START_NOT_STICKY;
			}
			stopSelf();
			return START_NOT_STICKY;
		}
		
		@Override
	    public final void onDestroy()
		{
	        toggleFlash(false);
	        super.onDestroy();
	    }
		
		public synchronized final boolean toggleFlash(boolean on)
		{
			if (cam == null) cam = Camera.open();
			else if (on) on = false;
			if (cam == null) return false;
			final Parameters p = cam.getParameters();
			if (p == null) return false;
			if (on)
			{
				p.setFlashMode(Parameters.FLASH_MODE_TORCH);
				cam.setParameters(p);
				cam.startPreview();
				startForeground(2, mTorchNotif);
			}
			else
			{
				p.setFlashMode(Parameters.FLASH_MODE_OFF);
				cam.setParameters(p);
				cam.stopPreview();
				cam.release();
				cam = null;
				stopForeground(true);
				stopSelf();
			}
			return on;
		}
	}

	public static final void toggleFlash(final Context c)
	{
		final Intent i = new Intent(c, LightService.class);
		i.setAction(IACT_TOGGLE_LIGHT);
        c.startService(i);
	}
	
	public static final void softReboot() throws Exception
	{
		Runtime.getRuntime().exec(new String[] { "setprop", "ctl.restart", "surfaceflinger" }).waitFor();
		Runtime.getRuntime().exec(new String[] { "setprop", "ctl.restart", "zygote" }).waitFor();
	}
}
