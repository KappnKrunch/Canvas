package com.example.canvas;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import java.lang.Math;

import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class MyCanvas extends View
{

    Paint paint;
    float[] xVerts = new float[12];
    float[] yVerts = new float[12];

    String[] musicalNotes = {"C","G","D","A","E","B","Gb","Db","Ab","Eb","Bb","F"};
    String[] keyBoardNoteOrder = {"C","Db","D","Eb","E","F","Gb","G","Ab","A","Bb","B"};

    RectF[] keys = new RectF[24]; //each keys contains multiple rectangles
    int[] activeKeys = new int[24];



    public MyCanvas(Context context)
    {
        super(context);

        paint = new Paint();
        paint.setAntiAlias(true);

    }

    private float[] getVertices(String note)
    {
        //currently not working find a way to search arrays
        float[] noteVertices = new float[2];

        for(int i =0; i<musicalNotes.length; i++)
        {
            if( note == musicalNotes[i])
            {
                noteVertices[0] = xVerts[i];
                noteVertices[1] = yVerts[i];

                break;
            }
        }

        return noteVertices;
    }

    private void DrawCO5(Canvas canvas)
    {

        paint.setColor(Color.WHITE);

        canvas.drawRect(0f,0f,canvas.getWidth(), canvas.getHeight(), paint );

        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(canvas.getWidth() * 0.01618f * 0.5f);
        paint.setStyle(Paint.Style.STROKE);



        float radius = canvas.getWidth() / (1.618f*2);
        float centerX = canvas.getWidth()/2;
        float centerY = canvas.getHeight()/3;

        canvas.drawCircle(centerX,centerY,radius,paint);


        for(int i = 0; i < 12; i++)
        {
            float percentAroundCircle = (( ((float)i) /12) * 3.14159f * 2);


            float newX = (float)(centerX + radius * Math.sin(percentAroundCircle));
            float newY = (float)(centerY - radius * Math.cos(percentAroundCircle));

            xVerts[i] = newX;
            yVerts[i] = newY;

            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(newX,newY,10,paint);

            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(newX,newY,10,paint);


            float newTextX = (float)(centerX + radius * Math.sin(percentAroundCircle) * 1.3f);
            float newTextY = (float)(centerY - radius * Math.cos(percentAroundCircle) * 1.3f);

            paint.setStyle(Paint.Style.FILL);
            paint.setTextSize(50);
            paint.setTextAlign(Paint.Align.CENTER);

            canvas.drawText(musicalNotes[i],newTextX,newTextY,paint);


        }
    }

    private void DrawKeyBoard(Canvas canvas)
    {
        //this counts the keyboard as if theres 14 keys (white + black) and removes 2 to make it look like a keyboard
        int[] keyboardLayout = {1,1,1,1,1,0,1,1,1,1,1,1,1,0};

        float whiteKeyWidth = 25f;
        float whiteKeyHeight = 75;

        float keyScale = canvas.getWidth()/(whiteKeyWidth*28*0.5f);

        whiteKeyHeight = whiteKeyHeight*keyScale;
        whiteKeyWidth = whiteKeyWidth*keyScale;

        float blackKeyWidth = whiteKeyWidth * 0.5f ;
        float blackKeyHeight = whiteKeyHeight * (2f/3);

        float xPos = whiteKeyWidth * 0.5f;
        float yPos = canvas.getHeight() * (4f/5);

        int currentKeyIndex = 0;


        for(int i = 0; i < keyboardLayout.length * 2; i+=2)
        {
            //the loops counts by two in order to separate the white and black keys, black keys go on top of white keys
            if( keyboardLayout[i%14] == 1)
            {
                RectF currentKey;

                //Log.d("CREATION",String.valueOf(currentKeyIndex) + "," + String.valueOf(i));
                if(i % 2 == 0)
                {

                    if(activeKeys[currentKeyIndex] == 0)
                    {
                        paint.setColor(Color.LTGRAY);
                    }
                    else
                    {
                        paint.setColor(Color.RED);
                    }

                    paint.setStyle(Paint.Style.FILL);

                    //for the white keys two rectangles are needed currentKey is the lower half
                    currentKey = new RectF(xPos - whiteKeyWidth*0.48f , yPos + whiteKeyHeight*0.12f,
                            xPos + whiteKeyWidth*0.48f,yPos + whiteKeyHeight*0.48f);

                    canvas.drawRect(currentKey,paint);

                    RectF currentKeyPt2 = new RectF(xPos - whiteKeyWidth*0.48f , yPos - whiteKeyHeight*0.48f,
                            xPos + whiteKeyWidth*0.48f,yPos + whiteKeyHeight*0.48f);

                    //canvas.drawRect(currentKeyPt2,paint);
                }
                else
                {
                    if(activeKeys[currentKeyIndex] == 0)
                    {
                        paint.setColor(Color.BLACK);
                    }
                    else
                    {
                        paint.setColor(Color.RED);
                    }

                    paint.setStyle(Paint.Style.FILL);

                    currentKey = new RectF(xPos - blackKeyWidth*0.5f , yPos - blackKeyHeight*0.32f - blackKeyHeight*0.5f,
                            xPos + blackKeyWidth*0.5f,yPos - blackKeyHeight*0.32f + blackKeyHeight*0.5f);
                    canvas.drawRect(currentKey,paint );
                }

                keys[currentKeyIndex] = currentKey;

                //kind of awkward solution to mapping the key triangles to the correct notes while maintaining the proper clipping
                //ie black keys go on top of white keys

                currentKeyIndex+=2;
                if(keyboardLayout[(i+1) %14] == 0)
                {
                    currentKeyIndex--;
                }
            }
            else
            {
                currentKeyIndex++;
            }

            xPos += whiteKeyWidth;

            //after going through the white keys ( i even), switches the black keys (i odd) and counts up in 2
            if( i == keyboardLayout.length*2 - 2)
            {
                i=-1;
                currentKeyIndex=1;
                xPos = whiteKeyWidth * 0.5f + blackKeyWidth;
            }
        }

        paint.setColor(Color.WHITE);
        canvas.drawRect(0,canvas.getHeight() * (3.55f/5), canvas.getWidth(), canvas.getHeight() * (3.75f/5), paint);
    }

    private void DrawNotes(Canvas canvas)
    {
        float[] noteXVertices = new float[24];
        float[] noteYVertices = new float[24];

        int vertexCount = 0;

        for(int i = 0 ; i < activeKeys.length; i++)
        {
            if( activeKeys[i] == 1)
            {
                float[] vertex = getVertices(keyBoardNoteOrder[i%12]);

                noteXVertices[vertexCount] = vertex[0];
                noteYVertices[vertexCount] = vertex[1];

                vertexCount++;

            }
        }

        paint.setColor(Color.BLACK);
        for(int i = 0; i < vertexCount; i++)
        {
            for(int j = 0; j < vertexCount; j++)
            {
                canvas.drawLine(noteXVertices[i],noteYVertices[i],noteXVertices[j],noteYVertices[j],paint);
            }
        }
    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        DrawCO5(canvas);
        DrawKeyBoard(canvas);
        DrawNotes(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if( event.getAction() == MotionEvent.ACTION_DOWN)
        {
            for(int i = 0; i < keys.length;i++)
            {
                //if the point where you tap is inside any of the keys rectangles
                boolean inKey = (keys[i].contains(event.getX(),event.getY()));

                if(inKey){
                    activeKeys[i] = (activeKeys[i]+1) % 2; //toggles the key
                    this.invalidate();
                }
            }
        }

        return super.onTouchEvent(event);
    }
}
