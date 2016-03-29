package com.atid.app.rfid.util;

import com.atid.lib.dev.rfid.type.MaskActionType;
import com.atid.lib.dev.rfid.type.MaskTargetType;

public class StringUtil {
	
	public static String toString(MaskActionType type) {
		switch (type) {
		case Assert_Deassert: return "assert SL or inventoried -> A\r\ndeassert SL or inventoried -> B";
		case Assert_DoNothing: return "assert SL or inventoried -> A\r\ndo nothing";
		case DoNothing_Deassert: return "do nothing\r\ndeassert SL or inventoried -> B";
		case Negate_DoNothing: return "negate SL or (A -> B, B -> A)\r\ndo nothing";
		case Deassert_Assert: return "deassert SL or inventoried -> B\r\nassert SL or inventoried -> A";
		case Deassert_DoNothing: return "deassert SL or inventoried -> B\r\ndo nothing";
		case DoNothing_Assert: return "do nothing\r\nassert SL or inventoried -> A";
		case DoNothing_Negate: return "do nothing\r\nnegate SL or (A -> B, B -> A)";
		}
		return "";
	}
	
	public static String toString(MaskTargetType type) {
		switch (type) {
		case S0: return "Session 0";
		case S1: return "Session 1";
		case S2: return "Session 2";
		case S3: return "Session 3";
		case SL: return "Session Flag";
		}
		return "";
	}
}
