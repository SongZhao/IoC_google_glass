package com.patron.IoC;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.*;
/*
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
*/
//import java.util.UUID;
import java.util.*;


public class ClassicBluetoothServer extends Activity {

	public final static String TAG = "ClassicBluetoothServer";
	BluetoothAdapter mBluetoothAdapter;
	BluetoothServerSocket mBluetoothServerSocket;
	public static final int REQUEST_TO_START_BT = 100;
	public static final int REQUEST_FOR_SELF_DISCOVERY = 200;
	private TextView mTvInfo;

	UUID MY_UUID = UUID.fromString("D04E3068-E15B-4482-8306-4CABFA1726E7");

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mTvInfo = (TextView) findViewById(R.id.info);


		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        /*
        final BluetoothManager bluetoothManager =
				(BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();
		*/
		if (mBluetoothAdapter == null) {
			mTvInfo.setText("Device does not support Bluetooth");
			return;
		} else {
			if (!mBluetoothAdapter.isEnabled()) {
				mTvInfo.setText("Bluetooth supported but not enabled");
				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_TO_START_BT);
			} else {
				mTvInfo.setText("Bluetooth supported and enabled");				
				new AcceptThread().start();
			}
		}
	}

	private class AcceptThread extends Thread {
		private BluetoothServerSocket mServerSocket;

		public AcceptThread() {
			try {
				mServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("ClassicBluetoothServer", MY_UUID);
			} 
			catch (IOException e) {
				final IOException ex = e;
				runOnUiThread(new Runnable() {
					public void run() {
						mTvInfo.setText(ex.getMessage());
					}
				});
			}
		}

		public void run() {
			BluetoothSocket socket = null;
			// Keep listening until exception occurs or a socket is returned
			while (true) {
				try {
					runOnUiThread(new Runnable() {
						public void run() {
							mTvInfo.setText(mTvInfo.getText() + "\n\nWaiting for phone to connect ...");
						}
					});

					socket = mServerSocket.accept(); // blocking call

				} catch (IOException e) {
					Log.v(TAG, e.getMessage());
					break;
				}
				// If a connection was accepted
				if (socket != null) {
					// Do work in a separate thread
					new ConnectedThread(socket).start();

					try {
						mServerSocket.close();						
					} catch (IOException e) {
						Log.v(TAG, e.getMessage());
					}
					break;
				}
			}
		}
	}

	private class ConnectedThread extends Thread {
		private final BluetoothSocket mSocket;
		private final InputStream mInputStream;
        private String read_line;
		public ConnectedThread(BluetoothSocket socket) {
			mSocket = socket;
            InputStream is = null;
            read_line = null;
			try {
				is = socket.getInputStream();
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
			}
			mInputStream = is;
		}

		public void run() {

			if (mInputStream != null) {
                BufferedReader inFromPhone = new BufferedReader(new InputStreamReader(mInputStream));

				try {
					read_line = inFromPhone.readLine();
				} catch (IOException e) {
					Log.e(TAG, e.getMessage());
				}


				try {

					mSocket.close();
					runOnUiThread(new Runnable() {
						public void run() {
							mTvInfo.setText( read_line + " did I read something?\n"); // Glass
						}
					});                	

				} catch (IOException e) {
					Log.e(TAG, e.getMessage());
				}
			}

			new AcceptThread().start();				
		}
	}
}

