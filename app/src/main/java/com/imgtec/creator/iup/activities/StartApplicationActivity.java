package com.imgtec.creator.iup.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;


public class StartApplicationActivity extends AppCompatActivity {

  @Override
  protected void onResume() {
    super.onResume();

    ActivitiesAndFragmentsHelper.startActivityAndFinishPreviousOne(this, new Intent(this, LogInActivity.class));
  }
}
