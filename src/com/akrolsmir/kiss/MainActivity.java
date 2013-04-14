package com.akrolsmir.kiss;

import java.io.File;
import java.util.Arrays;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepository;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class MainActivity extends SherlockActivity {

	private String localPath, remotePath;
	private Repository localRepo;
	private Git git;
	private boolean updateExists;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		try {
			File sdCard = Environment.getExternalStorageDirectory();
			localPath = sdCard.getAbsolutePath() + "/AShot";
			//remotePath = "git://github.com/akrolsmir/Shot.git"; also works
			remotePath = "https://github.com/akrolsmir/Shot.git";
			localRepo = new FileRepository(localPath + "/.git");
			git = new Git(localRepo);
		} catch (Exception e) {
			Log.d("TAGGG", "TAGGG", e);
		}

		Button clone = (Button) findViewById(R.id.clone);
		clone.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				new CloneRepoTask().execute(remotePath, localPath);
			}
		});

		Button branchCreate = (Button) findViewById(R.id.branchCreate);
		branchCreate.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
					new CheckUpdateTask(MainActivity.this).execute(MainActivity.this);

//					git.branchCreate().setName("master")
//							.setUpstreamMode(SetupUpstreamMode.SET_UPSTREAM)
//							.setStartPoint("origin/master").setForce(true)
//							.call();
			}
		});

		Button pull = (Button) findViewById(R.id.pull);
		pull.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				new PullTask().execute();
			}
		});
		
		new CheckUpdateTask(this).execute(this);
		Log.d("TAGGG", "ACTIVITY1" + this);
	}

	public class CheckUpdateTask extends AsyncTask<SherlockActivity, Integer, Boolean> {
		
		SherlockActivity activity;
		Exception e;
		
		public CheckUpdateTask(SherlockActivity activity) {
			activity = this.activity;
			Log.d("TAGGG", "ACTIVITY" + activity);
		}

		@Override
		protected Boolean doInBackground(SherlockActivity... params) {
			try {
				activity = params[0];
				Log.d("TAGGG", "ACTIVITY0" + activity);
				//				ObjectId to = localRepo
				//						.resolve("refs/remotes/origin/master");
				ObjectId from = localRepo.resolve("refs/heads/master");
				for (Ref ref : git.lsRemote().call()) {
					Log.d("TAGGG", from + "->" + ref.getObjectId());
					return !from.equals(ref.getObjectId());
				}
			} catch (Exception e) {
				this.e = e;
				publishProgress();
				Log.d("TAGGG", "TAGGG", e);
			}
			return null;
		}
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
			super.onProgressUpdate(values);
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			Log.d("TAGGG", "Update found? " + result);
			updateExists = result;
			if(activity != null){
				activity.supportInvalidateOptionsMenu();
			}
			super.onPostExecute(result);
		}

	}

	private class CloneRepoTask extends AsyncTask<String, Integer, Void> {
		
		Exception e;

		@Override
		protected Void doInBackground(String... params) {
			try {
				Git.cloneRepository().setURI(params[0])
						.setDirectory(new File(params[1])).call();
			} catch (Exception e) {
				this.e = e;
				publishProgress();
				Log.d("TAGGG", "TAGGG", e);
			}
			return null;
		}
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
			super.onProgressUpdate(values);
		}

		@Override
		protected void onPostExecute(Void result) {
			Log.d("TAGGG", "DONE CLONING");
			super.onPostExecute(result);
		}
	}

	private class PullTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			try {
				git.pull().call();
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(), Arrays.toString(e.getStackTrace()), Toast.LENGTH_LONG).show();
				Log.d("TAGGG", "TAGGG", e);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			Log.d("TAGGG", "DONE PULLING");
			super.onPostExecute(result);
		}

	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (updateExists)
			getSupportMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.update) {
			new AlertDialog.Builder(this)
			.setTitle("Update Available").show();
		}
		return super.onOptionsItemSelected(item);
	}

}
