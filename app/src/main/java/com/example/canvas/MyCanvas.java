package com.example.canvas;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import java.lang.Math;

import android.graphics.Point;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.graphics.Path;
import java.util.ArrayList;

public class MyCanvas extends View
{

    Paint paint;
    float[] xVerts = new float[12];
    float[] yVerts = new float[12];

    String[] musicalNotes = {"C","G","D","A","E","B","Gb","Db","Ab","Eb","Bb","F"}; //order which they appear on the circle
    String[] keyBoardNoteOrder = {"C","Db","D","Eb","E","F","Gb","G","Ab","A","Bb","B"}; //order which they appear on the keyboard

    RectF[][] keys = new RectF[24][2]; //each keys contains multiple rectangles

    int activeChordIndex = 0;
    ArrayList<int[]> chords = new ArrayList<>();
    int[] activeKeys = new int[24];

    RectF addChordButton;
    RectF removeChordButton;
    RectF cycleChordProgressionForwardButton;
    RectF cycleChordProgressionBackwardButton;

    ChordPlayer chordPlayer;


    public MyCanvas(Context context)
    {
        super(context);

        paint = new Paint();
        paint.setAntiAlias(true);

        chordPlayer = new ChordPlayer();
    }



    private void SwitchChords(int direction)
    {
        //takes in an int that corresponds to how many spaces the index shifts
        if(chords.size() > 0) {
            activeChordIndex = (activeChordIndex + direction) % chords.size();
            if( activeChordIndex < 0){activeChordIndex+=chords.size();}

            //activeKeys = chords.get(activeChordIndex);

            for (int i=0;i<activeKeys.length;i++)
            {
                activeKeys[i] = chords.get(activeChordIndex)[i];

            }
        }
    }

    private void AddChord()
    {
        //creates a new set of active keys and copies the current sequence
        int[] newChord = new int[24];
        for(int i=0; i < 24; i++){ newChord[i]=activeKeys[i]; }

        if(chords.size()>0){
            chords.add((activeChordIndex + 1),newChord);
        }
        else{
            chords.add(newChord);
        }


        //sets all the keys to in active after adding chord
        for (int i=0; i<activeKeys.length;i++){ activeKeys[i] = 0; }
    }

    private void RemoveChord()
    {
        if( chords.size() > 0)
        {
            boolean sequencesMatch = true;
            for (int i = 0; i < 24; i++) {
                if (activeKeys[i] != chords.get(activeChordIndex)[i]) {
                    sequencesMatch = false;
                }
            }

            if (sequencesMatch) {
                chords.remove(activeChordIndex);
            }
        }

        for (int i=0; i<activeKeys.length;i++){ activeKeys[i] = 0; }

    }

    private float[] getVertices(String note)
    {
        //currently not working find a way to search arrays
        float[] noteVertices = new float[2];

        for(int i =0; i<musicalNotes.length; i++)
        {
            if( note.equals(musicalNotes[i]) )
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
        paint.setStyle(Paint.Style.FILL);

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

                //this is the bit that actually
                if(i % 2 == 0)
                {

                    if(activeKeys[currentKeyIndex] == 0)
                    {
                        paint.setColor(Color.LTGRAY);
                    }
                    else
                    {
                        paint.setColor(0xfffd837b);//0xfd837b
                    }

                    paint.setStyle(Paint.Style.FILL);

                    //for the white keys multiple rectangles are needed because the area that can be tapped isn't a perfect rectangle
                    currentKey = new RectF(xPos - whiteKeyWidth*0.48f , yPos + whiteKeyHeight*0.12f,
                            xPos + whiteKeyWidth*0.48f,yPos + whiteKeyHeight*0.48f);

                    RectF currentKeyPt2 = new RectF(xPos - whiteKeyWidth*0.24f , yPos - whiteKeyHeight*0.48f,
                            xPos + whiteKeyWidth*0.24f,yPos + whiteKeyHeight*0.48f);

                    RectF currentKeyPt3 = new RectF(xPos - whiteKeyWidth*0.48f , yPos - whiteKeyHeight*0.48f,
                            xPos + whiteKeyWidth*0.48f,yPos + whiteKeyHeight*0.48f);


                    canvas.drawRect(currentKey,paint);
                    canvas.drawRect(currentKeyPt2,paint);
                    canvas.drawRect(currentKeyPt3,paint);

                    keys[currentKeyIndex][1] = currentKeyPt2;
                }
                else
                {
                    if(activeKeys[currentKeyIndex] == 0)
                    {
                        paint.setColor(Color.BLACK);
                    }
                    else
                    {
                        paint.setColor(0xffb84451);
                    }

                    paint.setStyle(Paint.Style.FILL);

                    currentKey = new RectF(xPos - blackKeyWidth*0.5f , yPos - blackKeyHeight*0.32f - blackKeyHeight*0.5f,
                            xPos + blackKeyWidth*0.5f,yPos - blackKeyHeight*0.32f + blackKeyHeight*0.5f);
                    canvas.drawRect(currentKey,paint );
                }



                keys[currentKeyIndex][0] = currentKey;

                //kind of awkward solution to mapping the key triangles to the correct notes while maintaining the proper clipping
                //ie black keys go on top of white keys

                currentKeyIndex+=2;
                if(keyboardLayout[(i+1) %14] == 0)
                {
                    currentKeyIndex--;
                }
            }
            else{currentKeyIndex++;} //basically mimicking what the loop is doing (counting in 2) but changes the sequence so that its out of 12 keys and not 14



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
        float[] noteXVertices = new float[12];
        float[] noteYVertices = new float[12];

        int vertexCount = 0;

        int j = 0; //special index for maintaining proper sequencing

        for(int i = 0 ; i < 12; i++)
        {
            if( activeKeys[j] == 1 | activeKeys[j+12] == 1)
            {
                float[] vertex = getVertices(keyBoardNoteOrder[j]);

                noteXVertices[vertexCount] = vertex[0];
                noteYVertices[vertexCount] = vertex[1];

                vertexCount++;
            }

            j = (j+7)%12; //does the indexing by intervals of 7 because thats how its displayed on the circle
        }

        paint.setColor(Color.LTGRAY);
        paint.setStrokeWidth(5);
        paint.setStyle(Paint.Style.FILL);

        Path co5Shape = new Path();
        co5Shape.moveTo(noteXVertices[0],noteYVertices[0]);
        for(int i = 1; i < vertexCount; i++)
        {
            co5Shape.lineTo(noteXVertices[i],noteYVertices[i]);
        }
        co5Shape.lineTo(noteXVertices[0],noteYVertices[0]);

        paint.setColor(Color.LTGRAY);
        paint.setStrokeWidth(5);
        paint.setStyle(Paint.Style.FILL);

        canvas.drawPath(co5Shape,paint);

        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(5);
        paint.setStyle(Paint.Style.STROKE);

        canvas.drawPath(co5Shape,paint);
    }

