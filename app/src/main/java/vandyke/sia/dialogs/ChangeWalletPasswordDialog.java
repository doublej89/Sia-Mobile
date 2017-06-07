package vandyke.sia.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import org.json.JSONObject;
import vandyke.sia.R;
import vandyke.sia.SiaRequest;
import vandyke.sia.api.Wallet;

public class ChangeWalletPasswordDialog extends DialogFragment {

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_wallet_change_password, null);
        builder.setTitle("Change Wallet Password")
                .setView(view)
                .setPositiveButton("Change Password", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String newPassword = ((EditText)view.findViewById(R.id.newPassword)).getText().toString();
                        if (!newPassword.equals(((EditText)view.findViewById(R.id.confirmNewPassword)).getText().toString())) {
                            Toast.makeText(getContext(), "New passwords don't match", Toast.LENGTH_SHORT).show();
                            return; // TODO: make dialog not disappear in this case
                        }
                        Wallet.changePassword(((EditText) view.findViewById(R.id.currentPassword)).getText().toString(),
                                newPassword, new SiaRequest.VolleyCallback() {
                                    public void onSuccess(JSONObject response) {
                                        SiaRequest.genericSuccessToast(getContext());
                                    }
                                    public void onError(JSONObject error) {
                                        SiaRequest.checkIfIncorrectWalletPassword(getContext(), error);
                                        SiaRequest.checkIfWalletLocked(getContext(), error);
                                    }
                                });
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        return builder.create();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_wallet_change_password, null);
    }

    public static void createAndShow(FragmentManager fragmentManager) {
        new SendDialog().show(fragmentManager, "change password dialog");
    }
}
