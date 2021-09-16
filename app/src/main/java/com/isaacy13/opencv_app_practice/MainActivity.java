package com.isaacy13.opencv_app_practice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    public static final int PICK_IMAGE = 1;
    public static final String TESS_DATA = "/tessdata";
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/Tess";
    public TessBaseAPI baseAPI = new TessBaseAPI();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

        // create button
        Button openGalleryButton = findViewById(R.id.openGalleryButton);
        // create listener for button
        openGalleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });
    }

    private void openGallery() { // opens gallery & sends request code
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    @Override // waits for request code & performs actions
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE) {
            Uri imageURI = data.getData();
            Bitmap bitmapIMG = null;
            try {
                bitmapIMG = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageURI);
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println(Environment.getExternalStorageDirectory().getAbsolutePath());

            prepareTessData();

            baseAPI.setImage(bitmapIMG);
            String resultText = baseAPI.getUTF8Text();

            TextView resultsBox = findViewById(R.id.textResult);
            resultsBox.setText(resultText);
        }
    }

    private void prepareTessData(){
        boolean fileExistFlag = false;
        AssetManager assetManager = getAssets();

        String dstPathDir = "/tesseract/tessdata/";

        String srcFile = "eng.traineddata";
        InputStream inFile = null;

        dstPathDir = getFilesDir() + dstPathDir;
        String dstInitPathDir = getFilesDir() + "/tesseract";
        String dstPathFile = dstPathDir + srcFile;
        FileOutputStream outFile = null;

        try {
            inFile = assetManager.open(srcFile);

            File f = new File(dstPathDir);

            if (!f.exists()) {
                if (!f.mkdirs()) {
                    Toast.makeText(this, srcFile + " can't be created.", Toast.LENGTH_SHORT).show();
                }
                outFile = new FileOutputStream(new File(dstPathFile));
            } else {
                fileExistFlag = true;
            }

        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());

        } finally {

            if (fileExistFlag) {
                try {
                    if (inFile != null) inFile.close();
                    baseAPI.init(dstInitPathDir, "eng");
                    return;

                } catch (Exception ex) {
                    Log.e(TAG, ex.getMessage());
                }
            }

            if (inFile != null && outFile != null) {
                try {
                    //copy file
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = inFile.read(buf)) != -1) {
                        outFile.write(buf, 0, len);
                    }
                    inFile.close();
                    outFile.close();
                    baseAPI.init(dstInitPathDir, "eng");
                } catch (Exception ex) {
                    Log.e(TAG, ex.getMessage());
                }
            } else {
                Toast.makeText(this, srcFile + " can't be read.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}