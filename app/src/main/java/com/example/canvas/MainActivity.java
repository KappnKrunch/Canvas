package com.example.canvas;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity
{

    ChordVisualizer chordVisualization;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        chordVisualization = new ChordVisualizer(this);

        chordVisualization.setBackgroundColor(Color.YELLOW);


        setContentView(chordVisualization);
    }
}
