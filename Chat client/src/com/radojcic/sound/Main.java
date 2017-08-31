/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.radojcic.sound;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

/**
 *
 * @author Ivan
 */
public class Main {

    public static final AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 16000.0f, 16, 1, 2, 16000.0f, true);

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            SourceDataLine zvucnici = (SourceDataLine) AudioSystem.getLine(info);
            zvucnici.open(format, zvucnici.getBufferSize());
            zvucnici.start();

            info = new DataLine.Info(TargetDataLine.class, format);
            TargetDataLine mikrofon = (TargetDataLine) AudioSystem.getLine(info);
            mikrofon.open(format, mikrofon.getBufferSize());
            mikrofon.start();

            SlanjeZvuka slanjeZvuka = new SlanjeZvuka(mikrofon, format, zvucnici);
            Thread t = new Thread(slanjeZvuka);
            t.start();
        } catch (LineUnavailableException ex) {
            return;
        }

    }
}
