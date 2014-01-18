package com.flamingo.service;

import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;

/*
 *  Copyright 2014 Chris Eggison
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
 * Service which contains a MediaPlayer to keep a streaming media operation alive
 * 
 * @author Chris Eggison (@50hzgamer)
 *
 */
public class PlayerService extends Service {
	private MediaPlayer mediaPlayer;
	private OnMediaPlayerStateChangeListener listener;
	private boolean isPrepared = false;
	private Uri previousUri;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mediaPlayer != null) {
			mediaPlayer.release();
		}
		stopForeground(true);
	}
	
	/**
	 * Starts the media player with the given URL
	 * @param url		URL to stream
	 */
	public void startPlayer(String url) {
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			mediaPlayer.release();
		}
		
		isPrepared = false;
		Uri uri = Uri.parse(url);
		previousUri = uri;
		
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				if (listener != null) {
					listener.onPrepared();
				}
				isPrepared = true;
				startStream();
			}
		});
		mediaPlayer.setOnErrorListener(new OnErrorListener() {
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				if (listener != null) {
					listener.onError(what, extra);
				}
				return true;
			}
		});
		mediaPlayer.setOnBufferingUpdateListener(new OnBufferingUpdateListener() {
			@Override
			public void onBufferingUpdate(MediaPlayer mp, int percent) {
				if (listener != null) {
					listener.onBuffer(percent);
				}
			}
		});
		mediaPlayer.setOnInfoListener(new OnInfoListener() {
			@Override
			public boolean onInfo(MediaPlayer arg0, int what, int extra) {
				if (listener != null) {
					listener.onInfo(what, extra);
				}
				return false;
			}
		});
		try {
			mediaPlayer.setDataSource(getApplicationContext(), uri);
			mediaPlayer.prepareAsync();
			if (listener != null) {
				listener.onChanged();
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * If the stream dies, restart the player with the previous URI the player was just playing.
	 */
	public void restartStream() {
		if (previousUri != null) {
			startPlayer(previousUri.toString());
		}
	}
	
	/**
	 * Starts Stream playback
	 */
	public void startStream() {
		if (!isPrepared) return;
		
		if (mediaPlayer != null) {
			if (!mediaPlayer.isPlaying()) {
				mediaPlayer.start();
				
				if (listener != null) {
					listener.onStarted();
				}
			}
		}
	}
	
	/**
	 * Stops Stream playback
	 */
	public void stopStream() {
		if (!isPrepared) return;
		
		if (mediaPlayer != null) {
			if (mediaPlayer.isPlaying()) {
				mediaPlayer.pause();
				
				if (listener != null) {
					listener.onStopped();
				}
			}
		}
	}
	
	public boolean isPlaying() {
		if (mediaPlayer != null) {
			return mediaPlayer.isPlaying();
		}
		
		return false;
	}
	
	
	
	public OnMediaPlayerStateChangeListener getPlayerStateListener() {
		return listener;
	}

	public void setPlayerStateListener(OnMediaPlayerStateChangeListener listener) {
		this.listener = listener;
	}



	public class PlayerServiceBinder extends Binder {
		public PlayerService getService() {
			return PlayerService.this;
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return new PlayerServiceBinder();
	}
	
	/**
	 * Listener for various MediaPlayer functions which can be accessed in a safe manner
	 * 
	 * @author Chris Eggison (@50hzgamer)
	 *
	 */
	public static abstract class OnMediaPlayerStateChangeListener {
		/** Called when the Media Player has been prepared. **/
		public abstract void onPrepared();
		/** Called when media playback has been paused **/
		public abstract void onStopped();
		/** Called when media playback has been started **/
		public abstract void onStarted();
		/** Called when the currently playing media item has been replaced **/
		public abstract void onChanged();
		/**
		 * Called when media playback encounters an error
		 * @param what		An error, use MediaPlayer.MEDIA_ERROR_... for possible reported options
		 * @param extra		Extra info, context dependent
		 */
		public abstract void onError(int what, int extra);
		/** Called when media is buffering **/
		public abstract void onBuffer(int percent);
		/**
		 * Called when the MediaPlayer offers some information as to what it's doing
		 * @param code		An information code, use MediaPlayer.MEDIA_INFO... for possible reported options
		 * @param extra		Extra info, context dependent
		 */
		public abstract void onInfo(int code, int extra);
	}

}
