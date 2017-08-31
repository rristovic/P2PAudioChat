package com.radojcic.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.DefaultCaret;

import com.radojcic.networking.IClientListener;
import com.radojcic.networking.IClientListener.ClientChatListener;
import com.radojcic.networking.IMessageSender;
import com.radojcic.util.Messages;

public class MainConsole extends JFrame
		implements IClientListener.NewClientListener, IClientListener.MessageListener {

	// Variables declaration - do not modify
	private javax.swing.JButton btnExit;
	private javax.swing.JButton btnReconnect;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.JScrollPane jScrollPane2;
	private javax.swing.JTextArea mMessageWindow;
	private javax.swing.JList<String> mUserList;
	// End of variables declaration
	DefaultListModel<String> onlineUsers;

	// Message sender for sending messages to the server
	private IMessageSender msgSender;

	// New chat widnow field
	private ChatWindow chatWindow;
	private ClientChatListener chatEndListener;

	public MainConsole(IMessageSender msgSender) {
		super("Sukisa InstantMessenger");

		this.msgSender = msgSender;

		init();
	}

	/**
	 * Initialise variables
	 */
	private void init() {
		onlineUsers = new DefaultListModel<String>();

		initComponents();
		mUserList.setModel(onlineUsers);

		setListeners();
		setVisible(true);
	}

	/**
	 * Set action listeners onto fields
	 */
	private void setListeners() {
		mUserList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (mUserList.getSelectedIndex() == -1) {
				} else if(!e.getValueIsAdjusting()){
					msgSender.sendMessage(
							Messages.selectClientReqMsg(onlineUsers.getElementAt(mUserList.getSelectedIndex())));
				}
			}
		});
	}

	@Override
	public void onNewMessage(final String message) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				MainConsole.this.mMessageWindow.append(message + "\n");
			}
		});
	}

	public IClientListener.MessageListener onChatUserChosen(IMessageSender msgSender, String userName) {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					chatWindow = new ChatWindow(msgSender, userName);
					chatWindow.setLocationRelativeTo(MainConsole.this);

					MainConsole.this.setVisible(false);

					chatWindow.addWindowListener(new WindowAdapter() {
						@Override
						public void windowClosed(WindowEvent arg0) {
							// TODO Auto-generated method stub
							super.windowClosed(arg0);

							MainConsole.this.setVisible(true);
							MainConsole.this.chatEnded();
						}
					});
				}
			});
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return chatWindow;
	}

	@Override
	public IClientListener.MessageListener onNewChat(IMessageSender msgSender, String message,
			String chatBuddyName) {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					chatWindow = new ChatWindow(msgSender, chatBuddyName);
					chatWindow.setLocationRelativeTo(null);

					MainConsole.this.setVisible(false);

					chatWindow.addWindowListener(new WindowAdapter() {
						@Override
						public void windowClosed(WindowEvent arg0) {
							// TODO Auto-generated method stub
							super.windowClosed(arg0);

							MainConsole.this.setVisible(true);
							MainConsole.this.chatEnded();
						}
					});
				}
			});
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return chatWindow;
	}

	private void chatEnded() {
		if (this.chatEndListener != null)
			this.chatEndListener.onChatEnded();
	}

	public void setChatEndListener(IClientListener.ClientChatListener chatEndListener) {
		this.chatEndListener = chatEndListener;
	}
	
	public void setClients(List<String> clients) {
		this.onlineUsers.clear();
		for (String string : clients) {
			this.onlineUsers.addElement(string);
		}
	}
	
	public void addClient(String client) {
		this.onlineUsers.addElement(client);
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
		jScrollPane1 = new javax.swing.JScrollPane();
		mUserList = new javax.swing.JList<>();
		jScrollPane2 = new javax.swing.JScrollPane();
		mMessageWindow = new javax.swing.JTextArea();
		btnExit = new javax.swing.JButton();
		btnReconnect = new javax.swing.JButton();

		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

		mUserList.setModel(new javax.swing.AbstractListModel<String>() {
			String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };

			public int getSize() {
				return strings.length;
			}

			public String getElementAt(int i) {
				return strings[i];
			}
		});
		mUserList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
		jScrollPane1.setViewportView(mUserList);

		mMessageWindow.setEditable(false);
		mMessageWindow.setColumns(20);
		mMessageWindow.setLineWrap(true);
		mMessageWindow.setRows(5);
		jScrollPane2.setViewportView(mMessageWindow);

		btnExit.setText("Exit");
		btnExit.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnExitActionPerformed(evt);
			}
		});

		btnReconnect.setText("Refresh");
		btnReconnect.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnReconnectActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
		jPanel1.setLayout(jPanel1Layout);
		jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
						jPanel1Layout.createSequentialGroup().addContainerGap()
								.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
										.addComponent(btnReconnect, javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 342, Short.MAX_VALUE)
								.addComponent(btnExit, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
						.addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 151,
								javax.swing.GroupLayout.PREFERRED_SIZE).addContainerGap()));
		jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel1Layout.createSequentialGroup().addContainerGap()
						.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 262, Short.MAX_VALUE)
								.addGroup(jPanel1Layout.createSequentialGroup().addComponent(jScrollPane2)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
										.addComponent(btnReconnect, javax.swing.GroupLayout.PREFERRED_SIZE, 38,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
										.addComponent(btnExit, javax.swing.GroupLayout.PREFERRED_SIZE, 38,
												javax.swing.GroupLayout.PREFERRED_SIZE)))
						.addContainerGap()));

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(
				jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
		layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(
				jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));

		pack();
	}// </editor-fold>

	private void btnExitActionPerformed(java.awt.event.ActionEvent evt) {
		System.exit(0);
	}

	private void btnReconnectActionPerformed(java.awt.event.ActionEvent evt) {
		msgSender.sendMessage(Messages.GET_CLIENTS_REQ);
	}

	@Override
	public void onNewMessage(byte[] audioMessage) {
		throw new RuntimeException("Method not implemented.");
	}

}
