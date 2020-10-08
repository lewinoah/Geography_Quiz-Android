package com.example.geoquiz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class QuizActivity extends AppCompatActivity {
    private Button mTrueButton;
    private Button mFalseButton;
    private Button mNextButton;
    private Button mPrevButton;
    private Button mCheatButton;
    private static final int REQUEST_CODE_CHEAT = 0;
    private boolean mIsCheater;
    private final int MAX_CHEAT = 2;
    private int cheatCount;

    private int totalScore;
    ArrayList<Integer> seen;
    private final String TAG = "QuizActivity";
    private static final String KEY_INDEX = "index";
    private static final String KEY_SCORE = "score";
    private static final String KEY_CHEAT = "cheat_count";
    private static final String KEY_SET = "seen_set";
    private static final String KEY_CHEAT_STATE = "has_cheated";

    private TextView mQuestionTextView;
    private int mCurrentIndex;
    private Question[] mQuestionBank = new Question[]{
        new Question(R.string.question_africa, false),
        new Question(R.string.question_americas, true),
        new Question(R.string.question_asia, true),
        new Question(R.string.question_australia, true),
        new Question(R.string.question_mideast, false),
        new Question(R.string.question_ocean, true)
    };

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_CODE_CHEAT) {
            if (data == null) {
                return;
            }
            mIsCheater = CheatActivity.wasAnswerShown(data);
            if(mIsCheater) {
                mNextButton.setClickable(false); //disable buttons to force an answer out of user
                mPrevButton.setClickable(false); //disable buttons to force an answer out of user
                mQuestionTextView.setClickable(false);
            }
            /////////////////////////////////////////////
            String debugHelper = mNextButton.isClickable() ? "true":"false";
            Log.d(TAG, "NEXT BUTTON is Clickable? " + debugHelper);
            /////////////////////////////////////////////
        }
    }

    private int checkAnswer(boolean userPressedTrue){
        boolean expected = mQuestionBank[mCurrentIndex].isAnswerTrue();
        int messageResId = R.string.incorrect_toast; //ANSWER ASSUMED TO BE WRONG
        if(userPressedTrue == expected){
            //ANSWER IS RIGHT
            messageResId = R.string.correct_toast;
        }
        if (mIsCheater) {
            messageResId = R.string.judgment_toast;
            cheatCount++;
        }
        Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show();
        //HANDLE RETURN
        // 1 for correct
        // 0 for cheating
        // -1 for wrong
        if(mIsCheater || expected==userPressedTrue) return 1;
        return 0;
    }

    private void updateQuestion(){
        int currQuestion  = mQuestionBank[this.mCurrentIndex].getTextRedId();
        mQuestionTextView.setText(currQuestion);
        if(!seen.contains(mCurrentIndex)){
            mTrueButton.setClickable(true);
            mFalseButton.setClickable(true);
            if(cheatCount < MAX_CHEAT) mCheatButton.setClickable(true);
            else {
                int noMoreCheat = R.string.no_more_cheat;
                Toast.makeText(this, noMoreCheat, Toast.LENGTH_SHORT).show();
            }
        }
        else{
            mTrueButton.setClickable(false);
            mFalseButton.setClickable(false);
            mCheatButton.setClickable(false);
        }

        if(seen.size() == mQuestionBank.length){
            //TOAST THE SCORE AND A CONGRATS SIGN
            int messageResId = R.string.fail;
            if(totalScore > seen.size()/2) messageResId = R.string.pass;
            Toast.makeText(this, messageResId, Toast.LENGTH_LONG).show();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        Log.d(TAG, "OnCreate() called");

        /*
        ----------------    RESTORE EXISTING VALUES TO MEMBER VARIABLES -----------
                IN CASES OF ROTATION OR OnStop() Destroy by OS
         */
        mCurrentIndex = 0;
        totalScore = 0;
        cheatCount = 0;
        seen = new ArrayList<>();
        mIsCheater = false;
        if(savedInstanceState != null){
            mCurrentIndex = savedInstanceState.getInt(KEY_INDEX);
            totalScore = savedInstanceState.getInt(KEY_SCORE);
            cheatCount = savedInstanceState.getInt(KEY_CHEAT);
            seen = (ArrayList<Integer>) savedInstanceState.get(KEY_SET);
            mIsCheater = savedInstanceState.getBoolean(KEY_CHEAT_STATE, false);
        }
        ////////////////////////////////////////////////////////////////////
        String x = mIsCheater ? "true":"false";
        Log.d(TAG, " mIsCheater (OnCreate) is " + x);
        ////////////////////////////////////////////////////////////////////

        /*
                ---------   BUTTON INITIALIZATION BELOW   ----------
         */

        mCheatButton = (Button) findViewById(R.id.cheat_button);
        mCheatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean ans = mQuestionBank[mCurrentIndex].isAnswerTrue();
                Intent intent = CheatActivity.newIntent(QuizActivity.this, ans);
                startActivityForResult(intent, REQUEST_CODE_CHEAT);
            }
        });

        mPrevButton = (Button) findViewById(R.id.prev_button);
        mPrevButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                mCurrentIndex = (mCurrentIndex-1) % mQuestionBank.length;
                if(mCurrentIndex < 0){
                    mCurrentIndex += mQuestionBank.length;
                }
                mIsCheater = false;
                updateQuestion();
            }
        });

        mQuestionTextView = (TextView) findViewById(R.id.question_text_view);
        mQuestionTextView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mCurrentIndex = (mCurrentIndex+1) % mQuestionBank.length;
                mIsCheater = false;
                updateQuestion();
            }
        });
        mNextButton = (Button) findViewById(R.id.next_button);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCurrentIndex = (mCurrentIndex+1) % mQuestionBank.length;
                mIsCheater = false;
                updateQuestion();
            }
        });
        mTrueButton =  (Button) findViewById(R.id.true_button);
        mTrueButton.setOnClickListener(new View.OnClickListener(){
           @Override
           public void onClick(View v) {
               totalScore+=checkAnswer(true);
               mTrueButton.setClickable(false);
               mFalseButton.setClickable(false);
               mCheatButton.setClickable(false);
               mNextButton.setClickable(true);
               mPrevButton.setClickable(true);
               mQuestionTextView.setClickable(true);
               seen.add(mCurrentIndex);
           }
       }
        );
        mFalseButton = (Button) findViewById(R.id.false_button);
        mFalseButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                totalScore += checkAnswer(false);
                mTrueButton.setClickable(false);
                mFalseButton.setClickable(false);
                mCheatButton.setClickable(false);
                mNextButton.setClickable(true);
                mPrevButton.setClickable(true);
                mQuestionTextView.setClickable(true);
                seen.add(mCurrentIndex);
            }
        });

        if(mIsCheater){
            mNextButton.setClickable(false);
            mPrevButton.setClickable(false);
            mQuestionTextView.setClickable(false);
        }

        updateQuestion();

    }


    /*
    FOR LOGGING PURPOSES ONLY
    OnResume()
    OnPause()
    onStart()
    OnStop()
    OnDestroy()
    will be @Override  (as shown below)
     */

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "OnStart() called");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "OnStop() called");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "OnResume() called");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "OnPause() called");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "OnDestroy() called");
    }

    /*
    SAVE STATE OF APP DURING ROTATION BELOW
    USING OnSaveInstanceState(Bundle x)
     */

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "OnSaveInstanceState() called");
        outState.putInt(KEY_INDEX, mCurrentIndex);
        outState.putInt(KEY_SCORE, totalScore);
        outState.putInt(KEY_CHEAT, cheatCount);
        outState.putIntegerArrayList(KEY_SET, seen);
        outState.putBoolean(KEY_CHEAT_STATE, mIsCheater);
    }
}