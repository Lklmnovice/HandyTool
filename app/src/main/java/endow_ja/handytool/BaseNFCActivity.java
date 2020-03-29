package endow_ja.handytool;


import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

/**
 * Questa è una classe base che permette di lavorare con i tag NFC
 * Consente di avere la precedenza in caso che si individui un tag NFC
 * Anche scrivere and leggere i tag, attraverso writeTag e readTag
 *
 * Per qualsiasi Activity che svilupperemo, si deve derivare da essa
 */
public class BaseNFCActivity extends AppCompatActivity {
    protected NfcAdapter mNfcAdapter;       //adapter per avere supporto nativo di nfc
    private PendingIntent mPendingIntent;   //un pendIntent per realizare il meccanismo di precedenza
    protected Tag detectedTag;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
    }

    //foreground nfc dispatching
    @Override
    protected void onStart() {
        super.onStart();
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass() ), 0);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mNfcAdapter != null)
            mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mNfcAdapter != null)
            mNfcAdapter.disableForegroundDispatch(this);
    }

    /**
     * La funzione per scrivere un messaggio NDEF in un tag
     * @param message il messaggio NDEF da scrivere
     * @param tag il tag
     * @return true - successo, viceversa
     */
    protected boolean writeTag(NdefMessage message, Tag tag) {
        int size = message.toByteArray().length;
        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                ndef.connect();
                if (!ndef.isWritable()) {
                    return false;
                }
                if (ndef.getMaxSize() < size) {
                    return false;
                }
                ndef.writeNdefMessage(message);
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }


    protected NdefMessage buildMessage(byte[] data) {
        NdefMessage ndefMessage = new NdefMessage(NdefRecord.createExternal(
                "com.ferrarisBrunelleschi.handytool",  // your domain name
                "Functions",
                data));
        return ndefMessage;
    }
}
