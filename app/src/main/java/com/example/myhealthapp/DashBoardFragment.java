package com.example.myhealthapp;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.myhealthapp.network.model.DailyLimit;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DashBoardFragment extends Fragment {
    private PieChart pieChart;
    TextView tv_food, tv_goal, tv_remaining;

    public DashBoardFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View myV = inflater.inflate(R.layout.fragment_dash_board, container, false);

        pieChart = myV.findViewById(R.id.pie_chart);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        DocumentReference docRef = db.collection("foodLimit").document(user.getUid()).collection(date).document("daily");
        docRef.get().addOnCompleteListener((new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {

                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("IMAD", "DocumentSnapshot data: " + document.getData());
//                            City city = documentSnapshot.toObject(City.class);
                        DailyLimit dl = document.toObject(DailyLimit.class);
                        tv_goal = (TextView) myV.findViewById(R.id.goal);
                        tv_goal.setText(String.valueOf(dl.getDaily_limit()));

                        tv_food = (TextView) myV.findViewById(R.id.food);
                        tv_food.setText(String.valueOf(dl.getConsumption()));

                        // tv_remaining=(TextView) findViewById(R.id.remaining);
                        // tv_remaining.setText(String.valueOf(dl.getDaily_limit()-dl.getConsumption()));
                    } else {
                        Log.d("IMAD", "No such document");

                        //  Map<String, Object> city = new HashMap<>();
                        DailyLimit dlimit = new DailyLimit(2300, 0);

                        docRef.set(dlimit).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Log.d("IMAD", "DocumentSnapshot written with ID: " + docRef.getId());

                                tv_goal = (TextView) myV.findViewById(R.id.goal);
                                tv_goal.setText(String.valueOf(2300));

                                tv_food = (TextView) myV.findViewById(R.id.food);
                                tv_food.setText(String.valueOf(0));
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("IMAD", "Error adding document", e);
                            }
                        });
                    }
                } else {
                    Log.d("IMAD", "get failed with ", task.getException());
                }
                drawPC();
            }
        }));

        return myV;
    }

    private void drawPC() {
        ArrayList<PieEntry> pieEntries = new ArrayList<>();

        int food_cons = Integer.parseInt(tv_food.getText().toString());
        int dlimit = Integer.parseInt(tv_goal.getText().toString());
        int rem = dlimit - food_cons;

        if (rem < 0)
            rem = 0;

        pieEntries.add(new PieEntry(rem, ""));
        pieEntries.add(new PieEntry(food_cons, ""));

        Description desc = new Description();
        desc.setText("");

        pieChart.setDescription(desc);

        PieDataSet pds = new PieDataSet(pieEntries, String.valueOf(dlimit));

        ArrayList<Integer> cols = new ArrayList<>();
        cols.add(Color.parseColor("#FFAB00"));
        cols.add(Color.parseColor("#151724"));

        pds.setColors(cols);

        PieData pieData = new PieData(pds);
        pieChart.setData(pieData);
        pieChart.getLegend().setEnabled(false);
        pieChart.setHoleRadius(90);
        pieChart.setCenterText(String.valueOf(rem));
        pieChart.setCenterTextColor(getResources().getColor(R.color.white));
        pieChart.setCenterTextTypeface(Typeface.DEFAULT_BOLD);
        pieChart.setCenterTextSize(32);
        pieChart.setHoleColor(Color.parseColor("#242833"));

        pieChart.setHighlightPerTapEnabled(false);
        pds.setDrawValues(false);
        pieChart.invalidate();

        pieChart.animateXY(1000, 1000);
    }
}