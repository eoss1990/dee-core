package com.seeyon.v3x.dee.function;

import java.util.UUID;

/**
 *  UUID函数，生成一个长整型的唯一ID。
 */
public class UUIDTag extends Tag {

	@Override
	public Object execute() {
		return UUID.randomUUID().getMostSignificantBits();
	}

}
