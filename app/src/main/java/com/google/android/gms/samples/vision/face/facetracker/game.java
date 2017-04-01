package com.google.android.gms.samples.vision.face.facetracker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.camera2.params.Face;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by 40737 on 2017/2/4.
 */

public class game extends Thread{

    private float x;
    private float y;
    private Context context;

    public game(Context context) {
        this.context = context;
    }

    public void setButtonListener(Button button, final TextView textView) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (textView.getText() != "It works.") {
                    textView.setText("It works.");
                } else {
                    textView.setText("Listen from fragment.");
                }
            }
        });
    }

    public void setCoordinates(float _x, float _y, Handler handler) {
        x = _x;
        y = _y;

        Message msg = new Message();
        msg.what = 1;
        handler.sendMessage(msg);
    }
}
