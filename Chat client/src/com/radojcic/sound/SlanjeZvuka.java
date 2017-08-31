package com.radojcic.sound;

import java.io.ByteArrayInputStream;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author PC
 */
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

public class SlanjeZvuka extends Thread {

    private TargetDataLine mikrofon;
    private SourceDataLine zvucnici;
    private AudioFormat format;

    public SlanjeZvuka(TargetDataLine mikrofon, AudioFormat format, SourceDataLine zvucnici) {
        this.mikrofon = mikrofon;
        this.zvucnici = zvucnici;
        this.format = format;

    }

    @Override
    public void run() {

        try {
            mikrofon.open(format);
            mikrofon.start();

            byte[] buf;
            buf = new byte[1024];



            while (mikrofon.isOpen()) {
                mikrofon.read(buf, 0, 1024);
                zvucnici.write(buf, 0, buf.length);
                buf = new byte[1024];
            }
            
            ByteArrayInputStream stream = new ByteArrayInputStream(buf);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
