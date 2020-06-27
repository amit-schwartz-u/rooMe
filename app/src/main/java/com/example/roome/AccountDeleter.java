package com.example.roome;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.Button;

public class AccountDeleter {

    private Context _context;
    private Activity _activity;

    public AccountDeleter(Context context, Activity activity) {
        this._context = context;
        this._activity = activity;
    }

    public void showSignOutDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(_context);
        View v = _activity.getLayoutInflater().inflate(R.layout.sing_out_dialog, null);
        dialogBuilder.setView(v);
        final AlertDialog alertdialog = dialogBuilder.create();
        onClickDialog(v, alertdialog);
        if (alertdialog.getWindow() != null) {
            alertdialog.getWindow().setBackgroundDrawable
                    (new ColorDrawable(Color.TRANSPARENT));
        }
        alertdialog.show();
    }

    private void onClickDialog(View view, final AlertDialog alertdialog) {
        Button signOutBtn = view.findViewById(R.id.signOutBtnDialog);
        Button cancelBtn = view.findViewById(R.id.cancelBtnDialog);
        signOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String aptUid = MyPreferences.getUserUid(_context);
                FirebaseMediate.deleteAptUserFromApp(aptUid);
                for (String roommateId :
                        FirebaseMediate.getAllRoommateSearcherKeys())
                {
                    FirebaseMediate.addAptIdToRmtPrefList(ChoosingActivity.DELETE_USERS,roommateId,aptUid);
                }
                MyPreferences.resetData(_context);
                Intent i = new Intent(_context,MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                _context.startActivity(i);
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertdialog.cancel();
            }
        });
    }
}
