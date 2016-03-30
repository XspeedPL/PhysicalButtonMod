package xeed.xposed.cbppmod.ui;

import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.DragSortListView.DragSortListener;

import android.content.*;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import xeed.xposed.cbppmod.*;

public final class ChainListFragment extends Fragment implements DragSortListener, OnItemClickListener
{
    private final ChainAdapter ca = new ChainAdapter();
    private PBMain pb = null;
    private PagerAdapter pa = null;

    @Override
    public final void onCreate(final Bundle b)
    {
        super.onCreate(b);
        pb = (PBMain)getActivity();
        pa = pb.getPager();
        setRetainInstance(true);
    }
    
    @Override
    public final View onCreateView(final LayoutInflater li, final ViewGroup vg, final Bundle b)
    {
        if (!pa.getIterator().hasNext() && PBMain.getActiveVerCode() != 0)
        {
            final AlertDialog.Builder bu = new AlertDialog.Builder(pb);
            bu.setMessage(R.string.diag_no_chs);
            bu.setNegativeButton(R.string.diag_no, null);
            bu.setPositiveButton(R.string.diag_yes, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(final DialogInterface di, final int which)
                {
                    Chain ch = new Chain(pb.sp, "Original camera");
                    ch.act = new Action(Action.ACTION_KEY, KeyEvent.KEYCODE_CAMERA);
                    ch.vib = 0;
                    ch.setEz(3, KeyEvent.KEYCODE_CAMERA, 3000);
                    pa.addChain(ch);
                    
                    ch = new Chain(pb.sp, "Play pause");
                    ch.act = new Action(Action.ACTION_MEDIA, Action.MEDIA_PLAY);
                    ch.setEz(0, KeyEvent.KEYCODE_CAMERA, 0);
                    pa.addChain(ch);
                    
                    ch = new Chain(pb.sp, "Next track");
                    ch.act = new Action(Action.ACTION_MEDIA, Action.MEDIA_NEXT);
                    ch.setEz(3, KeyEvent.KEYCODE_VOLUME_UP, 500);
                    pa.addChain(ch);
                    
                    ch = new Chain(pb.sp, "Original vol-up");
                    ch.act = new Action(Action.ACTION_KEY, KeyEvent.KEYCODE_VOLUME_UP);
                    ch.vib = 0;
                    ch.setEz(0, KeyEvent.KEYCODE_VOLUME_UP, 0);
                    pa.addChain(ch);
                    
                    ch = new Chain(pb.sp, "Previous track");
                    ch.act = new Action(Action.ACTION_MEDIA, Action.MEDIA_PREV);
                    ch.setEz(3, KeyEvent.KEYCODE_VOLUME_DOWN, 500);
                    pa.addChain(ch);
                    
                    ch = new Chain(pb.sp, "Original vol-dn");
                    ch.act = new Action(Action.ACTION_KEY, KeyEvent.KEYCODE_VOLUME_DOWN);
                    ch.vib = 0;
                    ch.setEz(0, KeyEvent.KEYCODE_VOLUME_DOWN, 0);
                    pa.addChain(ch);
                    
                    changed(false);
                }
            });
            bu.create().show();
        }
        final LinearLayout ll = (LinearLayout)li.inflate(R.layout.chainlist, vg, false);
        ((TextView)ll.findViewById(R.id.status)).setText(getString(R.string.diag_sts) + ": " + getString(R.string.diag_sav));
        final DragSortListView dlv = (DragSortListView)ll.findViewById(R.id.chainlist);
        dlv.setAdapter(ca);
        dlv.setDragSortListener(this);
        dlv.setOnItemClickListener(this);
        return ll;
    }
    
    @Override
    public final void onResume()
    {
        super.onResume();
        changed(pa.saved);
    }
    
    @Override
    public final void drop(final int from, final int to)
    {
        if (from != to)
        {
            final Chain ch = pa.getChains().remove(from);
            pa.getChains().add(to, ch);
            changed(false);
        }
    }
    
    final void changed(final boolean save)
    {
        pa.saved = save;
        ca.notifyDataSetChanged();
        final LinearLayout ll = (LinearLayout)getView();
        if (ll != null)
        {
            final TextView tv = (TextView)ll.findViewById(R.id.status);
            tv.setText(getString(R.string.diag_sts) + ": " + getString(save ? R.string.diag_sav : R.string.diag_nsav));
            tv.setTextColor(save ? 0xff00ff00 : 0xffff0000);
        }
    }
    
    private final class ChainAdapter extends BaseAdapter
    {
        @Override
        public final int getCount() { return pa.getChains().size(); }

        @Override
        public final Object getItem(int i) { return pa.getChains().get(i); }

        @Override
        public final long getItemId(int i) { return pa.getChains().get(i).hashCode(); }

        @Override
        public final View getView(final int pos, final View reuse, final ViewGroup vg)
        {
            final LayoutInflater li = LayoutInflater.from(pb);
            final Chain ch = pa.getChains().get(pos);
            final LinearLayout ll;
            if (reuse != null) ll = (LinearLayout)reuse;
            else ll = (LinearLayout)li.inflate(R.layout.chainitem, vg, false);
            final ViewGroup keys = (ViewGroup)ll.findViewById(R.id.keys);
            PBMain.populateKeys(keys, li, ch, null);
            ((TextView)ll.findViewById(R.id.name)).setText(ch.nm);
            ((TextView)ll.findViewById(R.id.desc)).setText(getString(R.string.diag_md) + ": " + pb.mode(ch.md) + ", " + getString(R.string.diag_au) + ": " + pb.audio(ch.au));
            return ll;
        }
    }

    @Override
    public final void onItemClick(final AdapterView<?> av, final View v, final int pos, final long id)
    {
        pa.setEdit(pa.getChains().get(pos));
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
                if (pa.getChains().get(pos) == pa.edit) pa.setEdit(null);
                pa.getChains().remove(pos);
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
