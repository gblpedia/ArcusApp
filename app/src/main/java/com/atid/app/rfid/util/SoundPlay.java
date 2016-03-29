package com.atid.app.rfid.util;

import au.com.adilamtech.app.arcus.R;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

public class SoundPlay {

	private SoundPool mSound;
	private int mSuccess;
	private int mFail;
	
	public SoundPlay(Context context) {
		mSound = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
		mSuccess = mSound.load(context, R.raw.success, 1);
		mFail = mSound.load(context, R.raw.fail, 1);
	}
	
	public void playSuccess() {
		mSound.play(mSuccess, 1, 1, 0, 0, 1);
	}
	
	public void playFail() {
		mSound.play(mFail, 1, 1, 0, 0, 1);
	}
}
