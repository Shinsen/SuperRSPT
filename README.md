SuperRSPT
=========

**Simple Android RSPT media player**

------

Was a small project to fix a shortfall in built-in Android functionality towards streaming audio. Sites like Mixlr surface their RSTP streams if you have absolutely no other means of playing back their audio streams, the Android Video Player or Google+ Photos app will playback these streams, however it expects RSPT to be a video and thus, would not be useless to the user if the Video Player app was no longer in the foreground.

This app will intercept browser Intents towards rspt:// URLs and play them back with a minimal user interface to support this. The primary UI is a dialog with a play/pause control and a "quit" button. If the dialog is dismissed, a Notification is displayed (happy side effect of using startForeground on the service) which re-launches the UI dialog. It's very basic.

I offer this code as a hopeful good base for anyone who needs any example code for streaming music with the Android MediaPlayer or using a MediaPlayer in a Service. Although I will warn you that this implementation is slightly over-engineered for convenience.

------

Known bugs:
 - Play/pause button doesn't refresh correctly when starting a dialog when the Service has already been setup
 - Rotating a device while the dialog is showing will cause the service to recreate the MediaPlayer object and thus, restart any streams playing.
 - Sometimes RSPT streams will enter into buffering and never leave. *Strange bug, uncertain if it's a MediaPlayer issue.*
