package xeed.xposed.cbppmod.ui;

import java.util.ListIterator;

import android.content.*;
import android.content.DialogInterface.*;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar.OnSeekBarChangeListener;
import xeed.xposed.cbppmod.*;

public final class ChainEditFragment extends Fragment implements OnFocusChangeListener, OnCheckedChangeListener, OnClickListener, OnSeekBarChangeListener, OnItemSelectedListener
{
    private PBMain pb = null;
    private PagerAdapter pa = null;
    private AlertDialog d = null;
    private BroadcastReceiver br = null;
    private int lk = -1;
    private boolean layout = true;
    
    public final void onCreate(final Bundle b)
    {
        super.onCreate(b);
        pb = (PBMain)getActivity();
        pa = pb.getPager();
        setRetainInstance(true);
        br = new BroadcastReceiver()
        {
            @Override
            public final void onReceive(final Context c, final Intent i)
            {
                lk = i.getIntExtra("xeed.xposed.cbppmod.Key", -1);
                if (lk != -1 && d != null)
                    ((EditText)d.findViewById(R.id.key)).setText(PBMain.key(lk));
            }
        };
        pb.registerReceiver(br, new IntentFilter("xeed.xposed.cbppmod.Key"), null, null);
    }
    
    @Override
    public final void onDestroy()
    {
        pb.unregisterReceiver(br);
        super.onDestroy();
    }
    
    @Override
    public final View onCreateView(final LayoutInflater li, final ViewGroup vg, final Bundle b)
    {
        layout = true;
        final ViewGroup ret = (ViewGroup)li.inflate(R.layout.editor, vg, false);
        PBMain.populateKeys((ViewGroup)ret.findViewById(R.id.keys), li, pa.edit, this);
        final AutoCompleteTextView actv = (AutoCompleteTextView)ret.findViewById(R.id.ch_nm);
        actv.setText(pa.edit.nm);
        actv.setOnFocusChangeListener(this);
        Button bt = (Button)ret.findViewById(R.id.ch_act);
        bt.setText(PBMain.action(pa.edit.act));
        bt.setOnClickListener(this);
        bt = (Button)ret.findViewById(R.id.ch_md);
        bt.setText(pb.mode(pa.edit.md));
        bt.setOnClickListener(this);
        bt = (Button)ret.findViewById(R.id.ch_au);
        bt.setText(pb.audio(pa.edit.au));
        bt.setOnClickListener(this);
        bt = (Button)ret.findViewById(R.id.ch_adv_cnv);
        if (pa.edit.isEz()) bt.setOnClickListener(this);
        else bt.setVisibility(View.GONE);
        CheckBox cb = (CheckBox)ret.findViewById(R.id.ch_ccl);
        cb.setChecked(pa.edit.ccl);
        cb.setOnCheckedChangeListener(this);
        cb.setTag(Boolean.TRUE);
        cb = (CheckBox)ret.findViewById(R.id.ch_tst);
        cb.setChecked(pa.edit.tst);
        cb.setOnCheckedChangeListener(this);
        final Spinner s = (Spinner)ret.findViewById(R.id.ch_rep_md);
        final String[] rep_items = PBMain.r.getStringArray(R.array.rep_items);
        s.setAdapter(new ArrayAdapter<String>(pb, android.R.layout.simple_list_item_1, rep_items));
        s.setSelection(pa.edit.rep == -1 ? 1 : 0);
        s.setOnItemSelectedListener(this);
        SeekBar sb = (SeekBar)ret.findViewById(R.id.ch_rep);
        if (pa.edit.rep == -1) sb.setVisibility(View.GONE);
        else sb.setProgress(pa.edit.rep);
        sb.setOnSeekBarChangeListener(this);
        sb.setTag(Boolean.TRUE);
        sb = (SeekBar)ret.findViewById(R.id.ch_vib);
        sb.setProgress(pa.edit.vib / 10);
        sb.setOnSeekBarChangeListener(this);
        layout = false;
        return ret;
    }
    
    public final void actionChanged()
    {
        ((Button)getView().findViewById(R.id.ch_act)).setText(PBMain.action(pa.edit.act));
        onChange(false);
    }
    
