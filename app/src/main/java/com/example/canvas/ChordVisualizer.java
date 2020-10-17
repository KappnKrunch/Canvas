package com.example.canvas;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import java.lang.Math;

import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.graphics.Path;
import java.util.ArrayList;
import java.lang.String;

public class ChordVisualizer extends View
{
    Paint paint;

    float co5CenterX;
    float co5CenterY;

    float[] xVerts = new float[12];
    float[] yVerts = new float[12];

    String[] keyBoardNoteOrder = {"C","Db","D","Eb","E","F","Gb","G","Ab","A","Bb","B"}; //order which they appear on the keyboard
    String[] keyBoardNoteOrderSharps = {"C","C#","D","D#","E","F","F#","G","G#","A","A#","B"};

    RectF[][] keys = new RectF[24][2]; //each keys contains multiple rectangles

    int activeChordIndex = 0;
    ArrayList<int[]> chords = new ArrayList<>();
    int[] activeKeys = new int[24];

    //buttons

    RectF addChordButton;
    RectF removeChordButton;
    RectF cycleChordProgressionForwardButton;
    RectF cycleChordProgressionBackwardButton;
    RectF settingsButton;
    RectF showInternalRelationsButton;
    RectF sharpsButton;
    RectF noteSpacingButton;

    //settings

    int vertexSpacing = 11;

    enum menuType{chordDisplay,Settings};
    menuType menu = menuType.chordDisplay;

    enum chordRelational{union,intersection,internal,none};
    chordRelational chordRelationship = chordRelational.none;

    boolean sharps = true;
    boolean showBoundary = true;
    boolean showCircleLine = true;



    public ChordVisualizer(Context context)
    {
        super(context);

        paint = new Paint();
        paint.setAntiAlias(true);
    }



