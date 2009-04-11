package edu.virginia.cs.storagedesk.storageserver;


import java.io.IOException;

public interface IResponse {
	public byte[] encap() throws IOException;
}
