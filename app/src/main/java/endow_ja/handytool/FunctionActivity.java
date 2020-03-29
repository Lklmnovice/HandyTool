package endow_ja.handytool;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.VectorDrawable;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.constraint.ConstraintLayout;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.content.res.AppCompatResources;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.BounceInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static android.text.InputType.TYPE_CLASS_NUMBER;
import static android.text.InputType.TYPE_CLASS_TEXT;


public class FunctionActivity extends BaseNFCActivity{
    private GridView gridView; //gridview in activity_functions
    private List<Map<String, Object>> dataList;

    //
    private int[] iconIds = {
            R.drawable.ic_formatta,
            R.drawable.ic_direzione,
            R.drawable.ic_chiamata,
            R.drawable.ic_sms,
            R.drawable.ic_foto,
            R.drawable.ic_web,
            R.drawable.ic_bluetooth,
            R.drawable.ic_wifi,
            R.drawable.ic_hotspot,
            R.drawable.ic_app,
            R.drawable.ic_flashlight,
            R.drawable.ic_screenshot,
            R.drawable.ic_silent,
            R.drawable.ic_home,
            R.drawable.ic_app
    };

    private int[] iconIds_activated = {
            R.drawable.ic_formatta_1,
            R.drawable.ic_direzione_1,
            R.drawable.ic_chiamata_1,
            R.drawable.ic_sms_1,
            R.drawable.ic_foto_1,
            R.drawable.ic_web_1,
            R.drawable.ic_bluetooth_1,
            R.drawable.ic_wifi_1,
            R.drawable.ic_hotspot_1,
            R.drawable.ic_app_1,
            R.drawable.ic_flashlight_1,
            R.drawable.ic_screenshot_1,
            R.drawable.ic_silent_1,
            R.drawable.ic_home_1,
            R.drawable.ic_app
    };

    private String[] functionsNames = {
            "Svuota",         // 0
            "Direzione",        // 1    sensitive data
            "Chiamata",         // 2    sensitive data
            "SMS",              // 3     sensitive data
            "Foto",             // 4
            "Web",              // 5
            "Bluetooth",        // 6
            "Wifi",             // 7
            "Hotspot",           // 8
            "App",              //9
            "Torcia",           //10
            "Screenshot",       //11
        "Mo. Silen.",           //12
            "Home",              //13
            "test"
    };



    private boolean canWrite = false;
    private boolean isViewActivated = false;