    private void SwitchChords(int direction)
    {
        //takes in an int that corresponds to how many spaces the index shifts
        if(chords.size() > 0)
        {
            activeChordIndex = (activeChordIndex + direction) % chords.size();

            if(activeChordIndex < 0){activeChordIndex+=chords.size();}

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
        int activeKeyCount = 0;
        for(int i = 0; i < 24; i++)
        {
            newChord[i] = activeKeys[i];
            activeKeyCount += activeKeys[i];
        }

        if(activeKeyCount>0){
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

            for (int i = 0; i < 24; i++)
            {
                if (activeKeys[i] != chords.get(activeChordIndex)[i])
                {
                    sequencesMatch = false;
                }
            }

            if (sequencesMatch)
            {
                chords.remove(activeChordIndex);
            }
        }

        for (int i=0; i<activeKeys.length;i++){ activeKeys[i] = 0; }
    }

    private String GetNoteFromVertex(float xPos, float yPos)
    {
        ArrayList<Integer> noteIndexX = new ArrayList<>();
        ArrayList<Integer> noteIndexY = new ArrayList<>();

        for(int i = 0; i < 12; i++)
        {
            if( xVerts[i] == xPos){
                noteIndexX.add(i);
            }

            if( yVerts[i] == yPos){
                noteIndexY.add(i);
            }
        }

        ArrayList<Integer> finalNoteIndex = new ArrayList<>();

        for(int i =0; i < noteIndexX.size();i++)
        {
            if(noteIndexY.contains(noteIndexX.get(i)))
            {
                finalNoteIndex.add(noteIndexX.get(i));
            }
        }

        int noteIndex = finalNoteIndex.get(0);

        //save here
        return keyBoardNoteOrder[noteIndex*vertexSpacing % 12];
    }

    private int GetNoteIndexFromString(String note)
    {
        int returnIndex = -1;

        for(int i = 0; i < keyBoardNoteOrder.length; i++)
        {
            if(note.equals(keyBoardNoteOrder[i]))
            {
                returnIndex = i;
            }
        }

        if(returnIndex == -1)
        {
            Log.e("GetNoteFromString","InvalidNoteName");
        }

        return returnIndex;
    }

    private void DrawArrowhead(Canvas canvas, float xPos, float yPos, float fromX, float fromY, float toX, float toY, Paint paint)
    {
        //gets the rotation between the two points
        float deltaX = fromX - toX;
        float deltaY = fromY - toY;
        float thetaRadians = (float)Math.atan2(deltaY,deltaX) - 3.14159f*0.5f; //add a 90 degree rotation


        //describes an equilateral triangle at the origin then rotates it; based on theta radians
        float size = 20;
        float topVertexX = (0f * (float)Math.cos(thetaRadians)) - (size*0.5f * (float)Math.sin(thetaRadians));
        float topVertexY = (size*0.5f*(float)Math.cos(thetaRadians)) + (0f*(float)Math.sin(thetaRadians));

        float rightVertexX = (size*0.5f * (float)Math.cos(thetaRadians)) - (-size*0.5f * (float)Math.sin(thetaRadians));
        float rightVertexY = (-size*0.5f * (float)Math.cos(thetaRadians)) + (size*0.5f * (float)Math.sin(thetaRadians));

        float leftVertexX = (-size*0.5f * (float)Math.cos(thetaRadians)) - (-size*0.5f * (float)Math.sin(thetaRadians));
        float leftVertexY = (-size*0.5f * (float)Math.cos(thetaRadians)) + (-size*0.5f * (float)Math.sin(thetaRadians));


        //draws the triangle local to (xPos,yPos)
        Path arrowHeadPath = new Path();

        arrowHeadPath.moveTo(topVertexX+xPos,topVertexY+yPos);
        arrowHeadPath.lineTo(leftVertexX+xPos,leftVertexY+yPos);
        arrowHeadPath.lineTo(rightVertexX+xPos,rightVertexY+yPos);
        arrowHeadPath.lineTo(topVertexX+xPos,topVertexY+yPos);

        canvas.drawPath(arrowHeadPath,paint);
    }

    private float[] getVertices(String note)
    {
        //currently not working find a way to search arrays
        float[] noteVertices = new float[2];

        for(int i =0; i<12; i++)
        {
            if( note.equals(keyBoardNoteOrder[i]) )
            {
                noteVertices[0] = xVerts[vertexSpacing*i%12];
                noteVertices[1] = yVerts[vertexSpacing*i%12];

                break;
            }
        }

        return noteVertices;
    }

    private void DrawCO5(Canvas canvas)
    {
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(canvas.getWidth() * 0.01618f * 0.5f);
        paint.setStyle(Paint.Style.STROKE);

        if(chordRelationship == chordRelational.union){paint.setColor(Color.WHITE);}

        float radius = canvas.getWidth() / (1.618f*1.5f);
        float centerX = canvas.getWidth()/2;
        float centerY = canvas.getHeight()/2.8f;

        co5CenterX = centerX; //set these globally
        co5CenterY = centerY;

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


            float newTextX = (float)(centerX + radius * Math.sin(percentAroundCircle) * 1.15f);
            float newTextY = (float)(centerY - radius * Math.cos(percentAroundCircle) * 1.15f);

            float textHeightOffset = (paint.ascent() + paint.descent()) * 0.5f;
            newTextY -= textHeightOffset;

            paint.setStyle(Paint.Style.FILL);
            paint.setTextSize(50);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTypeface(Typeface.DEFAULT);

            if(sharps)
            {
                canvas.drawText(keyBoardNoteOrderSharps[i*vertexSpacing % 12],newTextX,newTextY,paint);
            }
            else
            {
                canvas.drawText(keyBoardNoteOrder[i*vertexSpacing % 12],newTextX,newTextY,paint);
            }
        }
    }

