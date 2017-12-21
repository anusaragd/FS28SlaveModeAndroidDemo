package com.futronictech.fs28slavemodeandroiddemo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
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
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Set;

import static android.content.ContentValues.TAG;
import static com.futronictech.fs28slavemodeandroiddemo.MainActivity.MESSAGE_EXIT_PROGRAM;

/**
 * Created by MASTERS on 19/12/2560.
 */

public class home extends Activity {

    private static final boolean D = false;

    public static boolean mStop = true;
    public static boolean mConnected = false;
    public static int mStep = 0;
    public static int mCaptureType = 0;
    public static boolean mOpened = false;
    private String mConnectedDeviceName = null;
//    // Local Bluetooth adapter
//    private BluetoothAdapter mBluetoothAdapter = null;
//    private BluetoothDataService mBTService = null;

    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothDataService mBTService = null;

    public static byte[] mImageFP = new byte[153602];
    private static Bitmap mBitmapFP;
    public static byte[] mWsqImageFP;
    
    Button select ,  create , match , scan ;
    ImageView showpic;
    TextView mMessage;
//    private Handler mHandler;
    private final int SELECT_PHOTO = 1;



    private BluetoothAdapter BA;
    private Set<BluetoothDevice> pairedDevices;

    // Message types sent from the BluetoothDataService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int MESSAGE_SHOW_MSG = 6;
    public static final int MESSAGE_SHOW_IMAGE = 7;
    public static final int MESSAGE_SHOW_PROGRESSBAR = 8;
    public static final int MESSAGE_STOP = 9;
    public static final int MESSAGE_DATA_ERROR = 10;
    public static final int MESSAGE_EXIT_PROGRAM = 11;
    public static final int MESSAGE_ENABLE_BUTTONS = 12;
    public static final int MESSAGE_DISABLE_STOP = 13;
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_FILE_FORMAT = 3;
    // Capture image type
    public static final int CAPTURE_RAW = 1;
    public static final int CAPTURE_WSQ = 2;

    // Key names received from the BluetoothDataService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String SHOW_MESSAGE = "show_message";
    public static final String TOAST = "toast";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        BA = BluetoothAdapter.getDefaultAdapter();

//        showpic = (ImageView)findViewById(R.id.imagematch) ;
        showpic = (ImageView)findViewById(R.id.imagematch) ;
        mMessage = (TextView) findViewById(R.id.status_text);
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

