package in.vogo.iot.vogoble;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

public class CustomAdapter extends BaseAdapter {

    Context context;
    String[] questionsList;
    LayoutInflater inflter;
    public static int selectedIndex = 0;

    public CustomAdapter(Context applicationContext, String[] questionsList) {
        this.context = context;
        this.questionsList = questionsList;
        inflter = (LayoutInflater.from(applicationContext));
    }
    @Override
    public int getCount() {
        return questionsList.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View view, final ViewGroup parent) {

        view = inflter.inflate(R.layout.list_items, null);

        TextView helmet = (TextView) view.findViewById(R.id.helmet);
        RadioButton yes = (RadioButton) view.findViewById(R.id.yes);
        ImageView iv = (ImageView) view.findViewById(R.id.fuel);

        yes.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked){
                    selectedIndex = position;
                    for(int i = 0;i<parent.getChildCount();i++){
                        if(i!=position){
                            RadioButton r = (RadioButton)parent.getChildAt(i).findViewById(R.id.yes);
                            r.setChecked(false);
                        }
                    }
                }

            }
        });
        yes.setText(questionsList[position]);

        Random rand=new Random();
        int rad = rand.nextInt((4+1) - 1) + 1;



        switch (rad){
            case 1:
                iv.setImageResource(R.drawable.outline_signal_cellular_1_bar_black_48);
                helmet.setText((1)+" helmets");
                break;
            case 2:
                iv.setImageResource(R.drawable.outline_signal_cellular_2_bar_black_48);
                helmet.setText((2)+" helmets");
                break;
            case 3:
                iv.setImageResource(R.drawable.outline_signal_cellular_3_bar_black_48);
                helmet.setText((2)+" helmets");
                break;
            case 4:
                iv.setImageResource(R.drawable.outline_signal_cellular_4_bar_black_48);
                helmet.setText((1)+" helmets");
                break;
            default:
                iv.setImageResource(R.drawable.outline_signal_cellular_3_bar_black_48);
                helmet.setText((0)+" helmets");

        }

        return view;
    }
}
