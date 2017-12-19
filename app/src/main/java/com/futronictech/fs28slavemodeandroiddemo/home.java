package com.futronictech.fs28slavemodeandroiddemo;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by MASTERS on 19/12/2560.
 */

public class home extends Activity {

    public static int mCaptureType = 0;
    // Capture image type
    public static final int CAPTURE_RAW = 1;
    public static final int CAPTURE_WSQ = 2;

    public static boolean mStop = true;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothDataService mBTService = null;

    public static byte[] mImageFP = new byte[153602];
    private static Bitmap mBitmapFP;
    public static byte[] mWsqImageFP;
    
    Button select ,  create , match , scan ;
    ImageView showpic;
    private Handler mHandler;
    private final int SELECT_PHOTO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

//        showpic = (ImageView)findViewById(R.id.imagematch) ;
        showpic = (ImageView)findViewById(R.id.imagematch) ;
//        showpic.getDrawable();


        select = (Button)findViewById(R.id.select_button);
        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scan.setEnabled(true);
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
//                startActivityForResult(Intent.createChooser(intent,"Select Picture "),222);
                startActivityForResult(intent, SELECT_PHOTO);

//                ShowBitmap();

            }
        });

        create = (Button)findViewById(R.id.create_button);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(home.this,MainActivity.class);
                startActivity(intent);
                
            }
        });


        match  = (Button)findViewById(R.id.match_button);
        match.setEnabled(false);
        match.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCaptureType = CAPTURE_RAW;
                match.setEnabled(false);
                startCapture();
            }
        });

        scan = (Button)findViewById(R.id.scan_button);
        scan.setEnabled(false);
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                match.setEnabled(true);
            }
        });
    }

    public void startCapture()
    {
        mStop = false;
        if (mBTService != null) {
            mBTService.startCapture();
        }
        else
        {
            mBTService = new BluetoothDataService(this, mHandler);
            mBTService.startCapture();
        }
    }

//    private void ShowBitmap()
//    {
//        int[] pixels = new int[153600];
//        for( int i=0; i<153600; i++)
//            pixels[i] = mImageFP[i];
//        Bitmap emptyBmp = Bitmap.createBitmap(pixels, 320, 480, Bitmap.Config.RGB_565);
//
//        int width, height;
//        height = emptyBmp.getHeight();
//        width = emptyBmp.getWidth();
//
//        mBitmapFP = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
//        Canvas c = new Canvas(mBitmapFP);
//        Paint paint = new Paint();
//        ColorMatrix cm = new ColorMatrix();
//        cm.setSaturation(0);
//        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
//        paint.setColorFilter(f);
//        c.drawBitmap(emptyBmp, 0, 0, paint);
//
//        showpic.setImageBitmap(mBitmapFP);
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode) {
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK) {
                    try {
                        final Uri imageUri = imageReturnedIntent.getData();
                        final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        showpic.setImageBitmap(selectedImage);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                }
        }
    }
}
