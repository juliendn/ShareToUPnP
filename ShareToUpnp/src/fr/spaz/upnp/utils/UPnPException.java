package fr.spaz.upnp.utils;

public class UPnPException extends Exception
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UPnPException()
	{
		super();
	}

	public UPnPException(String detailMessage, Throwable throwable)
	{
		super(detailMessage, throwable);
	}

	public UPnPException(String detailMessage)
	{
		super(detailMessage);
	}

	public UPnPException(Throwable throwable)
	{
		super(throwable);
	}
	
}
