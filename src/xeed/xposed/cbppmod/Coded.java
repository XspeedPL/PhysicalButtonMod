package xeed.xposed.cbppmod;

import java.util.List;

import android.annotation.TargetApi;
import android.app.*;
import android.content.*;
import android.content.pm.ResolveInfo;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.camera2.*;
import android.os.Build;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public final class Coded
{
    public static final int SDK = Build.VERSION.SDK_INT;
    
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
		private Notification mTorchNotif = null;
	    private PendingIntent mPendingIntent = null;
	    private TorchAccess mAccess = null;
		
		@Override
		public final IBinder onBind(final Intent i) { return null; }

		@Override
		public final void onCreate()
		{
			super.onCreate();
			if (SDK > 22) mAccess = new TorchNew();
			else mAccess = new TorchOld();
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
			if (i != null && i.getAction() == IACT_TOGGLE_LIGHT && mAccess.toggle(true))
			{
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
                    showError(LightService.this, "No flash");
                    return !on;
                }
                catch (final Exception ex)
                {
                    showError(LightService.this, ex.getLocalizedMessage());
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
	
    @TargetApi(12)
	public static final void switchToLastApp(final Context c, final ActivityManager am)
	{
		if (SDK > 10)
		{
			int lastAppId = 0;
			final Intent intent = new Intent(Intent.ACTION_MAIN);
			String homePackage = "com.android.launcher";
			intent.addCategory(Intent.CATEGORY_HOME);
			final ResolveInfo res = c.getPackageManager().resolveActivity(intent, 0);
			if (res.activityInfo != null && !res.activityInfo.packageName.equals("android"))
			{
				homePackage = res.activityInfo.packageName;
			}
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
			if (lastAppId != 0)
			{
				am.moveTaskToFront(lastAppId, SDK > 11 ? ActivityManager.MOVE_TASK_NO_USER_ACTION : 0);
				return;
			}
		}
		showError(c, "Android ver < 11");
    }
	
	private static final void showError(final Context c, final String txt)
	{
	    Toast.makeText(c, "ERROR: " + txt, Toast.LENGTH_SHORT).show();
	}
}
