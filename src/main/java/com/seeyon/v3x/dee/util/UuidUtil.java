package com.seeyon.v3x.dee.util;

import java.util.UUID;

public class UuidUtil {
	public static String uuid(){
		return String.valueOf(UUID.randomUUID().getMostSignificantBits());
	}

}
