package edu.virginia.cs.storagedesk.storageserver;


import java.io.IOException;

public interface IRequest {
	public boolean decap(byte[] header) throws IOException;
}
