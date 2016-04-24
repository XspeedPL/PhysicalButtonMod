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
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar.OnSeekBarChangeListener;
import xeed.library.ui.BaseSettings;
import xeed.library.ui.SimpleDialog;
import xeed.xposed.cbppmod.*;

public final class ChainEditFragment extends Fragment implements OnFocusChangeListener, OnCheckedChangeListener, OnClickListener, OnSeekBarChangeListener
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
        try { pb.unregisterReceiver(br); }
        catch (final Exception ex) { }
        super.onDestroy();
    }
    
    @Override
    public final View onCreateView(final LayoutInflater li, final ViewGroup vg, final Bundle b)
    {
        layout = true;
        final ViewGroup ret = (ViewGroup)li.inflate(R.layout.editor, vg, false);
        PBMain.populateKeys((ViewGroup)ret.findViewById(R.id.keys), li, pa.edit, this);
        ret.findViewById(R.id.ch_nm).setOnFocusChangeListener(this);
        Button bt = (Button)ret.findViewById(R.id.ch_act);
        bt.setText(PBMain.action(pa.edit.act));
        bt.setOnClickListener(this);
        bt = (Button)ret.findViewById(R.id.ch_md);
        bt.setText(pb.btnTxt(R.array.md_items, pa.edit.md));
        bt.setOnClickListener(this);
        bt.setTag(Boolean.TRUE);
        bt = (Button)ret.findViewById(R.id.ch_au);
        bt.setText(pb.btnTxt(R.array.au_items, pa.edit.au));
        bt.setOnClickListener(this);
        bt.setTag(Boolean.TRUE);
        bt = (Button)ret.findViewById(R.id.ch_pl);
        bt.setText(pb.music(pa.edit.pl));
        bt.setOnClickListener(this);
        bt.setTag(Boolean.TRUE);
        bt = (Button)ret.findViewById(R.id.ch_tel);
        bt.setText(pb.btnTxt(R.array.tel_items, pa.edit.tel));
        bt.setOnClickListener(this);
        bt.setTag(Boolean.TRUE);
        bt = (Button)ret.findViewById(R.id.ch_not);
        bt.setText(pb.notify(pa.edit.not));
        bt.setOnClickListener(this);
        bt.setTag(Boolean.TRUE);
        ((Button)ret.findViewById(R.id.ch_adv_cnv)).setOnClickListener(this);
        ((CheckBox)ret.findViewById(R.id.ch_ccl)).setOnCheckedChangeListener(this);
        ((CheckBox)ret.findViewById(R.id.ch_rep_md)).setOnCheckedChangeListener(this);
        ((CheckBox)ret.findViewById(R.id.ch_scr)).setOnCheckedChangeListener(this);
        ((SeekBar)ret.findViewById(R.id.ch_rep)).setOnSeekBarChangeListener(this);
        ((SeekBar)ret.findViewById(R.id.ch_vib)).setOnSeekBarChangeListener(this);
        layout = false;
        return ret;
    }
    
    public final void actionChanged()
    {
        ((Button)getView().findViewById(R.id.ch_act)).setText(PBMain.action(pa.edit.act));
        onChange(false);
    }

    private final void showMultiDialog(final Button src, final int title, final int arr, int val)
    {
        final AlertDialog.Builder b = new AlertDialog.Builder(pb, BaseSettings.getDiagTh());
        b.setTitle(title);
        final boolean[] data = new boolean[3];
        for (int i = 0; i < 3; ++i)
        {
            data[i] = (val & 1) == 1;
            val >>= 1;
        }
        b.setPositiveButton(R.string.diag_ok, new DialogInterface.OnClickListener()
        {
            @Override
            public final void onClick(final DialogInterface di, final int pos)
            {
                int x = 0;
                for (int i = 2; i >= 0; --i)
                    x = (x << 1) | (data[i] ? 1 : 0);
                if (src.getId() == R.id.ch_md)
                {
                    pa.edit.md = x;
                    src.setText(pb.btnTxt(R.array.md_items, pa.edit.md));
                }
                else if (src.getId() == R.id.ch_au)
                {
                    pa.edit.au = x;
                    src.setText(pb.btnTxt(R.array.au_items, pa.edit.au));
                }
                else if (src.getId() == R.id.ch_pl)
                {
                    pa.edit.pl = x;
                    src.setText(pb.music(pa.edit.pl));
                }
                else if (src.getId() == R.id.ch_tel)
                {
                    pa.edit.tel = x;
                    src.setText(pb.btnTxt(R.array.tel_items, pa.edit.tel));
                }
                else if (src.getId() == R.id.ch_not)
                {
                    pa.edit.not = x;
                    src.setText(pb.notify(pa.edit.not));
                }
                onChange(false);
            }
        });
        b.setNegativeButton(R.string.diag_cancel, null);
        b.setMultiChoiceItems(arr, data, new OnMultiChoiceClickListener()
        {
            @Override
            public final void onClick(final DialogInterface di, final int pos, final boolean f)
            {
                data[pos] = f;
            }
        });
        b.create().show();
    }
    
    @Override
    public final void onClick(final View v)
    {
        if (v.getId() == R.id.ch_act)
            new ActionDialog(this, pa.edit.act, BaseSettings.getDiagTh()).show();
        else if (v.getTag() == Boolean.TRUE)
        {
            final int title, arr, val;
            if (v.getId() == R.id.ch_md)
            {
                title = R.string.diag_md;
                arr = R.array.md_items;
                val = pa.edit.md;
            }
            else if (v.getId() == R.id.ch_au)
            {
                title = R.string.diag_au;
                arr = R.array.au_items;
                val = pa.edit.au;
            }
            else if (v.getId() == R.id.ch_pl)
            {
                title = R.string.diag_pl;
                arr = R.array.pl_items;
                val = pa.edit.pl;
            }
            else if (v.getId() == R.id.ch_tel)
            {
                title = R.string.diag_tel;
                arr = R.array.tel_items;
                val = pa.edit.tel;
            }
            else
            {
                title = R.string.diag_not;
                arr = R.array.not_items;
                val = pa.edit.not;
            }
            showMultiDialog((Button)v, title, arr, val);
        }
        else if (v.getId() == R.id.add_key) keyDialog(new Key(0, false, 0), 1);
        else if (v.getId() == R.id.ch_ez)
        {
            final AlertDialog.Builder b = new AlertDialog.Builder(pb, BaseSettings.getDiagTh());
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
        final AlertDialog.Builder b = new AlertDialog.Builder(pb, BaseSettings.getDiagTh());
        b.setView(R.layout.keyeditor);
        b.setPositiveButton(R.string.diag_ok, new DialogInterface.OnClickListener()
        {
            @Override
            public final void onClick(final DialogInterface di, final int i)
            {
                final int dl = ((SeekBar)d.findViewById(R.id.key_dl)).getProgress() * 50;
                if (type < 2 || PBMain.isKeySafe(lk)) updateKey(dl, k, type);
                else SimpleDialog.create(pb, BaseSettings.getDiagTh(), R.string.diag_yes, R.string.diag_no, R.string.diag_cnf_t, R.string.diag_cnf_dng, new DialogInterface.OnClickListener()
                {
                    @Override
                    public final void onClick(final DialogInterface di, final int i)
                    {
                        updateKey(dl, k, type);
                    }
                }).show();
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
                        pb.requestIntercept(f);
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
                pb.requestIntercept(false);
            }
        });
        d.show();
    }
    
    private final void updateKey(final int dl, final Key k, final int type)
    {
        if (type > 1)
        {
            pa.edit.setEz(type - 2, lk, dl);
        }
        else
        {
            k.code = lk;
            k.dn = ((CheckBox)d.findViewById(R.id.key_dn)).isChecked();
            k.dl = dl;
            if (type == 1) pa.edit.ks.add(k);
        }
        PBMain.populateKeys((ViewGroup)getView().findViewById(R.id.keys), LayoutInflater.from(pb), pa.edit, ChainEditFragment.this);
        onChange(false);
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
        ((Button)vg.findViewById(R.id.ch_md)).setText(pb.btnTxt(R.array.md_items, pa.edit.md));
        ((Button)vg.findViewById(R.id.ch_au)).setText(pb.btnTxt(R.array.au_items, pa.edit.au));
        ((Button)vg.findViewById(R.id.ch_pl)).setText(pb.music(pa.edit.pl));
        ((Button)vg.findViewById(R.id.ch_tel)).setText(pb.btnTxt(R.array.tel_items, pa.edit.tel));
        ((Button)vg.findViewById(R.id.ch_not)).setText(pb.notify(pa.edit.not));
        ((Button)vg.findViewById(R.id.ch_adv_cnv)).setVisibility(pa.edit.isEz() ? View.VISIBLE : View.GONE);
        ((CheckBox)vg.findViewById(R.id.ch_ccl)).setChecked(pa.edit.ccl);
        ((CheckBox)vg.findViewById(R.id.ch_scr)).setChecked(pa.edit.scr);
        ((CheckBox)vg.findViewById(R.id.ch_rep_md)).setChecked(pa.edit.rep == -1);
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
        else if (cb.getId() == R.id.ch_scr) pa.edit.scr = c;
        else if (cb.getId() == R.id.ch_rep_md)
        {
            getView().findViewById(R.id.ch_rep).setVisibility(c ? View.GONE : View.VISIBLE);
            pa.edit.rep = c ? -1 : 0;
            onChange(false);
        }
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
}
