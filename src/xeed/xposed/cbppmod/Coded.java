package xeed.xposed.cbppmod;

import java.util.Arrays;
import java.util.List;

import android.annotation.TargetApi;
import android.app.*;
import android.content.*;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.camera2.*;
import android.os.*;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;
import de.robv.android.xposed.XposedHelpers;

@SuppressWarnings("deprecation")
public final class Coded
{
    public static final int SDK = Build.VERSION.SDK_INT;
    
	public static final String IACT_TOGGLE_LIGHT = "xeed.xposed.cbppmod.TOGGLE_LIGHT";
	private static Context app = null;
	
	public static final void setAppCtx(final Context c) { app = c; }
	
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
		private Notification mTorchNotif = null;
	    private TorchAccess mAccess = null;
	    private boolean state = false;
		
		@Override
		public final IBinder onBind(final Intent i) { return null; }

		@Override
		public final void onCreate()
		{
			super.onCreate();
			if (app == null) app = this;
			if (SDK > 22) mAccess = new TorchNew();
			else mAccess = new TorchOld();
			final NotificationCompat.Builder b = new NotificationCompat.Builder(this);
			b.setContentTitle(getResources().getStringArray(R.array.action_coded_k)[Action.CODED_FLHT]);
			b.setSmallIcon(android.R.drawable.ic_menu_camera);
			b.setContentIntent(PendingIntent.getService(this, 0, new Intent(this, LightService.class), 0));
			mTorchNotif = b.build();
		}

		@Override
		public final int onStartCommand(final Intent i, final int f, final int id)
		{
			if (i != null && i.getAction() == IACT_TOGGLE_LIGHT && !state && mAccess.toggle(true))
			{
			    state = true;
			    startForeground(2, mTorchNotif);
			    return START_REDELIVER_INTENT;
			}
			stopSelf();
			return START_NOT_STICKY;
		}
		
		@Override
	    public final void onDestroy()
		{
	        mAccess.close();
	        super.onDestroy();
	    }
		
		private interface TorchAccess
		{
		    public boolean toggle(final boolean on);
		    public void close();
		}
		
		private final class TorchOld implements TorchAccess
		{
		    private Camera mCam = null;
		    
            @Override
            public final void close()
            {
                if (mCam != null) toggle(false);
                mCam.release();
                mCam = null;
            }

            @Override
            public synchronized final boolean toggle(final boolean on)
            {
                if (mCam == null) mCam = Camera.open();
                if (mCam == null) return false;
                final Parameters p = mCam.getParameters();
                if (on)
                {
                    final List<String> l = p.getSupportedFlashModes();
                    if (l != null && l.contains(Parameters.FLASH_MODE_TORCH))
                    {
                        p.setFlashMode(Parameters.FLASH_MODE_TORCH);
                        mCam.setParameters(p);
                        mCam.startPreview();
                    }
                    else return false;
                }
                else
                {
                    p.setFlashMode(Parameters.FLASH_MODE_OFF);
                    mCam.setParameters(p);
                    mCam.stopPreview();
                }
                return on;
            }
		}
		
		@TargetApi(23)
		private final class TorchNew implements TorchAccess
		{
		    @Override
		    public final void close() { toggle(false); }
		    
		    @Override
		    public final boolean toggle(final boolean on)
		    {
                final CameraManager cammgr = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
                try
                {
                    final String[] ids = cammgr.getCameraIdList();
                    for (final String id : ids)
                    {
                        final CameraCharacteristics cc = cammgr.getCameraCharacteristics(id);
                        if (cc.get(CameraCharacteristics.FLASH_INFO_AVAILABLE))
                        {
                            cammgr.setTorchMode(id, on);
                            return on;
                        }
                    }
                    showError("No flash");
                    return !on;
                }
                catch (final Exception ex)
                {
                    showError(ex.getLocalizedMessage());
                    return !on;
                }
		    }
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
	
    @TargetApi(11)
	public static final void switchToLastApp(final Context c)
	{
		if (SDK > 10)
		{
		    final ActivityManager am = (ActivityManager)c.getSystemService(Context.ACTIVITY_SERVICE);
			int lastAppId = 0;
			final Intent intent = new Intent(Intent.ACTION_MAIN);
			String homePackage = "com.android.launcher";
			intent.addCategory(Intent.CATEGORY_HOME);
			final ResolveInfo res = c.getPackageManager().resolveActivity(intent, 0);
			if (res.activityInfo != null && !res.activityInfo.packageName.equals("android"))
				homePackage = res.activityInfo.packageName;
			final List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(5);
			for (int i = 1; i < tasks.size(); ++i)
			{
				final String pkg = tasks.get(i).topActivity.getPackageName();
				if (!homePackage.equals(pkg) && !pkg.equals("com.android.systemui"))
				{
					lastAppId = tasks.get(i).id;
					break;
				}
			}
			if (lastAppId != 0) am.moveTaskToFront(lastAppId, 0);
		}
		else showError(app.getString(R.string.diag_and_req, "11 (Honeycomb)"));
    }
	
    public static final void killForeground(final Context c, final String topPkg, final String data)
    {
        try
        {
            final List<String> wlist = Arrays.asList(data.split(" "));
            if (wlist.contains(topPkg)) return;
            final ApplicationInfo ai = c.getPackageManager().getApplicationInfo(topPkg, 0);
            XposedHelpers.callMethod(c.getSystemService(Context.ACTIVITY_SERVICE), "forceStopPackage", topPkg);
            Toast.makeText(c, app.getString(R.string.diag_kill, ai.loadLabel(c.getPackageManager()).toString()), Toast.LENGTH_SHORT).show();
        }
        catch (final Exception ex) { showError(ex.getLocalizedMessage()); }
    }
    
	private static final void showError(final String txt)
	{
	    Toast.makeText(app, "ERROR: " + txt, Toast.LENGTH_SHORT).show();
	}
}