    @Override
    public final void onClick(final View v)
    {
        final boolean md = v.getId() == R.id.ch_md;
        if (v.getId() == R.id.ch_act)
            new ActionDialog(this, pa.edit.act).show();
        else if (md || v.getId() == R.id.ch_au)
        {
            final AlertDialog.Builder b = new AlertDialog.Builder(pb);
            b.setTitle(md ? R.string.diag_sel_md : R.string.diag_sel_au);
            final boolean[] arr = new boolean[3];
            for (int x = md ? pa.edit.md : pa.edit.au, i = 0; i < 3; ++i, x >>= 1)
                arr[i] = (x & 1) == 1;
            b.setPositiveButton(R.string.diag_ok, new DialogInterface.OnClickListener()
            {
                @Override
                public final void onClick(final DialogInterface di, final int pos)
                {
                    int x = 0;
                    for (int i = 2; i >= 0; --i)
                        x = (x << 1) | (arr[i] ? 1 : 0);
                    if (md)
                    {
                        pa.edit.md = x;
                        ((Button)v).setText(pb.mode(pa.edit.md));
                    }
                    else
                    {
                        pa.edit.au = x;
                        ((Button)v).setText(pb.audio(pa.edit.au));
                    }
                    onChange(false);
                }
            });
            b.setNegativeButton(R.string.diag_cancel, null);
            b.setMultiChoiceItems(md ? R.array.md_items : R.array.au_items, arr, new OnMultiChoiceClickListener()
            {
                @Override
                public final void onClick(final DialogInterface di, final int pos, final boolean f)
                {
                    arr[pos] = f;
                }
            });
            b.create().show();
        }
        else if (v.getId() == R.id.add_key) keyDialog(new Key(0, false, 0), 1);
        else if (v.getId() == R.id.ch_ez)
        {
            final AlertDialog.Builder b = new AlertDialog.Builder(pb);
            b.setSingleChoiceItems(R.array.ez_types, pa.edit.getEz(), new DialogInterface.OnClickListener()
            {
                @Override
                public final void onClick(final DialogInterface di, final int i)
                {
                    keyDialog(pa.edit.ks.getLast(), i + 2);
                    di.dismiss();
                }
            });
            b.setNegativeButton(R.string.diag_cancel, null);
            b.create().show();
        }
        else if (v.getId() == R.id.ch_adv_cnv)
        {
            pa.edit.setEz(-1, -1, -1);
            v.setVisibility(View.GONE);
            PBMain.populateKeys((ViewGroup)getView().findViewById(R.id.keys), LayoutInflater.from(pb), pa.edit, ChainEditFragment.this);
            onChange(true);
        }
        else if (v.getTag() != null) keyDialog((Key)v.getTag(), 0);
        if (v.getId() != R.id.ch_nm) onFocusChange(getView().findViewById(R.id.ch_nm), false);
    }
    
    private final void keyDialog(final Key k, final int type)
    {
        final AlertDialog.Builder b = new AlertDialog.Builder(pb);
        b.setView(R.layout.keyeditor);
        b.setPositiveButton(R.string.diag_ok, new DialogInterface.OnClickListener()
        {
            @Override
            public final void onClick(final DialogInterface di, final int i)
            {
                if (type > 1)
                {
                    pa.edit.setEz(type - 2, lk, ((SeekBar)d.findViewById(R.id.key_dl)).getProgress() * 50);
                }
                else
                {
                    k.code = lk;
                    k.dn = ((CheckBox)d.findViewById(R.id.key_dn)).isChecked();
                    k.dl = ((SeekBar)d.findViewById(R.id.key_dl)).getProgress() * 50;
                    if (type == 1) pa.edit.ks.add(k);
                }
                PBMain.populateKeys((ViewGroup)getView().findViewById(R.id.keys), LayoutInflater.from(pb), pa.edit, ChainEditFragment.this);
                onChange(false);
            }
        });
        b.setNegativeButton(R.string.diag_cancel, null);
        if (type == 0)
            b.setNeutralButton(R.string.diag_rmv, new DialogInterface.OnClickListener()
            {
                @Override
                public final void onClick(final DialogInterface di, final int i)
                {
                    pa.edit.ks.remove(k);
                    PBMain.populateKeys((ViewGroup)getView().findViewById(R.id.keys), LayoutInflater.from(pb), pa.edit, ChainEditFragment.this);
                    onChange(false);
                }
            });
        d = b.create();
        d.setOnShowListener(new OnShowListener()
        {
            @Override
            public final void onShow(final DialogInterface di)
            {
                d.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                ((EditText)d.findViewById(R.id.key)).setText(PBMain.key(lk = k.code));
                final CheckBox cb = (CheckBox)d.findViewById(R.id.key_dn);
                if (type > 1) cb.setVisibility(View.GONE);
                else cb.setChecked(k.dn);
                ((SeekBar)d.findViewById(R.id.key_dl)).setProgress(k.dl / 50);
                final ToggleButton tb = (ToggleButton)d.findViewById(R.id.tgl_itc);
                tb.setChecked(false);
                tb.setOnCheckedChangeListener(new OnCheckedChangeListener()
                {
                    @Override
                    public final void onCheckedChanged(final CompoundButton cb, final boolean f)
                    {
                        pb.requestIntercept(f ? 1 : 0);
                    }
                });
            }
        });
        d.setOnDismissListener(new OnDismissListener()
        {
            @Override
            public final void onDismiss(final DialogInterface di)
            {
                d = null;
                pb.requestIntercept(0);
            }
        });
        d.show();
    }
    
