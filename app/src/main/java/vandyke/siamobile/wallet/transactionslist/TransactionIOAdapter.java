/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.wallet.transactionslist;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import vandyke.siamobile.R;
import vandyke.siamobile.api.Wallet;
import vandyke.siamobile.backend.wallet.transaction.TransactionIOBase;
import vandyke.siamobile.backend.wallet.transaction.TransactionInput;
import vandyke.siamobile.backend.wallet.transaction.TransactionOutput;

import java.util.ArrayList;

public class TransactionIOAdapter extends ArrayAdapter {

    private final int layoutResourceId;
    private final Context context;
    private ArrayList<TransactionIOBase> data;

    public TransactionIOAdapter(Context context, int layoutResourceId, ArrayList<TransactionIOBase> data) {
        super(context, layoutResourceId);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.data = data;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        TransactionIOAdapter.Holder holder;
        View row = convertView;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new TransactionIOAdapter.Holder();
            holder.idLabel = (TextView)row.findViewById(R.id.txParentIdLabel);
            holder.id = (TextView)row.findViewById(R.id.txDetailsId);
            holder.address = (TextView)row.findViewById(R.id.txDetailsAddress);
            holder.wallet = (TextView)row.findViewById(R.id.txDetailsWallet);
            holder.type = (TextView)row.findViewById(R.id.txDetailsType);
            holder.amount = (TextView)row.findViewById(R.id.txDetailsAmount);
            holder.maturityHeightLabel = (TextView)row.findViewById(R.id.txDetailsMaturityLabel);
            holder.maturityHeight = (TextView)row.findViewById(R.id.txDetailsMaturityHeight);

            row.setTag(holder);
        } else {
            holder = (TransactionIOAdapter.Holder)row.getTag();
        }

        TransactionIOBase txBase = data.get(position);
        if (txBase instanceof TransactionInput) {
            TransactionInput tx = (TransactionInput)txBase;
            holder.id.setText(tx.getParentId());
            holder.idLabel.setText("Parent ID:");
            holder.maturityHeight.setVisibility(View.GONE);
            holder.maturityHeightLabel.setVisibility(View.GONE);
        } else {
            TransactionOutput tx = (TransactionOutput)txBase;
            holder.id.setText(tx.getId());
            holder.idLabel.setText("ID:");
            holder.maturityHeight.setText(Integer.toString(tx.getMaturityHeight()));
            holder.maturityHeight.setVisibility(View.VISIBLE);
            holder.maturityHeightLabel.setVisibility(View.VISIBLE);
        }

        holder.address.setText(txBase.getRelatedAddress());
        holder.wallet.setText(txBase.isWalletAddress() ? "yes" : "no");
        holder.type.setText(txBase.getFundType());
        holder.amount.setText(Wallet.hastingsToSC(txBase.getValue()).toPlainString());

        return row;
    }

    public int getCount() {
        return data.size();
    }

    private static class Holder {
        private TextView idLabel;
        private TextView id;
        private TextView address;
        private TextView wallet;
        private TextView type;
        private TextView amount;
        private TextView maturityHeightLabel;
        private TextView maturityHeight;
    }
}
