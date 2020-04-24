package info.atiar.PuzzleDetector;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import androidx.appcompat.app.AlertDialog;

import com.kaopiz.kprogresshud.KProgressHUD;

import java.io.FileNotFoundException;

public class BP {


    public static AlertDialog alert;
    public static AlertDialog showDialog(Context context, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(message)
                .setCancelable(true)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alert = builder.create();
        alert.show();
        return alert;
    }
}
