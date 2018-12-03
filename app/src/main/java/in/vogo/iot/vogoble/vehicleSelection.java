package in.vogo.iot.vogoble;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

public class vehicleSelection extends AppCompatActivity {
    ListView simpleList;
    String[] vehicles = {"KA-06-AS-5050","KA-05-BS-3422","KA-06-AS-5050","KA-05-BS-3422","KA-06-AS-5050","KA-05-BS-3422"};
    Button submit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_selection);
        simpleList = (ListView) findViewById(R.id.simpleListView);
        submit = (Button) findViewById(R.id.submit);

        CustomAdapter customAdapter = new CustomAdapter(getApplicationContext(), vehicles);
        simpleList.setAdapter(customAdapter);


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Index",""+CustomAdapter.selectedIndex);
                Intent i = new Intent(vehicleSelection.this, UserlocationActivity.class);
                startActivity(i);
            }
        });

    }
}
