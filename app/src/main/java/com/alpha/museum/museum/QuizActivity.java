package com.alpha.museum.museum;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.GridView;
import android.widget.TextView;

import com.alpha.museum.museum.Adapters.QuizAdapter;

import java.util.ArrayList;

public class QuizActivity extends AppCompatActivity {

    TextView questionTxt;
    GridView responsesLst;

    private String question_a = "What's your name ?";
    private ArrayList<String> responses = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        questionTxt = (TextView) findViewById(R.id.question);
        responsesLst = (GridView) findViewById(R.id.responses);

        responses.add("Tibari");
        responses.add("Moha");
        responses.add("Arbi");
        responses.add("Hammo");

        questionTxt.setText(question_a);

        QuizAdapter quizAdapter = new QuizAdapter(getApplicationContext(), responses);
        responsesLst.setAdapter(quizAdapter);
    }
}
