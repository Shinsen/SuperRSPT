package com.flamingo.rsptstreamer;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

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
 * Notification methods
 * 
 * @author Chris
 *
 */
public class NotificationBroker {
	public static Notification getPlayingNotification(Context c, boolean isPlaying) {
		Notification out = null;
		
		String text = c.getString(R.string.app_name);
		
		Intent launchMain = new Intent(c, MainActivity.class);
		launchMain.putExtra("_showinfo", true);
		PendingIntent pendingMain = PendingIntent.getActivity(c, 0, launchMain, 0);
		
		out = new NotificationCompat.Builder(c)
				.setContentTitle("Playing Audio")
				.setContentText(text)
				.setSmallIcon(R.drawable.ic_action_play_black)
				.setContentIntent(pendingMain)
				.build();
		
		return out;
	}
}
