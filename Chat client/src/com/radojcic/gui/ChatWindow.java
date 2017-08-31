package com.radojcic.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.radojcic.networking.IClientListener;
import com.radojcic.networking.IMessageSender;
import com.radojcic.networking.error.ConnectionErrorException;
import com.radojcic.util.Messages;

public class ChatWindow extends JFrame implements IClientListener.MessageListener {

	// Variables declaration - do not modify
	private javax.swing.JPanel jPanel1;
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.JTextField mNewMsgArea;
	private javax.swing.JTextArea mMessagesWindow;
	private javax.swing.JButton mRecord;
	private javax.swing.JButton mSend;
	private javax.swing.JButton mStop;
	// End of variables declaration


	private IMessageSender msgSender;
	private String chatBuddyName;
	private boolean chatEnded;
	
	// Sound fields
	private TargetDataLine microfon;
	private byte[] buf;
	private ByteArrayOutputStream recordingOutputStream;
	private SourceDataLine speakers;
	private static final AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 16000.0f, 16, 1, 2,
			16000.0f, true);

	public ChatWindow(IMessageSender msgSender, String chatBuddy) {
		super("Chat room: " + chatBuddy);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		this.msgSender = msgSender;
		if (msgSender == null)
			dispose();
		this.chatBuddyName = chatBuddy;

		init();
	}

	/**
	 * Initialise variables
	 */
	private void init() {
		try {
			DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
			info = new DataLine.Info(TargetDataLine.class, format);
			microfon = (TargetDataLine) AudioSystem.getLine(info);
			
			info = new DataLine.Info(SourceDataLine.class, format);
			speakers = (SourceDataLine) AudioSystem.getLine(info);
			speakers.open(format, speakers.getBufferSize());
			speakers.start();
		} catch (LineUnavailableException ex) {
			return;
		}

		initComponents();
		setListener();
		setVisible(true);
	}

	/**
	 * Set action listeners onto fields
	 */
	private void setListener() {
		mNewMsgArea.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					msgSender.sendMessage(e.getActionCommand());
					mNewMsgArea.setText("");
				} catch (ConnectionErrorException ex) {
					mNewMsgArea.setText("Chat has been disconected.");
					mNewMsgArea.setEditable(false);
				}
			}
		});

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent e) {
				mNewMsgArea.requestFocus();
			}

			@Override
			public void windowClosed(WindowEvent arg0) {
				speakers.drain();
				speakers.close();
				microfon.close();
				try {
					recordingOutputStream.close();
				} catch (Exception e) {
				}
				
				if (!chatEnded) {
					chatEnded = true;
					try {
						ChatWindow.this.msgSender.sendMessage(Messages.CON_END_REQ);
					} catch (ConnectionErrorException ex) {
						System.out.println("Sending message failed: " + ex.getLocalizedMessage());
					}
				}
				super.windowClosed(arg0);
			}
		});
	}

	@Override
	public void onNewMessage(String message) {
		if (message.startsWith(Messages.CON_END_REQ)) {
			this.chatEnded = true;
			this.dispose();
			return;
		}

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				ChatWindow.this.mMessagesWindow.append(message + "\n");
			}
		});
	}
	
	@Override
	public void onNewMessage(byte[] audioMessage) {
		speakers.write(audioMessage, 0, audioMessage.length);
		speakers.drain();
	}

	private void btnRecordActionPerformed(ActionEvent evt) {
		try {
			recordingOutputStream = new ByteArrayOutputStream();
			microfon.open(format, microfon.getBufferSize());
			microfon.start();

			buf = new byte[1024];

			new Thread(new Runnable() {
				@Override
				public void run() {
					Thread.currentThread().setName("Recording-Thread");
					while (microfon.isOpen()) {
						int numOfBytesRead = microfon.read(buf, 0, 1024);
						recordingOutputStream.write(buf, 0, numOfBytesRead);
						speakers.write(buf, 0, buf.length);
						buf = new byte[1024];
					}					
				}
			}).start();
			

		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void btnStopActionPerformed(ActionEvent evt) {
		microfon.close();
//		try {
//			recordingOutputStream.close();
//		} catch (IOException e) {}
//		new Thread( new Runnable() {
//			public void run() {
//				Thread.currentThread().setName("Playback-Thread");
//				speakers.write(recordingOutputStream.toByteArray(), 0, recordingOutputStream.size());
//				
//			}
//		}).start();
	}

	private void btnSendActionPerformed(ActionEvent evt) {
		this.msgSender.sendSoundData(recordingOutputStream.toByteArray(), "");
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
	// <editor-fold defaultstate="collapsed" desc="Generated Code">
	private void initComponents() {

		jPanel1 = new javax.swing.JPanel();
		mRecord = new javax.swing.JButton();
		mSend = new javax.swing.JButton();
		mStop = new javax.swing.JButton();
		jScrollPane1 = new javax.swing.JScrollPane();
		mMessagesWindow = new javax.swing.JTextArea();
		mNewMsgArea = new javax.swing.JTextField();

		mRecord.setText("Snimi");

		mSend.setText("Posalji");

		mStop.setText("Stop");

		mMessagesWindow.setEditable(false);
		mMessagesWindow.setColumns(20);
		mMessagesWindow.setRows(5);
		jScrollPane1.setViewportView(mMessagesWindow);

		mNewMsgArea.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
			}
		});

		mRecord.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnRecordActionPerformed(evt);
			}
		});

		mStop.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnStopActionPerformed(evt);
			}
		});

		mSend.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnSendActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
		jPanel1.setLayout(jPanel1Layout);
		jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
						jPanel1Layout.createSequentialGroup().addContainerGap()
								.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
										.addComponent(mNewMsgArea)
										.addComponent(mSend, javax.swing.GroupLayout.Alignment.LEADING,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout
										.createSequentialGroup()
										.addComponent(mRecord, javax.swing.GroupLayout.DEFAULT_SIZE, 253,
												Short.MAX_VALUE)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
										.addComponent(mStop, javax.swing.GroupLayout.PREFERRED_SIZE, 137,
												javax.swing.GroupLayout.PREFERRED_SIZE))
										.addComponent(jScrollPane1))
								.addContainerGap()));
		jPanel1Layout
				.setVerticalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
								jPanel1Layout.createSequentialGroup().addContainerGap()
										.addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 176,
												Short.MAX_VALUE)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
										.addComponent(mNewMsgArea, javax.swing.GroupLayout.PREFERRED_SIZE, 53,
												javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
						.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(mRecord, javax.swing.GroupLayout.PREFERRED_SIZE, 41,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(mStop, javax.swing.GroupLayout.PREFERRED_SIZE, 41,
										javax.swing.GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addComponent(mSend,
								javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addContainerGap()));

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(
				jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
		layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(
				jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));

		pack();
	}// </editor-fold>
}