    private void DrawKeyBoard(Canvas canvas)
    {
        //this counts the keyboard as if there's 14 keys (white + black) and removes 2 to make it look like a keyboard
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

    private void DrawCO5Shape(Canvas canvas, float[] noteXVertices, float[] noteYVertices)
    {
        paint.setColor(Color.LTGRAY);
        paint.setStrokeWidth(5);
        paint.setStyle(Paint.Style.FILL);

        int startNoteIndex = 0;

        while(noteXVertices[startNoteIndex] == 0 && noteYVertices[startNoteIndex] == 0 && startNoteIndex < 11)
        {
            startNoteIndex++;
        }

        if(startNoteIndex < 12)
        {
            Path co5Shape = new Path();

            co5Shape.moveTo(noteXVertices[startNoteIndex],noteYVertices[startNoteIndex]);

            for(int i = startNoteIndex; i < 12; i++)
            {
                if(noteXVertices[i] == 0f && noteXVertices[i] == 0f){continue;}

                co5Shape.lineTo(noteXVertices[i],noteYVertices[i]);
            }
            {co5Shape.lineTo(noteXVertices[startNoteIndex],noteYVertices[startNoteIndex]);}

            paint.setColor(Color.LTGRAY);
            paint.setStrokeWidth(5);
            paint.setStyle(Paint.Style.FILL);

            canvas.drawPath(co5Shape,paint);

            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(5);
            paint.setStyle(Paint.Style.STROKE);

            canvas.drawPath(co5Shape,paint);
        }
    }

    private String IntervalBetweenNotes(String note1, String note2)
    {
        String[] intervals = {   "O","m2\nM7","M2\nm7","m3\nM6","M3\nm6","4\n5","T",
                                "5\n4","m6\nM3","M6\nm3","m7\nM2","M7\nm2"  };

        int interval = (GetNoteIndexFromString(note1) - GetNoteIndexFromString(note2) + 12)%12;

        return intervals[interval];
    }


    private void DrawCO5Relations(Canvas canvas, float[] noteXVertices, float[] noteYVertices)
    {
        if(chords.size() > 0)
        {
            float[] otherNoteXVertices = new float[12];
            float[] otherNoteYVertices = new float[12];
            int j = 0;

            for(int i = 0 ; i < 12; i++)
            {
                if(noteXVertices[i] == 0f && noteYVertices[i] == 0f){continue;}

                //if key is down add note to vertex list
                //two notes on the keyboard account for one note in the circle
                if( chords.get((activeChordIndex + chords.size() - 1)%chords.size())[j] == 1 | chords.get((activeChordIndex + chords.size() - 1)%chords.size())[j+12] == 1)
                {
                    float[] vertex = getVertices(keyBoardNoteOrder[j]);

                    otherNoteXVertices[i] = vertex[0];
                    otherNoteYVertices[i] = vertex[1];
                }
                else
                {
                    otherNoteXVertices[i] = 0f;
                    otherNoteYVertices[i] = 0f;
                }

                j = (j+vertexSpacing)%12; //does the indexing by intervals of vertexSpacing because thats how its displayed on the circle
            }

            //change the paint settings

            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(5);
            paint.setStyle(Paint.Style.FILL);
            paint.setTextAlign(Paint.Align.CENTER);

            //go through each vertex of the first chord and map/name its relation to the next chord
            for(int i = 0; i < 12; i++)
            {

                if(noteXVertices[i] == 0f && noteYVertices[i] == 0f){continue;}

                for(int k = 0; k < 12; k++)
                {
                    if(otherNoteXVertices[k] == 0f && otherNoteYVertices[k] == 0f){continue;}

                    paint.setColor(0xfffd837b);

                    canvas.drawLine(noteXVertices[i],noteYVertices[i],otherNoteXVertices[k],otherNoteYVertices[k],paint);

                    float directionalX = otherNoteXVertices[k] - noteXVertices[i];
                    float directionalY = otherNoteYVertices[k] - noteYVertices[i];

                    float directionalMagnitude = (float)Math.sqrt(Math.pow(directionalX,2)  + Math.pow(directionalY,2));

                    directionalX/=directionalMagnitude;
                    directionalY/=directionalMagnitude;

                    float distance = (0.161803f +
                            ((vertexSpacing*12+vertexSpacing*i-vertexSpacing*k)%12)*
                                    (1f/11f)*((vertexSpacing*12+vertexSpacing*i-vertexSpacing*k)%12)*(1f/11f)*2f*0.161803f );


                    float midPointX = noteXVertices[i] + (directionalX*directionalMagnitude*distance) - 40*directionalX;
                    float midPointY = noteYVertices[i] + (directionalY*directionalMagnitude*distance) - 40*directionalY;

                    DrawArrowhead(canvas,midPointX,midPointY,noteXVertices[i],noteYVertices[i],otherNoteXVertices[k],otherNoteYVertices[k],paint);
                    DrawArrowhead(canvas,midPointX,midPointY,noteXVertices[i],noteYVertices[i],otherNoteXVertices[k],otherNoteYVertices[k],paint);
                }
            }

            for(int i = 0; i < 12; i++)
            {
                if(noteXVertices[i] == 0f && noteYVertices[i] == 0f){continue;}

                for(int k = 0; k < 12; k++)
                {
                    if(otherNoteXVertices[k] == 0f && otherNoteYVertices[k] == 0f){continue;}

                    paint.setColor(Color.BLACK);
                    paint.setTextSize(35);
                    paint.setTypeface(Typeface.DEFAULT_BOLD);


                    //gets the directional vector between the vertices
                    float directionalX = otherNoteXVertices[k] - noteXVertices[i];
                    float directionalY = otherNoteYVertices[k] - noteYVertices[i];


                    float directionalMagnitude = (float)Math.sqrt(Math.pow(directionalX,2) + Math.pow(directionalY,2));

                    directionalX/=directionalMagnitude;
                    directionalY/=directionalMagnitude;

                    float distance = (0.161803f +
                            ((vertexSpacing*12+vertexSpacing*i-vertexSpacing*k)%12)*
                                    (1f/11f)*((vertexSpacing*12+vertexSpacing*i-vertexSpacing*k)%12)*(1f/11f)*2f*0.161803f );

                    float midPointX = noteXVertices[i] + (directionalX*directionalMagnitude*distance);
                    float midPointY = noteYVertices[i] + (directionalY*directionalMagnitude*distance);

                    float textHeightOffset = (paint.ascent() + paint.descent()) * 0.5f;

                    String noteA = GetNoteFromVertex(noteXVertices[i],noteYVertices[i]);
                    String noteB = GetNoteFromVertex(otherNoteXVertices[k],otherNoteYVertices[k]);

                    String intervalBetweenNotes = IntervalBetweenNotes(noteA,noteB);

                    if( intervalBetweenNotes.split("\n").length > 1)
                    {
                        String upperInterval = intervalBetweenNotes.split("\n")[0];

                        canvas.drawText(upperInterval,midPointX,midPointY - textHeightOffset,paint);
                    }
                    else
                    {
                        String upperInterval = intervalBetweenNotes.split("\n")[0];
                        canvas.drawText(upperInterval,midPointX,midPointY- textHeightOffset,paint);
                    }

                    canvas.drawCircle(otherNoteXVertices[k],otherNoteYVertices[k],10,paint);
                }

                canvas.drawCircle(noteXVertices[i],noteYVertices[i],10,paint);
            }
        }
    }

    private void DrawCO5InternalRelations(Canvas canvas, float[] noteXVertices, float[] noteYVertices)
    {
        //change the paint settings

        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(5);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);

        //go through each vertex of the first chord and map/name its relation to the next chord
        for(int i = 0; i < 12; i++)
        {
            if(noteXVertices[i] == 0f && noteYVertices[i] == 0f){continue;}

            for(int k = 0; k < 12; k++)
            {
                if(noteXVertices[k] == 0f && noteYVertices[k] == 0f){continue;}

                paint.setColor(0xfffd837b);
                canvas.drawLine(noteXVertices[i],noteYVertices[i],noteXVertices[k],noteYVertices[k],paint);

                float directionalX = noteXVertices[k] - noteXVertices[i];
                float directionalY = noteYVertices[k] - noteYVertices[i];

                float directionalMagnitude = (float)Math.sqrt(Math.pow(directionalX,2)  + Math.pow(directionalY,2));

                float distance = (0.161803f + ((vertexSpacing*12+vertexSpacing*i-vertexSpacing*k)%12)*(1f/11f)*((vertexSpacing*12+vertexSpacing*i-vertexSpacing*k)%12)*(1f/11f)*2f*0.161803f );

                directionalX/=directionalMagnitude;
                directionalY/=directionalMagnitude;


                float midPointX = noteXVertices[i] + (directionalX*directionalMagnitude*distance) - 40*directionalX;
                float midPointY = noteYVertices[i] + (directionalY*directionalMagnitude*distance) - 40*directionalY;

                DrawArrowhead(canvas,midPointX,midPointY,noteXVertices[i],noteYVertices[i],noteXVertices[k],noteYVertices[k],paint);
            }
        }

        for(int i = 0; i < 12; i++)
        {
            if(noteXVertices[i] == 0f && noteYVertices[i] == 0f){continue;}

            for(int k = 0; k < 12; k++)
            {
                if(noteXVertices[k] == 0f && noteYVertices[k] == 0f){continue;}

                paint.setColor(Color.BLACK);
                paint.setTextSize(35);
                paint.setTypeface(Typeface.DEFAULT_BOLD);


                //gets the directional vector between the vertices
                float directionalX = noteXVertices[k] - noteXVertices[i];
                float directionalY = noteYVertices[k] - noteYVertices[i];


                float directionalMagnitude = (float)Math.sqrt(Math.pow(directionalX,2) + Math.pow(directionalY,2));

                directionalX/=directionalMagnitude;
                directionalY/=directionalMagnitude;

                float distance = (0.161803f + ((vertexSpacing*12+vertexSpacing*i-vertexSpacing*k)%12)*(1f/11f)*((vertexSpacing*12+vertexSpacing*i-vertexSpacing*k)%12)*(1f/11f)*2f*0.161803f );


                float midPointX = noteXVertices[i] + (directionalX*directionalMagnitude*distance);
                float midPointY = noteYVertices[i] + (directionalY*directionalMagnitude*distance);

                float textHeightOffset = (paint.ascent() + paint.descent()) * 0.5f;

                String noteA = GetNoteFromVertex(noteXVertices[i],noteYVertices[i]);
                String noteB = GetNoteFromVertex(noteXVertices[k],noteYVertices[k]);

                String intervalBetweenNotes = IntervalBetweenNotes(noteA,noteB);

                if( intervalBetweenNotes.split("\n").length > 1)
                {
                    String upperInterval = intervalBetweenNotes.split("\n")[0];

                    canvas.drawText(upperInterval,midPointX,midPointY - textHeightOffset,paint);
                }
                else
                {
                    String upperInterval = intervalBetweenNotes.split("\n")[0];
                    canvas.drawText(upperInterval,midPointX,midPointY- textHeightOffset,paint);
                }

                canvas.drawCircle(noteXVertices[k],noteYVertices[k],10,paint);
            }

            canvas.drawCircle(noteXVertices[i],noteYVertices[i],10,paint);
        }
    }


