package io.flutter.plugins.webviewflutter;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ValueCallback;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class newActivity extends Activity {
    private static ValueCallback<Uri[]> mUploadMessages;
    private static final int PICK_Image_FILE = 2;
    private static final int READ_EXTERNAL_STORAGE_Code = 1;

    public static void getfilePathCallback(ValueCallback<Uri[]> filePathCallback){
        mUploadMessages = filePathCallback;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        if(isStoragePermissionGranted()) openImageIntent();
    }
    
    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            {
                return true;
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        READ_EXTERNAL_STORAGE_Code);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult (int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case READ_EXTERNAL_STORAGE_Code:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    openImageIntent();
                else{
                    mUploadMessages.onReceiveValue(null);
                    finish();
                }
                break;
        }
    }

    private void openImageIntent() {
        // Camera.
        Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Filesystem.
        final Intent fileIntent = new Intent();
        fileIntent.setType("image/*");
        fileIntent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        fileIntent.addCategory(Intent.CATEGORY_OPENABLE);

        // Chooser of filesystem options.
        final Intent chooserIntent = Intent.createChooser(fileIntent, "Select Source");

        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Parcelable[]{openCameraIntent});
        startActivityForResult(chooserIntent, PICK_Image_FILE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if(requestCode == PICK_Image_FILE){
                if(data != null){
                    Uri uri = data.getData();
                    if(uri==null){
                        Bundle bundle = data.getExtras();
                        try {
                            Bitmap bitmap = (Bitmap) bundle.get("data");
                            uri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, null,null));
                          /*  ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                            String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Title", null);
                            Uri lUri = Uri.parse(path);*/
                            Uri[] results = new Uri[]{uri};
                            mUploadMessages.onReceiveValue(results);
                        }catch (Exception e){
                            mUploadMessages.onReceiveValue(null);
                        }
                    }
                    else{
                        Uri[] results = new Uri[]{uri};
                        mUploadMessages.onReceiveValue(results);
                    }

                }else{
                    mUploadMessages.onReceiveValue(null);
                }}
        } else mUploadMessages.onReceiveValue(null);
        finish();
    }
}