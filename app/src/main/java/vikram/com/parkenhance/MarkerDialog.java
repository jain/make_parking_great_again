package vikram.com.parkenhance;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by vikram on 6/11/16.
 */
public class MarkerDialog extends Dialog {
    private Activity act;
    private ParkingLot parkingLot;

    public MarkerDialog(Activity act, ParkingLot pLot) {
        super(act);
        this.act = act;
        parkingLot = pLot;
    }

    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.marker_select);
        TextView title = (TextView) findViewById(R.id.title);
        title.setText(parkingLot.getName());

        final Button autoReserve = (Button) findViewById(R.id.auto_reserve);
        autoReserve.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view) {
                autoReserve(view);
            }
        });

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
        Toast.makeText(act, "payments", Toast.LENGTH_LONG).show();
    }

    public void autoReserve(View view) {
        Toast.makeText(act, "will reserve space", Toast.LENGTH_LONG).show();
    }

    public void see(View view) {
        Intent intent = new Intent(act, ParkLayoutActivity.class);
        act.startActivity(intent);
    }
}
