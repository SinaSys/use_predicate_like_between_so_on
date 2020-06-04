package com.sriyanksiddhartha.realmdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.sriyanksiddhartha.realmdemo.model.SocialAccount;
import com.sriyanksiddhartha.realmdemo.model.User;

import java.util.UUID;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmAsyncTask;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

/*
* 	Author: Sriyank Siddhartha
*
* 	Module 4: Performing Query, Update and Delete Operation
*
* 		"AFTER" project
* **/
public class MainActivity extends AppCompatActivity {

	private static final String TAG = MainActivity.class.getSimpleName();

	private EditText etPersonName, etAge, etSocialAccountName, etStatus;

	private Realm myRealm;
	private RealmAsyncTask realmAsyncTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		etPersonName 		= (EditText) findViewById(R.id.etPersonName);
		etAge 				= (EditText) findViewById(R.id.etAge);
		etSocialAccountName = (EditText) findViewById(R.id.etSocialAccount);
		etStatus 			= (EditText) findViewById(R.id.etStatus);

		myRealm = Realm.getDefaultInstance();

	}

	public void addUserToRealm_Synchronously(View view) {

		final String id = UUID.randomUUID().toString();
		final String name 				= etPersonName.getText().toString();
		final int age 					= Integer.valueOf(etAge.getText().toString());
		final String socialAccountName 	= etSocialAccountName.getText().toString();
		final String status 				= etStatus.getText().toString();

//		try {
//			myRealm.beginTransaction();
//			myRealm.commitTransaction();
//		} catch (Exception e) {
//			myRealm.cancelTransaction();
//		}

		myRealm.executeTransaction(new Realm.Transaction() {
			@Override
			public void execute(Realm realm) {
				SocialAccount socialAccount = realm.createObject(SocialAccount.class);
				socialAccount.setName(socialAccountName);
				socialAccount.setStatus(status);

				User user = realm.createObject(User.class, id);
				user.setName(name);
				user.setAge(age);
				user.setSocialAccount(socialAccount);
			}
		});
	} // Add data to Realm using Main UI Thread. Be Careful: As it may BLOCK the UI.

	public void addUserToRealm_ASynchronously(View view) {

		final String id = UUID.randomUUID().toString();
		final String name 				= etPersonName.getText().toString();
		final int age 					= Integer.valueOf(etAge.getText().toString());
		final String socialAccountName 	= etSocialAccountName.getText().toString();
		final String status 			= etStatus.getText().toString();

		realmAsyncTask = myRealm.executeTransactionAsync(new Realm.Transaction() {
			@Override
			public void execute(Realm realm) {
				SocialAccount socialAccount = realm.createObject(SocialAccount.class);
				socialAccount.setName(socialAccountName);
				socialAccount.setStatus(status);

				User user = realm.createObject(User.class, id);
				user.setName(name);
				user.setAge(age);
				user.setSocialAccount(socialAccount);
			}
		}, new Realm.Transaction.OnSuccess() {
			@Override
			public void onSuccess() {
				Toast.makeText(MainActivity.this, "Added Successfully", Toast.LENGTH_SHORT).show();
			}
		}, new Realm.Transaction.OnError() {
			@Override
			public void onError(Throwable error) {
				Toast.makeText(MainActivity.this, "Error Occurred", Toast.LENGTH_SHORT).show();
			}
		});
	} // Add Data to Realm in the Background Thread.

	public void sampleQueryExample(View view) {

		// Write your code here
		// displayQueriedUsers(userList2);

		//Method 1
	/*	RealmQuery<User> realmQuery = myRealm.where(User.class);
		realmQuery.greaterThan("age",15); //condition1
		realmQuery.contains("name","john", Case.INSENSITIVE); //condition2

		RealmResults<User> userList = realmQuery.findAll();
		displayQueriedUsers(userList);*/


		//use between
	/*	RealmQuery<User> realmQuery = myRealm.where(User.class);
		RealmResults<User> userList = realmQuery
				.between("age",20,40)
				.findAll();
		displayQueriedUsers(userList);*/


		// use group condition
	/*	RealmQuery<User> realmQuery = myRealm.where(User.class);
		RealmResults<User> userList = realmQuery
				.between("age",20,40)
				.beginGroup()
				.endsWith("name","n")
				.or()
				.contains("name","pe")
				.endGroup()
				.findAll();
		displayQueriedUsers(userList);*/

	//chaining queries
		RealmQuery<User> realmQuery = myRealm.where(User.class);
		RealmResults<User> userList = realmQuery
				.findAll()
				.sort("SocialAccount.name", Sort.DESCENDING);
				//.sort("SocialAccount.name", Sort.DESCENDING)
		displayQueriedUsers(userList);

	}

	public void displayAllUsers(View view) {

		RealmResults<User> userList = myRealm.where(User.class).findAll();
		displayQueriedUsers(userList);
	}

	private void displayQueriedUsers(RealmResults<User> userList) {

		StringBuilder builder = new StringBuilder();

		for (User user : userList) {
			builder.append("ID: ").append(user.getId());
			builder.append("\nName: ").append(user.getName());
			builder.append(", Age: ").append(user.getAge());

			SocialAccount socialAccount = user.getSocialAccount();
			builder.append("\nS'Account: ").append(socialAccount.getName());
			builder.append(", Status: ").append(socialAccount.getStatus()).append(" .\n\n");
		}

		Log.i(TAG + " UserList", builder.toString());
	}

	@Override
	protected void onStop() {
		super.onStop();

		if (realmAsyncTask != null && !realmAsyncTask.isCancelled()) {
			realmAsyncTask.cancel();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		myRealm.close();
	}
}