    private NdefMessage ndefMessage;
    private RelativeLayout relativeLayoutLoading;
    private RelativeLayout relativeLayoutInformation;
    private Button helper;
    private Button continua;
    private EditText numero;
    private EditText messaggio;
    private String phone;
    private ConstraintLayout hideLayout;
    private ImageView loadingBalls;
    private AnimationSet animation;
    private SimpleAdapter adapter;
    private Activity activity;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private static int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_functions);

        //Initialize view objects
        initializeViews();

        //load function icons
        //1. check the activated function
        Bundle extras = getIntent().getExtras();
        checkActivated(extras.getInt("activated"));
        prepareData();  //load icons into a list
        adapter = new SimpleAdapter(this, dataList, R.layout.funciton,
                new String[]{"image", "text"}, new int[]{R.id.function_image, R.id.function_name});
        gridView.setAdapter(adapter);


        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final byte[] data = new byte[]{(byte)position};
                switch (position) {
                    case 0: //formatta
                        Toast.makeText(getApplicationContext(), "Svuotare un bottone", Toast.LENGTH_SHORT).show();
                        ndefMessage = new NdefMessage(new NdefRecord[] { new NdefRecord(NdefRecord.TNF_EMPTY, null, null, null)});
                        setWritingView();
                        break;
                    case 1: //direzione
                        Toast.makeText(getApplicationContext(), "Direzione veloce", Toast.LENGTH_SHORT).show();
                        numero.setVisibility(View.GONE);
                        helper.setVisibility(View.GONE);
                        messaggio.setHint("città/via/numero civico");
                        setInformationFullView();
                        continua.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(messaggio.getText().toString().equals("")){
                                    messaggio.setError("Hai dimenticato l'indirizzo");
                                    return;
                                }
                                byte[] message =  messaggio.getText().toString() .getBytes();
                                byte[] data = new byte[message.length + 1];
                                data[0] = 1;
                                System.arraycopy(message, 0, data, 1, message.length);
//                                SharedPreferences pref = getApplicationContext().getSharedPreferences("UserPref_indirizzo", MODE_PRIVATE);
//                                SharedPreferences.Editor editor = pref.edit();
//
//                                String address = messaggio.getText().toString();
//                                Map<String, ?> map = pref.getAll();
//                                int size = map.size() + 1;
//                                if(!map.containsKey(address)) {
//                                    editor.putInt(address, size);
//                                    editor.commit();
//                                } else
//                                    size = (Integer) map.get(address);

//                                editor.putString("Indirizzo", messaggio.getText().toString());
//                                editor.commit();
//                                byte[] dataTemp = new byte[]{1, (byte)size};
                                ndefMessage = buildMessage(data);

                                disableInformationFullView();
                                setWritingView();
                            }
                        });
                        //ndefMessage = buildMessage(data);
                        setInformationFullView();
                        break;
                    case 2: //chiamata
                        Toast.makeText(getApplicationContext(), "Chiamata", Toast.LENGTH_SHORT).show();
                        if(requestPermission(Manifest.permission.CALL_PHONE, 1)) {
                            doChiamata();
                        }
                        break;
                    case 3: //sms
                        Toast.makeText(getApplicationContext(), "Messaggi SMS", Toast.LENGTH_SHORT).show();
                        //if(requestPermission(Manifest.permission.SEND_SMS, 2)) {
                            doSMS();
                        //}
                        break;
                    case 4: ; //foto
                        Toast.makeText(getApplicationContext(), "Foto veloce", Toast.LENGTH_SHORT).show();

                        if ((ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED)
                                ||
                                (ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) )
                            ActivityCompat.requestPermissions(activity,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 3);
                        else
                            savePhoto(4);



                        break;
                    case 5: //web
                        Toast.makeText(getApplicationContext(), "Pagina WEB rapida", Toast.LENGTH_SHORT).show();
                        numero.setVisibility(View.GONE);
                        helper.setVisibility(View.GONE);
                        messaggio.setHint("uri");
                        setInformationFullView();
                        continua.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String uriString = "http://" + messaggio.getText().toString();
                                try {
                                    URL u = new URL(uriString); // this would check for the protocol
                                    u.toURI();
                                } catch (Exception e) {
                                    Toast.makeText(getApplicationContext(), "Url non vaido", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                byte[] message =  uriString .getBytes();
                                byte[] data = new byte[message.length + 1];
                                data[0] = 5;
                                System.arraycopy(message, 0, data, 1, message.length);
                                ndefMessage = buildMessage(data);
                                setWritingView();
                                disableInformationFullView();


                            }
                        });
                        break;
                    case 6: //bluetooth
                        Toast.makeText(getApplicationContext(), "Attivare/Disativare bluetooth", Toast.LENGTH_SHORT).show();
                        setWritingView();
                        ndefMessage = buildMessage(data);
                        break;
                    case 7: //wifi
                        Toast.makeText(getApplicationContext(), "Attivare/Disativare wifi", Toast.LENGTH_SHORT).show();
                        setWritingView();
                        ndefMessage = buildMessage(data);
                        break;
                    case 8: //hotspot
                        Toast.makeText(getApplicationContext(), "Attivare/Disativare hotspot (solo ver.android < 8)", Toast.LENGTH_SHORT).show();
                        setWritingView();
                        ndefMessage = buildMessage(data);
                        break;
                    case 9: //app
                        Toast.makeText(getApplicationContext(), "App veloce", Toast.LENGTH_SHORT).show();
                        messaggio.setVisibility(View.GONE);
                        numero.setInputType(TYPE_CLASS_TEXT);
                        numero.setEnabled(false);
                        VectorDrawable bck = (VectorDrawable) AppCompatResources.getDrawable(
                                getApplicationContext(), R.drawable.ic_loadingtext_disabled);
                        numero.setTextColor(getResources().getColor(R.color.defaultColor));
                        numero.setBackground(bck);
                        numero.setText(" App");
                        helper.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getApplicationContext(), OpenApp.class);
                                intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                                startActivityForResult(intent, 2);
                            }
                        });

                        continua.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //convertire la stringa in un url
                                if (numero.getText().toString().equals("")) {
                                    numero.setError("Hai dimenticato il campo");
                                    return;
                                }
                                byte[] message = (numero.getText().toString()).getBytes();
                                byte[] data = new byte[message.length + 1];
                                data[0] = 9;
                                System.arraycopy(message, 0, data, 1, message.length);
                                ndefMessage = buildMessage(data);
                                setWritingView();
                                disableInformationFullView();
                                numero.setInputType(TYPE_CLASS_NUMBER);
                            }
                        });
                        setInformationFullView();

                        break;
                    case 10: //flashlight
                        Toast.makeText(getApplicationContext(), "Torcia", Toast.LENGTH_SHORT).show();
                        setWritingView();
                        ndefMessage = buildMessage(data);
                        break;
                    case 11: //screenshot
                        Toast.makeText(getApplicationContext(), "Screenshot", Toast.LENGTH_SHORT).show();
                        if(requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 4)) {
                            savePhoto(11);
                        }
                        break;


                    case 12: //musica
                        Toast.makeText(getApplicationContext(), "Modalità Silenziosa", Toast.LENGTH_SHORT).show();
                        NotificationManager notificationManager =
                                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                                && !notificationManager.isNotificationPolicyAccessGranted()) {

                            Intent intent = new Intent(
                                    Settings
                                            .ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                            startActivity(intent);
                        } else {
                            setWritingView();
                            ndefMessage = buildMessage(data);
                        }
                        break;
                    case 13: //home
                        Toast.makeText(getApplicationContext(), "Torna alla home", Toast.LENGTH_SHORT).show();
                        setWritingView();
                        ndefMessage = buildMessage(data);
                        break;

                    case 14:


                        Uri uri = Uri.parse("smsto:" + "+393314803166");
                        Intent i = new Intent(Intent.ACTION_SENDTO, uri);
                        i.setPackage("com.whatsapp");
                        startActivity(Intent.createChooser(i, ""));



                }


            }
        });

    }

    /***********************************************************************/
    /*************************  Initialization ****************************/
    /***********************************************************************/

    //load data into a datalist
    private void prepareData() {
        dataList = new ArrayList<>();
        for(int i=0;i<iconIds.length;i++){
            Map<String,Object> map =new HashMap<>();
            map.put("image",iconIds[i]);
            map.put("text",functionsNames[i]);
            dataList.add(map);
        }
    }

    //change the activated funciton's icon
    private void checkActivated(int num) {
        if (num < iconIds.length)
            iconIds[num] = iconIds_activated[num];
        else//
            iconIds[0] = iconIds_activated[0];
    }
    //initialize views
    private void initializeViews() {
        activity = this;
        gridView = (GridView)findViewById(R.id.activity_functions_gridview);
        relativeLayoutLoading = findViewById(R.id.activity_write);
        relativeLayoutInformation = findViewById(R.id.activity_information);
        hideLayout = findViewById(R.id.activity_hide);
        loadingBalls = (ImageView)findViewById(R.id.loadingballs);
        helper = findViewById(R.id.activity_information_helper);
        continua = findViewById(R.id.activity_information_continua);
        numero = findViewById(R.id.activity_information_numero);
        messaggio = findViewById(R.id.activity_information_messaggio);

        pref = getApplicationContext().getSharedPreferences("UserPref", MODE_PRIVATE);
        editor = pref.edit();

        //hide information/writing views
        hideLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disableWritingView();
                disableInformationFullView();
            }
        });

        //share the application
        ImageView share = (ImageView) findViewById(R.id.share);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT,
                        "Hey check out my app at: https://play.google.com/store/apps/details?id=endow_ja.handytool");
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
            }
        });

        //credit pages
        ImageView extra = (ImageView)findViewById(R.id.extra);
        extra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ExtraActivity.class));
            }
        });

        //animation
        animation = new AnimationSet(true);
        animation.addAnimation(new AlphaAnimation(0.8f, 1.0f));
        animation.addAnimation(new TranslateAnimation(
                Animation.RELATIVE_TO_SELF,0, Animation.RELATIVE_TO_SELF,0,
                Animation.RELATIVE_TO_SELF,0.1f, Animation.RELATIVE_TO_SELF,0));
        animation.setDuration(500);
        animation.setInterpolator(new BounceInterpolator());
    }

    /***********************************************************************/
    /*************************  Initialization ****************************/
    /***********************************************************************/

    private void savePhoto(int functionCode) {
        byte[] data = new byte[]{(byte)functionCode};
        ndefMessage = buildMessage(data);

        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                "HandyTool");
        if(!storageDir.exists()) {
            storageDir.mkdir();
        }
        setWritingView();
    }

    private void doSMS() {
        numero.setHint(" numero di telefono");
        messaggio.setHint(" messaggio");
        helper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                startActivityForResult(intent, 1);
            }
        });

        continua.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messaggio.setHint("messaggio");
                //convertire la stringa in un url
                if(numero.getText().toString().equals("")){
                    numero.setError("Hai dimenticato il numero");
                    return;
                }
                String numeroStr = numero.getText().toString();
                Map<String, ?> temp = pref.getAll();
                String key;
                if(!temp.containsValue(numeroStr)){
                    counter++;
                    key = "" + counter;
                    editor.putString(key, numeroStr);
                    editor.commit();
                } else
                    key = getKeyByValue(temp, numeroStr);

                key +=  "/" + messaggio.getText().toString();
                byte[] dataBytes = key.getBytes();
                byte[] data = new byte[dataBytes.length + 1];
                data[0] = 3;
                System.arraycopy(dataBytes, 0, data, 1, dataBytes.length);

                ndefMessage = buildMessage(data);
                disableInformationFullView();
                setWritingView();

            }
        });
        setInformationFullView();
    }
    private void doChiamata() {
        messaggio.setVisibility(View.GONE);
        numero.setHint("numero di telefono");
        helper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                startActivityForResult(intent, 1);
            }
        });

        continua.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //convertire la stringa in un url
                if (numero.getText().toString().equals("")) {
                    numero.setError("Hai dimenticato il numero");
                    return;
                }
                String numeroStr = numero.getText().toString();
                Map<String, ?> temp = pref.getAll();
                String key;
                if(!temp.containsValue(numeroStr)){
                    counter++;
                    key = "" + counter;
                    editor.putString(key, numeroStr);
                    editor.commit();
                } else
                    key = getKeyByValue(temp, numeroStr);

                byte[] message = key.getBytes();
                byte[] data = new byte[message.length + 1];
                data[0] = 2;
                System.arraycopy(message, 0, data, 1, message.length);

                ndefMessage = buildMessage(data);
                disableInformationFullView();
                setWritingView();
                messaggio.setVisibility(View.VISIBLE);
            }
        });
        setInformationFullView();
    }

    @Override
    public void onBackPressed() {
        if(isViewActivated){
            disableInformationFullView();
            disableWritingView();
        } else {
            this.finish();
        }
    }


    public void onNewIntent(Intent intent) {
        if(canWrite) {
            if (IntentHandler.feedback) {
                Vibrator myVib = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
                myVib.vibrate(100);
            }
            detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            writeTag(ndefMessage, detectedTag);

            canWrite = false;
            relativeLayoutLoading.setVisibility(View.GONE);

            startActivity(new Intent(this, EntryActivity.class));
            Toast.makeText(getApplicationContext(), "Il tuo handytool è pronto", Toast.LENGTH_SHORT).show();
            finish();
        }
    }




    //request permissions
    private boolean requestPermission(String permission, int request_code) {
        if (ContextCompat.checkSelfPermission(this,
                permission)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, request_code);
            return false;
        }
        return true;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == 1) && (resultCode == RESULT_OK)) {
            Cursor cursor = null;
            try {
                Uri uri = data.getData();
                cursor = getContentResolver().query(uri, new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER }, null, null, null);
                if (cursor != null && cursor.moveToNext()) {
                    phone = cursor.getString(0);
                    numero.setText(phone);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if ((requestCode == 2) && (resultCode == RESULT_OK)) {
            try {
                String str  = data.getData().toString();
                if(isViewActivated) {
                    numero.setText(str);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }



    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    doChiamata();

                return;
//            case 2:
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
//                    doSMS();
            case 3:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    savePhoto(4);
                return;
            case 4:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        savePhoto(11);
                }
                return;


        }
    }

    //change view

    private void setInformationFullView() {
        relativeLayoutInformation.setVisibility(View.VISIBLE);
        hideLayout.setVisibility(View.VISIBLE);
        isViewActivated =true;
        relativeLayoutInformation.startAnimation(animation);
    }

    private void disableInformationFullView() {
        relativeLayoutInformation.setVisibility(View.GONE);
        hideLayout.setVisibility(View.GONE);
        helper.setVisibility(View.VISIBLE);
        numero.setVisibility(View.VISIBLE);
        messaggio.setVisibility(View.VISIBLE);
        numero.setEnabled(true);
        VectorDrawable bck = (VectorDrawable) AppCompatResources.getDrawable(
                getApplicationContext(), R.drawable.ic_loadingtext);
        numero.setTextColor(getResources().getColor(R.color.colorAccent));
        numero.setBackground(bck);
        numero.setText("");

        isViewActivated = false;
    }


    private void setWritingView() {
        relativeLayoutLoading.setVisibility(View.VISIBLE);
        hideLayout.setVisibility(View.VISIBLE);

        final AnimatedVectorDrawableCompat animatedVectorDrawableCompat = AnimatedVectorDrawableCompat.create(this, R.drawable.avd_bouncingballs);
        loadingBalls.setImageDrawable(animatedVectorDrawableCompat);
        animatedVectorDrawableCompat.start();
        canWrite = true;
        isViewActivated = true;


        relativeLayoutLoading.startAnimation(animation);
    }

    private void disableWritingView() {
        relativeLayoutLoading.setVisibility(View.GONE);
        hideLayout.setVisibility(View.GONE);
        isViewActivated = false;
        canWrite = false;
    }

    public  <T, E> T getKeyByValue(Map<T, ?> map, E value) {
        for (Map.Entry<T, ?> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }
//    private String checkNumber(String number) {
////        String temp = number.replaceAll("\\s","");
////        if(temp.charAt(0) != '+')
////            temp = "+39" + temp;
////        return temp;
////    }
}
