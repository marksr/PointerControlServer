package br.com.marksr.pointercontrol;

import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import br.com.marksr.pointercontrol.commons.GenericPublisher;
import br.com.marksr.pointercontrol.commons.PointerCommand;
import br.com.marksr.pointercontrol.commons.PointerCommandAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CommandsServer {
    private static final String TAG = CommandsServer.class.getSimpleName();
    private ServerSocket mSocket;
    private MyAsyncTask mTask;
    private ConcurrentLinkedQueue<PointerCommand> mQueue;
    private GenericPublisher<PointerCommand, String> mCommands;
    private int mServerPort;
    private final int mTimeout = 10000;


    public CommandsServer(int serverPort) {
        mServerPort = serverPort;
        mQueue = new ConcurrentLinkedQueue<>();
        mCommands = new GenericPublisher<PointerCommand, String>() {
            @Override
            public void action(PointerCommand c, String message) {
                if (!message.isEmpty() && c.getType() == message.charAt(0)) {
                    c.process(message);
                }
            }
        };
    }

    public void start() {
        Log.d(TAG, "start begin");
        if (null == mTask) {
            mTask = new MyAsyncTask();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                mTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mServerPort);
            } else {
                mTask.execute(mServerPort);
            }
            Log.d(TAG, "start execute");
        }
        Log.d(TAG, "start end");
    }

    public void stop() {
        Log.d(TAG, "stop begin");
        if (null != mTask) {
            mTask.setActive(false);
            mTask = null;
            Log.d(TAG, "stop stop");
        }
        Log.d(TAG, "stop end");
    }

    public void registerCommand(PointerCommand command) {
        mCommands.setOnEvent(command);
    }

    public void unregisterCommand(PointerCommand command) {
        mCommands.removeOnEvent(command);
    }

    public void sendCommand(PointerCommand command) {
        mQueue.add(command);
    }

    private void dispatchCommands(String message) {
        mCommands.fire(message);
    }

    //Inner Classes
    private class MyAsyncTask extends AsyncTask<Integer, String, Void> {
        private boolean mActive = true;
        private final PointerCommand ping = new PointerCommandAdapter("a,ping\n");

        public boolean isActive() {
            return mActive;
        }

        public void setActive(boolean mActive) {
            this.mActive = mActive;
        }

        @Override
        protected Void doInBackground(Integer... params) {
            try {
                mSocket = new ServerSocket(params[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }

            while (mActive) {
                Socket client = null;
                OutputStream os = null;
                BufferedReader in = null;
                try {
                    Log.d(TAG, "Socket waiting new client");
                    client = mSocket.accept();
                    Log.d(TAG, "Socket client accepted");
                    Log.d(TAG, "Socket creaning streams");
                    os = client.getOutputStream();
                    in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                } catch (IOException e) {
                    e.printStackTrace();
                }


                int localTimeout = 0;
                boolean pinged = false;

                if (null != client && null != os && null != in) {
                    while (mActive && localTimeout < mTimeout) {
                        PointerCommand command = mQueue.poll();

                        if (null != command) {
                            try {
                                os.write(command.execute().getBytes());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        try {
                            if (in.ready()) {
                                String input = in.readLine();

                                if (input.equals("x")) {
                                    localTimeout = mTimeout;
                                    pinged = true;
                                } else {
                                    publishProgress(input);
                                    localTimeout = 0;
                                    pinged = false;
                                }
                            } else {
                                Thread.sleep(10, 0);
                                localTimeout += 10;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        if (!pinged && localTimeout >= mTimeout/2) {
                            try {
                                os.write(ping.execute().getBytes());
                                pinged = true;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    try {
                        if (!client.isClosed())
                            client.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG, "Socket stoped");
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            dispatchCommands(values[0]);
        }
    }

}