    private void DrawUI(Canvas canvas)
    {
        paint.setTextSize(37.5f);
        paint.setStrokeWidth(3);


        addChordButton = new RectF(180,canvas.getHeight()*(9f/10),280,canvas.getHeight()*(9f/10)+ 120);

        paint.setColor(Color.LTGRAY);
        paint.setStyle(Paint.Style.FILL);

        canvas.drawRect(addChordButton,paint);

        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);

        canvas.drawRect(addChordButton,paint);

        canvas.drawText("+",230,canvas.getHeight()*(9f/10) + 75 , paint);



        removeChordButton = new RectF(40,canvas.getHeight()*(9f/10),140,canvas.getHeight()*(9f/10)+ 120);

        paint.setColor(Color.LTGRAY);
        paint.setStyle(Paint.Style.FILL);

        canvas.drawRect(removeChordButton,paint);

        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);

        canvas.drawRect(removeChordButton,paint);

        canvas.drawText("-",90,canvas.getHeight()*(9f/10) + 75 , paint);


        cycleChordProgressionBackwardButton = new RectF(360,canvas.getHeight()*(9f/10),460,canvas.getHeight()*(9f/10)+ 120);

        paint.setColor(Color.LTGRAY);
        paint.setStyle(Paint.Style.FILL);

        canvas.drawRect(cycleChordProgressionBackwardButton,paint);

        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);

        canvas.drawRect(cycleChordProgressionBackwardButton,paint);

        canvas.drawText("<",410,canvas.getHeight()*(9f/10) + 75 , paint);


        cycleChordProgressionForwardButton = new RectF(500,canvas.getHeight()*(9f/10),600,canvas.getHeight()*(9f/10) + 120);

        paint.setColor(Color.LTGRAY);
        paint.setStyle(Paint.Style.FILL);

        canvas.drawRect(cycleChordProgressionForwardButton,paint);

        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);

        canvas.drawRect(cycleChordProgressionForwardButton,paint);

        canvas.drawText(">",550,canvas.getHeight()*(9f/10) + 75 , paint);

    }


    private void WatchUIButtons(MotionEvent event)
    {
        boolean UIUpdated = false;

        if( addChordButton.contains(event.getX(),event.getY()))
        {
            AddChord();
            UIUpdated = true;
        }

        if( removeChordButton.contains(event.getX(),event.getY()))
        {
            RemoveChord();
            UIUpdated = true;
        }

        if(cycleChordProgressionForwardButton.contains(event.getX(),event.getY()))
        {
            SwitchChords(1);
            UIUpdated = true;
        }

        if(cycleChordProgressionBackwardButton.contains(event.getX(),event.getY()))
        {
            SwitchChords(-1);
            UIUpdated = true;
        }

        if( UIUpdated ){this.invalidate();}

    }

    private void WatchKeyboard(MotionEvent event)
    {
        //checks the keys
        for(int i = 0; i < keys.length;i++)
        {
            //if the point where you tap is inside any of the keys rectangles
            //if it is, the keys gets toggled
            boolean inKey1 = (keys[i][0].contains(event.getX(),event.getY()));

            boolean inKey2 = false;
            if( keys[i][1] != null ){inKey2 = keys[i][1].contains(event.getX(),event.getY());}

            if(inKey1 | inKey2){
                activeKeys[i] = (activeKeys[i]+1) % 2; //toggles the key
                this.invalidate();
            }
        }
    }




    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        DrawCO5(canvas);
        DrawKeyBoard(canvas);
        DrawNotes(canvas);
        DrawUI(canvas);

        //chordPlayer.PlayChord(activeKeys,0.5f);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if( event.getAction() == MotionEvent.ACTION_DOWN)
        {
            WatchUIButtons(event);
            WatchKeyboard(event);
        }

        return super.onTouchEvent(event);
    }
}
