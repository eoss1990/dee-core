package com.seeyon.v3x.dee;

/**
 * 转换异常。
 */
public class TransformException extends Exception {
    /**
	 * 
	 */
	private static final long serialVersionUID = 8701847765139562536L;

	public TransformException() {
        super();    //To change body of overridden methods use File | Settings | File Templates.
    }

    public TransformException(String s) {
        super(s);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public TransformException(String s, Throwable throwable) {
        super(s, throwable);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public TransformException(Throwable throwable) {
        super(throwable);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
