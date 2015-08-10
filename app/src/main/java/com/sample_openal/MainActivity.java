package com.sample_openal;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.urish.openal.ALException;
import org.urish.openal.Buffer;
import org.urish.openal.OpenAL;
import org.urish.openal.Source;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        OpenAL openal = null;
        try {
            openal = new OpenAL();
        } catch (ALException e) {
            e.printStackTrace();
        }
        Source source = null;
        Buffer buffer = null;

        try {
            source = openal.createSource();
            buffer = openal.createBuffer();
            buffer.addBufferData();
            source.setBuffer(buffer);
            source.play();

        } catch (ALException e) {
            e.printStackTrace();
        }

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private static final int HEADER_SIZE = 44;

    private static final String CHARSET = "ASCII";

    private static final String TAG = "Sample";

        /* ... */

    public static WavInfo readHeader(InputStream wavStream)
            throws IOException, DecoderException {

        ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        wavStream.read(buffer.array(), buffer.arrayOffset(), buffer.capacity());

        buffer.rewind();
        buffer.position(buffer.position() + 20);
        int format = buffer.getShort();
        checkFormat(format == 1, "Unsupported encoding: " + format); // 1 means Linear PCM
        int channels = buffer.getShort();
        checkFormat(channels == 1 || channels == 2, "Unsupported channels: " + channels);
        int rate = buffer.getInt();
        checkFormat(rate <= 48000 && rate >= 11025, "Unsupported rate: " + rate);
        buffer.position(buffer.position() + 6);
        int bits = buffer.getShort();
        checkFormat(bits == 16, "Unsupported bits: " + bits);
        int dataSize = 0;
        while (buffer.getInt() != 0x61746164) { // "data" marker
            Log.d(TAG, "Skipping non-data chunk");
            int size = buffer.getInt();
            wavStream.skip(size);

            buffer.rewind();
            wavStream.read(buffer.array(), buffer.arrayOffset(), 8);
            buffer.rewind();
        }
        dataSize = buffer.getInt();
        checkFormat(dataSize > 0, "wrong datasize: " + dataSize);

        return new WavInfo(new FormatSpec(rate, channels == 2), dataSize);
    }
}
