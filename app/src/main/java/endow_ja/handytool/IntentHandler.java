package endow_ja.handytool;

import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import endow_ja.handytool.Helper.ApManager;
import endow_ja.handytool.Helper.CmManager;
import endow_ja.handytool.Helper.screenshot.ScreenShotActivity;

public class IntentHandler extends AppCompatActivity {
    private Context context;    //context == getApplicationContext()
    static public boolean feedback = true;
    

    private SharedPreferences pref;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (feedback) {
            Vibrator myVib = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
            myVib.vibrate(100);
        }
        pref = getApplicationContext().getSharedPreferences("UserPref", MODE_PRIVATE);
        context = this.getApplicationContext();
        CmManager.unregisterFlashlightState(context);
        //leggere il messaggio

        Intent intent = getIntent();
        Parcelable[] rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

        //il primo record corrisponde alla funzione da chiamare --> functionCode
        byte[] inf = ((NdefMessage) rawMessages[0]).getRecords()[0].getPayload();
        int functionCode = inf[0];

        //Mettete le vostre funzioni da chiamare nel loro "case", per il numerino di ogni funzione, consultate il doc nel gruppo
        switch (functionCode) {
            case 0:
                break;
            case 1:
                getDirection(inf);
                break;
            case 2:
                handleCall(inf);
                break;
            case 3:
                sendSMS(inf);
                break;
            case 4:
                takePicture();
                break;
            case 5:
                openUri(inf);
                break;
            case 6:
                handleBluetooth();
                break;
            case 7:
                handleWifi();
                break;
            case 8:
                handleHotSpot();
                break;
            case 9: //app
                handleApp(inf);
                break;
            case 10:
                handleFlashLight();
                break;
            case 11: //screenshot
                handleScreenshot();
                break;
            case 12:
                handleSilentMode();
            case 13:
                handleHomeButton();
                break;
            default:
                break;
        }
        finish();   //termina l'activity
    }

    private void handleSilentMode() {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        AudioManager audioManager = (AudioManager) getBaseContext().getSystemService(Context.AUDIO_SERVICE);

        if (audioManager.getRingerMode() == 0)
            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        else if (audioManager.getRingerMode() == 2)
            audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
    }


    //1 get direction
    private void getDirection(byte[] data) {
        String dataString = new String(Arrays.copyOfRange(data, 1, data.length));
//        String dataString = pref.getString("Indirizzo", "milano");
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + Uri.encode(dataString));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }
    //02 call
    private void handleCall(byte[] data) {
        String code = new String(Arrays.copyOfRange(data, 1, data.length));
        String contact = pref.getString(code, "800447788");
        startActivity(new Intent(Intent.ACTION_CALL, Uri.fromParts("tel", contact, null)));
    }

    //03 send a sms
    private void sendSMS(byte[] data) {
        String dataString = new String(Arrays.copyOfRange(data, 1, data.length));
        int splitter = dataString.indexOf('/');
        String contactCode = dataString.substring(0, splitter);
        String message = dataString.substring(splitter + 1, dataString.length());
//        String contact = pref.getString("numeroSMS", "800447788");
        String contact = pref.getString(contactCode, "null");
        try {
//            SmsManager smsManager = SmsManager.getDefault();
//            smsManager.sendTextMessage(contact, null, message, null, null);

            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + contact));
            intent.putExtra("sms_body", message);
            startActivity(intent);
            Toast.makeText(getApplicationContext(), "Message Sent",
                    Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), "Require Send_SMS permission",
                    Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }

    //04 take a picture
    public void takePicture() {
    startActivity(new Intent(this, CameraActivity.class));

    }

    //05 open an uri
    private void openUri(byte[] data) {
        String dataString = new String(Arrays.copyOfRange(data, 1, data.length));
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(dataString));
        startActivity(browserIntent);
    }



    //06 open bluetooth
    private void handleBluetooth() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null)
            Toast.makeText(getApplicationContext(), "Device does not support Bluetooth", Toast.LENGTH_SHORT).show();
        else if (!mBluetoothAdapter.isEnabled()) {
            BluetoothAdapter.getDefaultAdapter().enable();
            Toast.makeText(getApplicationContext(), "Bluetooth Enabled", Toast.LENGTH_SHORT).show();
        } else {
            BluetoothAdapter.getDefaultAdapter().disable();
            Toast.makeText(getApplicationContext(), "Bluetooth Disabled", Toast.LENGTH_SHORT).show();
        }
    }
    //07 open wifi
    private void handleWifi() {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(!wifiManager.isWifiEnabled());
    }

    //08 hotspot
    private void handleHotSpot() {
        ApManager.configApState(this.context, !ApManager.isApOn(this.context));
    }

    //9 open an application
    private void handleApp(byte[] data) {
        String dataString = new String(Arrays.copyOfRange(data, 1, data.length));
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(dataString);
        if (launchIntent != null) {
            startActivity(launchIntent);//null pointer check in case package name was not found
        }
    }

    //10 open flashlight
    private void handleFlashLight() {
        CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            String backCamera = cameraManager.getCameraIdList()[0];
            CmManager.registerFlashlightState(context);
            cameraManager.setTorchMode(backCamera,!CmManager.isFlashlightOn);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    //11. handleScreenshot()
    private void handleScreenshot() {
        startActivity(new Intent(this, ScreenShotActivity.class));
    }

    //utility functions
//    private File createImage() {
//        Date now = new Date();
//        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);
//        File path = Environment.getExternalStoragePublicDirectory(
//                Environment.DIRECTORY_PICTURES);
//        File file = new File(path, "endowja" + now +"jpg");
//        file.mkdirs();
//        return  file;
//    }
    //13
    private  void handleHomeButton() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }
    private File createImage() throws IOException {
        // Create an image file name
        Log.d("handytool", "createimage: begin");
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                "HandyTool");
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(image);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);

        return image;
    }

}

