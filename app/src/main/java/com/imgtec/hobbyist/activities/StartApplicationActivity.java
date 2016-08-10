package com.imgtec.hobbyist.activities;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import ch.qos.logback.core.net.LoginAuthenticator;


public class StartApplicationActivity extends AppCompatActivity {

  @Override
  protected void onResume() {
    super.onResume();
    ActivitiesAndFragmentsHelper.startActivityAndFinishPreviousOne(this, new Intent(this, LogInActivity.class));
  }
}