    private void DrawCO5Notes(Canvas canvas)
    {
        float[] noteXVertices = new float[12];
        float[] noteYVertices = new float[12];

        int j = 0; //special index for maintaining proper sequencing

        for(int i = 0 ; i < 12; i++)
        {
            //if key is down add note to vertex list
            //two notes on the keyboard account for one note in the circle
            if( activeKeys[j] == 1 | activeKeys[j+12] == 1)
            {
                float[] vertex = getVertices(keyBoardNoteOrder[j]);

                noteXVertices[i] = vertex[0];
                noteYVertices[i] = vertex[1];
            }
            else
            {
                //point at the origin is null
                noteXVertices[i] = 0f;
                noteYVertices[i] = 0f;
            }

            j = (j+vertexSpacing)%12; //does the indexing by intervals of vertexSpacing because thats how its displayed on the circle
        }

        DrawCO5Shape(canvas, noteXVertices, noteYVertices);

        if(chordRelationship == chordRelational.union) {
            DrawCO5Relations(canvas, noteXVertices, noteYVertices);
        }
        else if(chordRelationship == chordRelational.internal)
        {
            DrawCO5InternalRelations(canvas, noteXVertices, noteYVertices);
        }

    }

    private void DrawChordUI(Canvas canvas)
    {
        paint.setTextSize(37.5f);
        paint.setStrokeWidth(3);
        paint.setTypeface(Typeface.DEFAULT);



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

        canvas.drawText("bck",410,canvas.getHeight()*(9f/10) + 75 , paint);



        cycleChordProgressionForwardButton = new RectF(500,canvas.getHeight()*(9f/10),600,canvas.getHeight()*(9f/10) + 120);

        paint.setColor(Color.LTGRAY);
        paint.setStyle(Paint.Style.FILL);

        canvas.drawRect(cycleChordProgressionForwardButton,paint);

        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);

