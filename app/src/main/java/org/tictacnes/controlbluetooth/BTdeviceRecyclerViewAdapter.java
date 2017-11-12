package org.tictacnes.controlbluetooth;

import android.bluetooth.BluetoothDevice;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.tictacnes.controlbluetooth.BTdevicesFragment.OnListFragmentInteractionListener;
import org.tictacnes.controlbluetooth.dummy.DummyContent.DummyItem;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class BTdeviceRecyclerViewAdapter extends RecyclerView.Adapter<BTdeviceRecyclerViewAdapter.ViewHolder>
        implements View.OnClickListener {

    // private final List<DummyItem> mValues;
    private final List<BluetoothDevice> mValues;
    // private final OnListFragmentInteractionListener mListener;
    private View.OnClickListener listener;

    public BTdeviceRecyclerViewAdapter(List<BluetoothDevice> items, OnListFragmentInteractionListener listener) {
        mValues = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_item, parent, false);
        view.setOnClickListener(this);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(mValues.get(position).getName());
        holder.mContentView.setText(mValues.get(position).getAddress());
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public void add(BluetoothDevice bluetoothDevice){
        this.mValues.add(bluetoothDevice);
    }

    public void setOnClickListener(View.OnClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onClick(View view) {
        if(listener != null)
            listener.onClick(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        // public DummyItem mItem;
        public BluetoothDevice mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.tvNameBT);
            mContentView = (TextView) view.findViewById(R.id.tvAddressBT);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
