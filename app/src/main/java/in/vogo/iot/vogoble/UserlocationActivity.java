package in.vogo.iot.vogoble;

import android.content.Intent;
import android.media.Image;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class UserlocationActivity extends AppCompatActivity {

    ImageView i1,i2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userlocation);

        i1 = (ImageView) findViewById(R.id.user1);
        i2 = (ImageView) findViewById(R.id.user2);

        i2.setVisibility(View.INVISIBLE);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                i2.setVisibility(View.VISIBLE);
                i1.setVisibility(View.INVISIBLE);
            }
        }, 5000);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(UserlocationActivity.this, MainActivity.class);
                startActivity(i);
            }
        }, 7000);

    }
}
