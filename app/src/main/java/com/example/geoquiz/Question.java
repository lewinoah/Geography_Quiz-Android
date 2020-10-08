package com.example.geoquiz;

public class Question {
    private int mTextRedId;
    private boolean mAnswerTrue;

    public int getTextRedId() {
        return mTextRedId;
    }

    public void setTextRedId(int textRedId) {
        mTextRedId = textRedId;
    }

    public boolean isAnswerTrue() {
        return mAnswerTrue;
    }

    public void setAnswerTrue(boolean answerTrue) {
        mAnswerTrue = answerTrue;
    }

    public Question(int newResId, boolean ans){
        this.mAnswerTrue = ans;
        this.mTextRedId = newResId;
    }
}
