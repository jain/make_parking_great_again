package vikram.com.parkenhance;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.TextHttpResponseHandler;

import cz.msebera.android.httpclient.Header;

/**
 * Created by vikram on 6/11/16.
 */
public class MarkerDialog extends Dialog {
    private MapsActivity act;
    private ParkingLot parkingLot;

    public MarkerDialog(MapsActivity act, ParkingLot pLot) {
        super(act);
        this.act = act;
        parkingLot = pLot;
    }

    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.marker_select);
        TextView title = (TextView) findViewById(R.id.title);
        title.setText(parkingLot.getName());

        //final Button autoReserve = (Button) findViewById(R.id.auto_reserve);
        /*autoReserve.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view) {
                autoReserve(view);
            }
        });*/

        final Button see = (Button) findViewById(R.id.see);
        see.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view) {
                see(view);
            }
        });

        final Button contact = (Button) findViewById(R.id.contact);
        contact.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view) {
                contact(view);
            }
        });
    }

    public void contact(View view) {
        //Toast.makeText(act, "payments", Toast.LENGTH_LONG).show();
        LayoutInflater layoutInflater = LayoutInflater.from(act);
        View promptView = layoutInflater.inflate(R.layout.contact_owner, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(act);
        builder.setView(promptView);
        final EditText editText = (EditText) promptView.findViewById(R.id.edittext);
        builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // FIRE ZE MISSILES!
                        String msg = editText.getText().toString();
                        final String add = "?id="+Common.id+"&lot="+Common.parkingLot.getName()+"&msg="+msg;
                        act.client.get(Common.BASEURL + Common.OWNER_MESSAGE+add, new TextHttpResponseHandler() {
                            @Override
                            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                                Toast.makeText(act, "failed to contact server", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                                Toast.makeText(act, "Your message has been sent to owner", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        // Create the AlertDialog object and return it
        //return builder.create();
        builder.create().show();
    }

    public void autoReserve(View view) {
        Toast.makeText(act, "will reserve space", Toast.LENGTH_LONG).show();
    }

    public void see(View view) {
        Intent intent = new Intent(act, ParkLayoutActivity.class);
        act.startActivity(intent);
    }
}
