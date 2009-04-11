package edu.virginia.cs.storagedesk.storageserver;

public class VersionNotMatchException extends Exception{
	/**
	 * 
	 */
	private static final long serialVersionUID = -112265594652916615L;

	public VersionNotMatchException () {
		super("Versions of physical and virtual chunks do not match");
	}

}
