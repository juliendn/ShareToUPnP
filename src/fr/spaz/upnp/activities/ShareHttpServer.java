package fr.spaz.upnp.activities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.provider.MediaStore;
import android.text.format.Formatter;
import android.util.Log;

public class ShareHttpServer extends Service
{

	private static final String TAG = "ShareHttpServer";

	protected ShareHttpBinder binder = new ShareHttpBinder();

	private ServerSocket mServerSocket;
	private ListenTask mListenTask;
	private Uri mUri;
	private Object mLock;

	@Override
	public void onCreate()
	{
		super.onCreate();
		mLock = new Object();
		try
		{
			mServerSocket = new ServerSocket(0);
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}

	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();

	}

	@Override
	public IBinder onBind(Intent arg0)
	{
		return binder;
	}

	private boolean startServer()
	{
		if (null != mUri)
		{
			mListenTask = new ListenTask();
			mListenTask.execute();
			return true;
		}
		return false;
	}

	private boolean stopServer()
	{
		mListenTask.cancel(true);
		return false;
	}

	private String getServerUrl()
	{
		// final ConnectivityManager manager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
		// NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		// if(wifi.isConnected())
		// {
		// wifi.getExtraInfo().
		// }

		final WifiManager manager = (WifiManager) getSystemService(WIFI_SERVICE);
		int ip = manager.getConnectionInfo().getIpAddress();

		// return String.format("http://%s:%d/", mServerSocket.getInetAddress().getHostAddress(), mServerSocket.getLocalPort());
		return String.format("http://%s:%d/", Formatter.formatIpAddress(ip), mServerSocket.getLocalPort());
	}

	private boolean serServerUri(Uri uri)
	{
		synchronized (mLock)
		{
			mUri = uri;
		}
		return true;
	}

	public class ShareHttpBinder extends Binder
	{
		public String getUrl()
		{
			return getServerUrl();
		}

		public boolean start()
		{
			return startServer();
		}

		public boolean stop()
		{
			return stopServer();
		}

		public boolean serUri(Uri uri)
		{
			return serServerUri(uri);
		}
	}

	public class ListenTask extends AsyncTask<Void, Void, Void>
	{

		private static final int BUFFER_SIZE = 512;

		@Override
		protected Void doInBackground(Void... params)
		{
			Socket socket = null;
			String type = null;
			Uri uri = null;
			try
			{
				while (!isCancelled())
				{

					socket = mServerSocket.accept();

					synchronized (mLock)
					{
						uri = mUri;
					}

					// Get file path
					final String[] proj = {MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.MIME_TYPE};
					final Cursor cursor = getContentResolver().query(uri, proj, null, null, null);
					if (cursor.moveToFirst())
					{
						final String path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
						final String mimetype = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE));

						final File file = new File(path);

						final OutputStream out = socket.getOutputStream();

						if (null != file && file.exists())
						{
							final int contentLength = (int)file.length();

							final String resp = String.format("HTTP/1.1 200 OK\r\nContent-Type: %s\r\nContent-Length: %d\r\n\r\n", mimetype, contentLength);
							final int headerLength = resp.getBytes().length;

							final FileInputStream fis = new FileInputStream(file);

							byte[] buffer = Arrays.copyOf(resp.getBytes(), headerLength + contentLength);
							while (fis.read(buffer, headerLength, contentLength) > 0)
							{
								out.write(buffer);
//								Log.d(TAG, String.format("write %d bytes. total bytes: %d.", TOTAL, headerLength + contentLength));
							}

//							byte[] buffer = new byte[BUFFER_SIZE];
//							while ((count = fis.read(buffer, 0, BUFFER_SIZE)) > 0)
//							{
//								total += count;
//
//								out.write(buffer);
//								Log.d(TAG, String.format("write %d bytes. total bytes: %d.", total, length));
//							}
							fis.close();
						}
						else
						{
							final String resp = "HTTP/1.1 404 Not found\r\n\r\n";
							out.write(resp.getBytes());
						}
						out.close();
					}

					// final FileInputStream fr = new FileInputStream(file);
					// fr.
					//
					// if (type.startsWith("image"))
					// {
					//
					// // File f = new File()
					//
					// final Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
					// // Add headers
					// bitmap.compress(CompressFormat.JPEG, 50, out);
					// // bitmap.compress(CompressFormat.JPEG, 100, out);
					// }
					// else if (type.startsWith("video"))
					// {
					// // MediaStore.Video.Media.DATA
					// final Cursor cursor = MediaStore.Video.query(getContentResolver(), uri, new String[]{MediaStore.Video.Media.DATA});
					// String Data = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
					//
					// }
					// else if (type.startsWith("audio"))
					// {
					//
					// }
					// else
					// {
					// throw new ShareMimetypeException();
					// }

					socket.close();

				}
			}
			catch (IOException ioe)
			{
				ioe.printStackTrace();
			}

			return null;
		}

	}

}