                if (!BA.isEnabled()) {
                    Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(turnOn, 0);
                    Toast.makeText(getApplicationContext(), "Turned on", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Already on", Toast.LENGTH_LONG).show();
                }
//                mCaptureType = CAPTURE_RAW;
//                startCapture();

                Intent intent = new Intent(home.this,MainActivity.class);
                startActivity(intent);
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

//    @Override
//    public void onStart() {
//        super.onStart();
//        if(D) Log.e(TAG, "++ ON START ++");
//
//        // If BT is not on, request that it be enabled.
//        // setupChat() will then be called during onActivityResult
//        if (!mBluetoothAdapter.isEnabled()) {
//            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
//        }
//    }


    @Override
    public synchronized void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");

    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        if(D) Log.e(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
        if(D) Log.e(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mStop = true;
        if (mBTService != null) {
            mBTService.stop();
            mBTService = null;
        }
        if(D) Log.e(TAG, "--- ON DESTROY ---");
    }

    @Override
    public void onBackPressed() {
        //super.OnBackPressed();
        new AlertDialog.Builder(this)
                .setTitle("Exit")
                .setMessage("Do you want to exit this program?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //send message to exit
                        mHandler.obtainMessage(MESSAGE_EXIT_PROGRAM).sendToTarget();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .setCancelable(false)
                .show();
    }

    public void ExitProgram()
    {
        onDestroy();
        System.exit(0);
    }

    private void SaveImage()
    {
        Intent serverIntent = new Intent(this, SelectFileFormatActivity.class);
        startActivityForResult(serverIntent, REQUEST_FILE_FORMAT);
    }

    private void SaveImageByFileFormat(String fileFormat, String fileName)
    {
        if( fileFormat.compareTo("WSQ") == 0 )	//save wsq file
        {
            if( mWsqImageFP != null )
            {
                File file = new File(fileName);
                try {
                    FileOutputStream out = new FileOutputStream(file);
                    out.write(mWsqImageFP, 0, mWsqImageFP.length);	// save the wsq_size bytes data to file
                    out.close();
                    mMessage.setText("Image is saved as " + fileName);
                } catch (Exception e) {
                    mMessage.setText("Exception in saving file");
                }
            }
            else
                mMessage.setText("Invalid WSQ image!");
            return;
        }
        // 0 - save bitmap file
        File file = new File(fileName);
        try {
            FileOutputStream out = new FileOutputStream(file);
            MyBitmapFile fileBMP = new MyBitmapFile(320, 480, mImageFP);
            out.write(fileBMP.toBytes());
            out.close();
            mMessage.setText("Image is saved as " + fileName);
        } catch (Exception e) {
            mMessage.setText("Exception in saving file");
        }
    }

    // The Handler that gets information back from the BluetoothChatService
    @SuppressLint("HandlerLeak") private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothDataService.STATE_CONNECTED:
                            mMessage.setText("Connected ");
                            mMessage.append(mConnectedDeviceName);
                            mOpened = true;
                            scan.setEnabled(true);
//                            mButtonCaptureWSQ.setEnabled(true);
                            break;
                        case BluetoothDataService.STATE_CONNECTING:
                            mMessage.setText("Connecting...");
                            break;
                        case BluetoothDataService.STATE_LISTEN:
                        case BluetoothDataService.STATE_NONE:
                            mMessage.setText("Not connected.");
                            mOpened = false;
                            if( mConnected )
                            {
                                mConnected = false;
                                if( !mStop )
                                {
                                    if( mBTService != null )
                                    {
                                        mBTService.stop();
                                        mBTService = null;
                                    }
                                }
                            }
                            else
//                                mButtonOpen.setEnabled(true);
                            break;
                    }
                    break;
                case MESSAGE_SHOW_MSG:
                    String showMsg = (String) msg.obj;
                    mMessage.setText(showMsg);
                    break;
                case MESSAGE_SHOW_PROGRESSBAR:
//                    mButtonOpen.setEnabled(false);
//                    mButtonSave.setEnabled(false);
//                    mProgressbar1.setProgress(mStep);
                    break;
                case MESSAGE_SHOW_IMAGE:
//                    mProgressbar1.setProgress(0);
                    ShowBitmap();
//                    mButtonSave.setEnabled(true);
//                    mButtonOpen.setEnabled(true);
                    scan.setEnabled(true);
//                    mButtonCaptureWSQ.setEnabled(true);
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_STOP:
                    mMessage.setText("Cancelled by user.");
//                    mButtonSave.setEnabled(true);
//                    mButtonOpen.setEnabled(true);
                    scan.setEnabled(true);
//                    mButtonCaptureWSQ.setEnabled(true);
                    break;
                case MESSAGE_DATA_ERROR:
//                    mButtonOpen.setEnabled(true);
                    switch (msg.arg1) {
                        case FamBTComm.ERROR_TIMEOUT:
                            mStep = 0;
//                            mProgressbar1.setProgress(0);
                            mMessage.setText("Time out to receive data!");
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            if( mConnected )
                            {
                                mConnected = false;
                                if( !mStop )
                                {
                                    if( mBTService != null )
                                    {
                                        mBTService.stop();
                                        mBTService = null;
                                    }
                                    mStop = true;
//                                    mButtonOpen.setText("Open BT Comm");
                                }
                            }
                            break;
                    }
                    break;
                case MESSAGE_EXIT_PROGRAM:
                    ExitProgram();
                    break;
                case MESSAGE_ENABLE_BUTTONS:
                    scan.setEnabled(true);
//                    mButtonCaptureWSQ.setEnabled(true);
//                    mButtonStop.setEnabled(false);
                    break;
                case MESSAGE_DISABLE_STOP:
//                    mButtonOpen.setEnabled(false);
//                    mButtonStop.setEnabled(false);
                    break;
            }
        }
    };

    private void ShowBitmap()
    {
        int[] pixels = new int[153600];
        for( int i=0; i<153600; i++)
            pixels[i] = mImageFP[i];
        Bitmap emptyBmp = Bitmap.createBitmap(pixels, 320, 480, Bitmap.Config.RGB_565);

        int width, height;
        height = emptyBmp.getHeight();
        width = emptyBmp.getWidth();

        mBitmapFP = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(mBitmapFP);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(emptyBmp, 0, 0, paint);

        showpic.setImageBitmap(mBitmapFP);
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
