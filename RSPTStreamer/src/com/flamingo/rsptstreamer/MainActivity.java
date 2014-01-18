package com.flamingo.rsptstreamer;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import com.flamingo.service.PlayerService;
import com.flamingo.service.PlayerService.OnMediaPlayerStateChangeListener;
import com.flamingo.service.PlayerService.PlayerServiceBinder;

/*
 * Copyright 2014 Chris Eggison
 *
 *	Licensed under the Apache License, Version 2.0 (the "License");
 *	you may not use this file except in compliance with the License.
 *	You may obtain a copy of the License at
 *	
 *	 http://www.apache.org/licenses/LICENSE-2.0
 *	
 *	Unless required by applicable law or agreed to in writing, software
 *	distributed under the License is distributed on an "AS IS" BASIS,
 *	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *	See the License for the specific language governing permissions and
 *	limitations under the License.
 */

/**
 * Primary UI entrypoint for users. Shows a dialog with simplistic information feedback and user controls.
 *
 * @author Chris
 *
 */
public class MainActivity extends Activity {

	private PlayerService service;
	private boolean isServiceBound = false;
	private ServiceConnection playerConnection;
	private OnMediaPlayerStateChangeListener serviceListener;
	
	private TextView tvTitle;
	private ImageButton ibPlay;
	
	private String curUrl = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.activity_main);
		
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		ibPlay = ((ImageButton) findViewById(R.id.ibPlay));
		
		if (getIntent() != null) {
			if (getIntent().getData() != null) {
				curUrl = getIntent().getData().toString();
			} else {
				if (getIntent().getExtras() != null) {
					if (!getIntent().getExtras().containsKey("_showinfo")) {
						this.finish();
					}
				}
			}
		}
		
		Intent i = new Intent(MainActivity.this, PlayerService.class);
		
		setupServiceConnection();
		
		ibPlay.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isServiceBound) {
					if (service.isPlaying()) {
						service.stopStream();
						ibPlay.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_play));
					} else {
						service.startStream();
						ibPlay.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_pause));
					}
				}
			}
		});
		
		((ImageButton) findViewById(R.id.ibQuit)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				service.stopStream();
				service.stopForeground(true);
				finish();
			}
		});
		
		getApplicationContext().bindService(i, playerConnection, Service.BIND_AUTO_CREATE);
	}

	/**
	 * Creates a new {@link ServiceConnection} object and a {@link OnMediaPlayerStateChangeListener} object.
	 */
	private void setupServiceConnection() {
		playerConnection = new ServiceConnection() {
			@Override
			public void onServiceDisconnected(ComponentName name) {
				isServiceBound = false;
			}
			
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				isServiceBound = true;
				MainActivity.this.service = ((PlayerServiceBinder) service).getService();
				onFirstConnection();
				
				MainActivity.this.service.stopForeground(true);
				
				serviceListener = new OnMediaPlayerStateChangeListener() {
					@Override
					public void onStopped() {
						tvTitle.setText("Paused");
						ibPlay.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_play));
					}
					
					@Override
					public void onStarted() {
						tvTitle.setText("Playing");
						ibPlay.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_pause));
					}
					
					@Override
					public void onPrepared() {
						tvTitle.setText("Waiting...");
					}
					
					@Override
					public void onInfo(int code, int extra) {
						if (code == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
							tvTitle.setText("Buffering...");
						}
						
						if (code == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
							tvTitle.setText("Playing");
						}
					}
					
					@Override
					public void onError(int what, int extra) {
						String msg = "Uhh... No idea what's wrong.";
						switch (what) {
						case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
							msg = "Server timed out"; break;
						case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
							msg = "Server no longer responding";
						case MediaPlayer.MEDIA_ERROR_MALFORMED:
							msg = "Malformed response";
						case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
							msg = "Media unsupported";
						case MediaPlayer.MEDIA_ERROR_IO:
							msg = "Couldn't reach server";
						case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
							msg = "Media cannot be streamed progressively";
						case MediaPlayer.MEDIA_ERROR_UNKNOWN:
							msg = "Unknown error";
						}
						
						tvTitle.setText(msg);
					}
					
					@Override
					public void onChanged() {
						tvTitle.setText("Changing...");
					}
					
					@Override
					public void onBuffer(int percent) {
						tvTitle.setText("Buffering... " + percent + "%");
					}
				};
				
				MainActivity.this.service.setPlayerStateListener(serviceListener);
			}
		};
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		if (service != null) {
			if (service.isPlaying()) {
				service.startForeground(99, NotificationBroker.getPlayingNotification(this, service.isPlaying()));
			}
		}
	}
	
	@Override
	public void onStop() {
		super.onStop();
		service.setPlayerStateListener(null);
	}

	/**
	 * Run when the service is first connected
	 */
	public void onFirstConnection() {
		if (service != null && curUrl != null) {
			service.startPlayer(curUrl);
		}
	}

}
