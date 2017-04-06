package com.seeyon.v3x.dee.adapter.webservice.addition;

import org.apache.ws.security.WSPasswordCallback;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.IOException;

public class ClientSecurityHandler implements CallbackHandler {

	public void handle(Callback[] callbacks) throws IOException,UnsupportedCallbackException {
		WSPasswordCallback callback = (WSPasswordCallback)callbacks[0];
		String id = callback.getIdentifier();
		int i = id.indexOf(",");
		String username = id.substring(0, i);
		String password = id.substring(i+1, id.length());
		callback.setIdentifier(username);
		callback.setPassword(password);
	}

}
