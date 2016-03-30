package xeed.xposed.cbppmod.ui;

import java.util.Iterator;
import java.util.LinkedList;

import android.content.*;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ToggleButton;
import xeed.xposed.cbppmod.*;

public final class KeyTestFragment extends Fragment implements OnCheckedChangeListener
{
    private static final int maxlen = 8;
    private static final LinkedList<Key> chain = new LinkedList<Key>();
    private final Chain ch = new Chain(null, "");
    private PBMain pb = null;
    private BroadcastReceiver br = null;
    private ToggleButton tb = null;
    
    public final void onCreate(final Bundle b)
    {
        super.onCreate(b);
        pb = (PBMain)getActivity();
        br = new BroadcastReceiver()
        {
            @Override
            public final void onReceive(final Context c, final Intent i)
            {
                final int key = i.getIntExtra("xeed.xposed.cbppmod.Key", -1);
                if (key != -1)
                {
                    final boolean down = i.getBooleanExtra("xeed.xposed.cbppmod.Down", true);
                    keyHook(key, down);
                    keysChanged();
                }
            }
        };
    }

    private final void keysChanged()
    {
        ch.ks.clear();
        ch.ks.addAll(chain);
        PBMain.populateKeys((ViewGroup)getView().findViewById(R.id.keys), LayoutInflater.from(pb), ch, null);
    }
    
    @Override
    public final View onCreateView(final LayoutInflater li, final ViewGroup vg, final Bundle b)
    {
        final ViewGroup ret = (ViewGroup)li.inflate(R.layout.test, vg, false);
        tb = (ToggleButton)ret.findViewById(R.id.tgl_itc);
        tb.setOnCheckedChangeListener(this);
        ch.ks.clear();
        ch.ks.addAll(chain);
        PBMain.populateKeys((ViewGroup)ret.findViewById(R.id.keys), li, ch, null);
        return ret;
    }
    
    private final void keyHook(final int key, final boolean down)
    {
        final long t = SystemClock.elapsedRealtime();
        if (!chain.isEmpty())
        {
            final Iterator<Key> it = chain.descendingIterator();
            while (it.hasNext())
            {
                final Key k = it.next();
                if (k.code == key)
                {
                    k.dl = ((int)(t - k.sw) / 10) * 10;
                    break;
                }
            }
        }
        chain.add(new Key(key, down, 0));
        while (chain.size() > maxlen) chain.removeFirst();
    }
    
    @Override
    public final void setUserVisibleHint(final boolean vis)
    {
        super.setUserVisibleHint(vis);
        if (!vis && pb != null) tb.setChecked(false);
    }
    
    @Override
    public final void onPause()
    {
        tb.setChecked(false);
        pb.unregisterReceiver(br);
        super.onPause();
    }
    
    @Override
    public final void onResume()
    {
        super.onResume();
        pb.registerReceiver(br, new IntentFilter("xeed.xposed.cbppmod.Key"), null, null);
    }

    @Override
    public final void onCheckedChanged(final CompoundButton cb, final boolean f)
    {
        pb.requestIntercept(f ? 1 : 0);
    }
}