        canvas.drawRect(cycleChordProgressionForwardButton,paint);

        canvas.drawText("fwd",550,canvas.getHeight()*(9f/10) + 75 , paint);



        settingsButton = new RectF(10,10,110,110);

        paint.setColor(Color.LTGRAY);
        paint.setStyle(Paint.Style.FILL);

        canvas.drawRect(settingsButton,paint);

        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);

        canvas.drawRect(settingsButton,paint);

        //canvas.drawText("s",60,70, paint);

    }

    private void WatchChordUIButtons(MotionEvent event)
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

        if(settingsButton.contains(event.getX(),event.getY()))
        {
            menu = menuType.Settings;
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


    //settings menu





    void DrawSettings(Canvas canvas)
    {
        settingsButton = new RectF(10,10,110,110);

        paint.setColor(Color.LTGRAY);
        paint.setStyle(Paint.Style.FILL);

        canvas.drawRect(settingsButton,paint);

        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);

        canvas.drawRect(settingsButton,paint);

        canvas.drawText("back",60,70, paint);




        paint.setColor(Color.LTGRAY);

        showInternalRelationsButton = new RectF(10,120,410,220);

        paint.setStyle(Paint.Style.FILL);

        canvas.drawRect(showInternalRelationsButton,paint);

        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);

        canvas.drawRect(showInternalRelationsButton,paint);

        canvas.drawText(chordRelationship.name(),210,185 , paint);




        paint.setColor(Color.LTGRAY);

        sharpsButton = new RectF(10,230,410,330);

        paint.setStyle(Paint.Style.FILL);

        canvas.drawRect(sharpsButton,paint);

        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);

        canvas.drawRect(sharpsButton,paint);

        canvas.drawText(sharps ? "#" : "b",210,230+65 , paint);



        paint.setColor(Color.LTGRAY);

        noteSpacingButton = new RectF(10,340,410,440);

        paint.setStyle(Paint.Style.FILL);

        canvas.drawRect(noteSpacingButton,paint);

        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);

        canvas.drawRect(noteSpacingButton,paint);

        String intervalSpacingName = "2nd";
        if(vertexSpacing == 5){intervalSpacingName = "4th";}
        else if(vertexSpacing == 7){intervalSpacingName = "5th";}
        else if(vertexSpacing == 11){intervalSpacingName = "7th";}

        canvas.drawText("interval: " + intervalSpacingName,210,405 , paint);
    }





    void WatchSettings(MotionEvent event)
    {
        boolean UIUpdated = false;

        if(settingsButton.contains(event.getX(),event.getY()))
        {
            menu = menuType.chordDisplay;
            UIUpdated = true;
        }

        if(showInternalRelationsButton.contains(event.getX(),event.getY()))
        {
            int ordinals = chordRelational.values().length;
            int index = (chordRelationship.ordinal()+1)%ordinals;

            chordRelationship = chordRelational.values()[index];

            UIUpdated = true;
        }

        if(noteSpacingButton.contains(event.getX(),event.getY()))
        {
            if(vertexSpacing == 1){vertexSpacing = 5;}
            else if(vertexSpacing == 5){vertexSpacing = 7;}
            else if(vertexSpacing == 7){vertexSpacing = 11;}
            else if(vertexSpacing == 11){vertexSpacing = 1;}
            else{ vertexSpacing = 1;}

            UIUpdated = true;
        }

        if(sharpsButton.contains(event.getX(),event.getY()))
        {
            sharps = !sharps;

            UIUpdated = true;
        }

        if( UIUpdated ){this.invalidate();}
    }







    //the program

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);

        canvas.drawRect(0f,0f, canvas.getWidth(), canvas.getHeight(), paint );

        if(menu == menuType.chordDisplay)
        {
            DrawCO5(canvas);
            DrawKeyBoard(canvas);
            DrawCO5Notes(canvas);
            DrawChordUI(canvas);
        }
        else if(menu == menuType.Settings)
        {
            DrawSettings(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if( event.getAction() == MotionEvent.ACTION_DOWN)
        {
            if(menu == menuType.chordDisplay)
            {
                WatchChordUIButtons(event);
                WatchKeyboard(event);
            }
            else if(menu == menuType.Settings)
            {
                WatchSettings(event);
            }
        }

        return super.onTouchEvent(event);
    }
}
