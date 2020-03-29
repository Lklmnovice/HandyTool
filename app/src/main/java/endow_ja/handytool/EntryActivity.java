package endow_ja.handytool;

import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.graphics.drawable.Animatable2Compat;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.content.res.AppCompatResources;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class EntryActivity extends BaseNFCActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);

        //start animation -- moving hand
        ImageView hand = (ImageView) findViewById(R.id.activity_entry_hand);
        final AnimatedVectorDrawableCompat animatedVectorDrawableCompat;
        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.M)   //api 23 doesn't support gradient in animated-vector
            animatedVectorDrawableCompat = AnimatedVectorDrawableCompat.create(this, R.drawable.avd_hand);
        else
            animatedVectorDrawableCompat = AnimatedVectorDrawableCompat.create(this, R.drawable.avd_hand_23);
        hand.setImageDrawable(animatedVectorDrawableCompat);
        ((Animatable)animatedVectorDrawableCompat).start();

        //set looper --> loop animation
        final Handler mainHandler = new Handler(Looper.getMainLooper());
        animatedVectorDrawableCompat.registerAnimationCallback(new Animatable2Compat.AnimationCallback() {
            @Override
            public void onAnimationEnd(Drawable drawable) {
                mainHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ((Animatable) animatedVectorDrawableCompat).start();
                    }
                }, 3000);
            }
        });

        int[] inf = new int[]{1};
        Intent mIntent = new Intent(this, endow_ja.handytool.FunctionActivity.class);
        mIntent.putExtra("activated", (int)inf[0]);
        Toast.makeText(this, "Benvenuto", Toast.LENGTH_SHORT).show();
        startActivity(mIntent);
        finish();
    }

    @Override
    public void onNewIntent(Intent intent) {
        byte[] inf = new byte[1];
        String action = intent.getAction();
        if (IntentHandler.feedback) {
            Vibrator myVib = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
            myVib.vibrate(100);
        }
        //Check received intent
        if(action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED) ||
                action.equals(NfcAdapter.ACTION_TECH_DISCOVERED) ||
                action.equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
            Parcelable[] rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            //check whether its an Handy Tool or not
            if(action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED))
                inf = ((NdefMessage) rawMessages[0]).getRecords()[0].getPayload();
            else {
                Toast.makeText(getApplicationContext(), "Il tag non è inizializzato", Toast.LENGTH_SHORT).show();
                inf[0] = 0;
            }
            Intent mIntent = new Intent(this, endow_ja.handytool.FunctionActivity.class);
            mIntent.putExtra("activated", (int)inf[0]);
            Toast.makeText(this, "Benvenuto", Toast.LENGTH_SHORT).show();
            startActivity(mIntent);
            finish();
        }
    }

    //chekc whether NFC is supported
    private void checkNFC() {
        if(this.mNfcAdapter == null) {
            Toast.makeText(this.getApplicationContext(), "Non supporta NFC", Toast.LENGTH_LONG).show();
            finish();
        } else if (this.mNfcAdapter != null && !this.mNfcAdapter.isEnabled()) {
            Toast.makeText(this.getApplicationContext(), "Non hai attivato NFC", Toast.LENGTH_LONG).show();
            finish();
        }

    }
}
