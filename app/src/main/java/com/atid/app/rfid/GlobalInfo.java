package com.atid.app.rfid;

import com.atid.lib.dev.rfid.type.TagType;

public class GlobalInfo {
	
	private static TagType smTagType = TagType.Tag6C;
	private static boolean smIsDisplayPc = true;
	private static boolean smIsContinuousMode = true;
	
	public static TagType getTagType() {
		return smTagType;
	}
	
	public static void setTagType(TagType type) {
		smTagType = type;
	}
	
	public static boolean isDisplayPc() {
		return smIsDisplayPc;
	}
	
	public static void setDisplayPc(boolean enabled) {
		smIsDisplayPc = enabled;
	}
	
	public static boolean isContinuousMode() {
		return smIsContinuousMode;
	}
	
	public static void setContinuousMode(boolean enabled) {
		smIsContinuousMode = enabled;
	}
}