    public final void updateEdit()
    {
        if (pb == null) return;
        layout = true;
        final ViewGroup vg = (ViewGroup)getView();
        if (vg == null) return;
        ((AutoCompleteTextView)vg.findViewById(R.id.ch_nm)).setText(pa.edit.nm);
        PBMain.populateKeys((ViewGroup)vg.findViewById(R.id.keys), LayoutInflater.from(pb), pa.edit, ChainEditFragment.this);
        ((Button)vg.findViewById(R.id.ch_act)).setText(PBMain.action(pa.edit.act));
        ((Button)vg.findViewById(R.id.ch_md)).setText(pb.mode(pa.edit.md));
        ((Button)vg.findViewById(R.id.ch_au)).setText(pb.audio(pa.edit.au));
        ((CheckBox)vg.findViewById(R.id.ch_ccl)).setChecked(pa.edit.ccl);
        ((CheckBox)vg.findViewById(R.id.ch_tst)).setChecked(pa.edit.tst);
        ((Spinner)vg.findViewById(R.id.ch_rep_md)).setSelection(pa.edit.rep == -1 ? 1 : 0);
        final SeekBar sb = (SeekBar)vg.findViewById(R.id.ch_rep);
        sb.setProgress(pa.edit.rep == -1 ? 0 : pa.edit.rep);
        sb.setVisibility(pa.edit.rep == -1 ? View.GONE : View.VISIBLE);
        ((SeekBar)vg.findViewById(R.id.ch_vib)).setProgress(pa.edit.vib / 10);
        layout = false;
    }

    @Override
    public final void onFocusChange(final View v, final boolean f)
    {
        final TextView tv = (TextView)v;
        if (!f && !pa.edit.nm.equals(tv.getText().toString()))
        {
            final String nnm = tv.getText().toString();
            final ListIterator<Chain> li = pa.getIterator();
            while (li.hasNext())
            {
                final Chain lch = li.next();
                if (lch.nm.equals(nnm))
                {
                    tv.setText(pa.edit.nm);
                    Toast.makeText(getContext(), R.string.diag_nm_exs, Toast.LENGTH_LONG).show();
                    return;
                }
            }
            pa.edit.nm = nnm;
            onChange(true);
        }
    }

    @Override
    public final void onCheckedChanged(final CompoundButton cb, final boolean c)
    {
        if (cb.getId() == R.id.ch_ccl) pa.edit.ccl = c;
        else pa.edit.tst = c;
        onChange(false);
    }

    @Override
    public final void onProgressChanged(final SeekBar sb, final int v, final boolean u)
    {
        if (sb.getId() == R.id.ch_rep) pa.edit.rep = v;
        else pa.edit.vib = v * 10;
        onChange(false);
    }

    private final void onChange(final boolean big)
    {
        if (!layout) pa.chainChanged(big);
    }
    
    @Override
    public final void onStartTrackingTouch(final SeekBar seekBar) { }

    @Override
    public final void onStopTrackingTouch(final SeekBar sb) { }

    @Override
    public final void onItemSelected(final AdapterView<?> av, final View v, final int pos, final long id)
    {
        final int rep = pos == 0 ? 0 : -1;
        if (rep != pa.edit.rep)
        {
            getView().findViewById(R.id.ch_rep).setVisibility(pos == 0 ? View.VISIBLE : View.GONE);
            pa.edit.rep = rep;
            onChange(false);
        }
    }

    @Override
    public final void onNothingSelected(final AdapterView<?> av)
    {
        av.setSelection(0);
    }
}
