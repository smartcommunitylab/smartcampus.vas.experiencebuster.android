package eu.trentorise.smartcampus.eb;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

public class VideoActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video);
		VideoView vv = (VideoView) findViewById(R.id.videoView1);
		MediaController mc = new MediaController(this);
		mc.setAnchorView(vv);
		mc.setMediaPlayer(vv);
		Uri video = Uri
				.parse("https://dl.dropboxusercontent.com/1/view/qn9wrbcxjhzsu5j/Apps/SC-lifelog/M_20131229_114818.mp4");
		vv.setMediaController(mc);
		vv.setVideoURI(video);
		vv.start();
	}
}
