package com.example.canvas;


import net.mabboud.android_tone_player.ContinuousBuzzer;
import net.mabboud.android_tone_player.OneTimeBuzzer;

public class ChordPlayer
{

    float[] hzIndex = new float[]{  65.41f,69.30f,73.42f,77.78f,82.41f,87.31f,92.50f,98.00f,103.83f,110.0f,116.54f,123.47f,130.81f,
                                    138.59f,146.83f,155.56f,164.81f,174.61f,185.0f,196.0f,207.65f,220.0f,233.08f,246.94f,261.63f};

    OneTimeBuzzer masterChord;

    ChordPlayer()
    {
        masterChord = new OneTimeBuzzer();
    }

    public void PlayChord(int[] activeKeys, float duration)
    {
        int chordIndex = 0;

        for(int i = 0; i < 24;i++)
        {

            chordIndex = (chordIndex+7)%24;

            if( activeKeys[chordIndex] == 1)
            {
                masterChord.stop();
                masterChord.setDuration(duration);
                masterChord.setVolume(100);

                masterChord.setToneFreqInHz(hzIndex[i]);

                masterChord.play();
            }
        }



    }


}