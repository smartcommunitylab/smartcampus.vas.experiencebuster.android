/*******************************************************************************
 * Copyright 2012-2013 Trento RISE
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either   express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package eu.trentorise.smartcampus.eb.custom;

import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import eu.trentorise.smartcampus.eb.R;
import eu.trentorise.smartcampus.eb.custom.capture.ResourceHandler;

public class EmbeddedMediaPlayer implements OnCompletionListener, SeekBar.OnSeekBarChangeListener, ResourceHandler {

	private View mc;
	private ImageView play;
	private ImageView rew;
	private ImageView ff;
	private SeekBar seek;
	// Media Player
    private  MediaPlayer mp;
    // Handler to update UI timer, progress bar etc,.
    private Handler mHandler = new Handler();

    private int seekForwardTime = 5000; // 5000 milliseconds
    private int seekBackwardTime = 5000; // 5000 milliseconds
	
    private String fileUri;
    private boolean started = false;
    
	public EmbeddedMediaPlayer(ViewGroup parent, String fileUri) {
		super();
		this.fileUri = fileUri;
		mc = ((LayoutInflater)parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.media_ctrl, null);
		play = (ImageView)mc.findViewById(R.id.media_play);
		ff = (ImageView)mc.findViewById(R.id.media_ff);
		rew = (ImageView)mc.findViewById(R.id.media_rew);
		seek = (SeekBar)mc.findViewById(R.id.media_seek);
		seek.setOnSeekBarChangeListener(this);
		 ff.setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View arg0) {
	                // get current song position
	                int currentPosition = mp.getCurrentPosition();
	                // check if seekForward time is lesser than song duration
	                if(currentPosition + seekForwardTime <= mp.getDuration()){
	                    // forward song
	                    mp.seekTo(currentPosition + seekForwardTime);
	                }else{
	                    // forward to end position
	                    mp.seekTo(mp.getDuration());
	                }
	            }
	        });
		 
		 rew.setOnClickListener(new View.OnClickListener() {
			 
	            @Override
	            public void onClick(View arg0) {
	                // get current song position
	                int currentPosition = mp.getCurrentPosition();
	                // check if seekBackward time is greater than 0 sec
	                if(currentPosition - seekBackwardTime >= 0){
	                    // forward song
	                    mp.seekTo(currentPosition - seekBackwardTime);
	                }else{
	                    // backward to starting position
	                    mp.seekTo(0);
	                }
	 
	            }
	        });
		 
		 play.setOnClickListener(new View.OnClickListener() {
			 
	            @Override
	            public void onClick(View arg0) {
	                // check for already playing
	                if(mp != null && mp.isPlaying()){
	                    if(mp!=null){
	                        mp.pause();
	                        // Changing button image to play button
	                        play.setImageResource(R.drawable.ic_media_play);
	                    }
	                }else{
		            	if (!started) {
		            		started = true;
		            		playFile();
		            	} else {
		                    // Resume song
	                        mp.start();
		            	}
                        // Changing button image to pause button
                        play.setImageResource(R.drawable.ic_media_pause);
	                }
	 
	            }
	        });

		 mp = new MediaPlayer();
		 mp.setOnCompletionListener(this);
	}
	
	public View getView() {
		return mc;
	}

	public void release() {
		if (mp != null) {
			mp.release();
			mp = null;
		}
	}
	
	private void playFile() {
		try {
        	mp.reset();
            mp.setDataSource(fileUri);
            mp.prepare();
            mp.start();
            // Changing Button Image to pause image
            play.setImageResource(R.drawable.ic_media_pause);
 
            // set Progress bar values
            seek.setProgress(0);
            seek.setMax(100);
 
            // Updating progress bar
            updateProgressBar();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            if (Utils.isRemote(fileUri)) {
                mc.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(fileUri)));
            }
        }
	}
	
	private void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    } 
	/**
     * Function to get Progress percentage
     * @param currentDuration
     * @param totalDuration
     * */
    private int getProgressPercentage(long currentDuration, long totalDuration){
        Double percentage = (double) 0;
 
        long currentSeconds = (int) (currentDuration / 1000);
        long totalSeconds = (int) (totalDuration / 1000);
 
        // calculating percentage
        percentage =(((double)currentSeconds)/totalSeconds)*100;
 
        // return percentage
        return percentage.intValue();
    }
 
    /**
     * Function to change progress to timer
     * @param progress -
     * @param totalDuration
     * returns current duration in milliseconds
     * */
    private int progressToTimer(int progress, int totalDuration) {
        int currentDuration = 0;
        totalDuration = (int) (totalDuration / 1000);
        currentDuration = (int) ((((double)progress) / 100) * totalDuration);
 
        // return current duration in milliseconds
        return currentDuration * 1000;
    }
    
    /**
     * Background Runnable thread
     * */
    private Runnable mUpdateTimeTask = new Runnable() {
           public void run() {
               try {
				long totalDuration = mp.getDuration();
				   long currentDuration = mp.getCurrentPosition();
				   // Updating progress bar
				   int progress = (int)(getProgressPercentage(currentDuration, totalDuration));
				   //Log.d("Progress", ""+progress);
				   seek.setProgress(progress);
				   // Running this thread after 100 milliseconds
				   mHandler.postDelayed(this, 100);
			} catch (Exception e) {
				return;
			}
           }
        };

	@Override
	public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
		try {
			mHandler.removeCallbacks(mUpdateTimeTask);
			int totalDuration = mp.getDuration();
			int currentPosition = progressToTimer(arg0.getProgress(), totalDuration);
			// forward or backward to certain seconds
			mp.seekTo(currentPosition);
			// update timer progress again
			updateProgressBar();
		} catch (IllegalStateException e) {
			return;
		}
    }

	@Override
	public void onStopTrackingTouch(SeekBar arg0) {
		 mHandler.removeCallbacks(mUpdateTimeTask);		
	}
        
        
	@Override
    public void onCompletion(MediaPlayer arg0) {
		started = false;
        play.setImageResource(R.drawable.ic_media_play);
    }

	public void init() {
		if (mp == null) {
			mp = new MediaPlayer();
			mp.setOnCompletionListener(this);
		}
	}
}
