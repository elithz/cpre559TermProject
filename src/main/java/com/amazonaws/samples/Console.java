package com.amazonaws.samples;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;

/***
 * 
 * Console.java
 * 
 * @author elith, daiyuan
 * @version 2.1 NERVE Software 2019/5/10
 *
 */

public class Console {

	public static JFrame frmConsole;
	private final Action action = new SwingAction();
	private final Action action_1 = new SwingAction_1();
	private final Action action_2 = new SwingAction_2();

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Console.frmConsole.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public Console() {
		initialize();
	}

	private void initialize() {
		frmConsole = new JFrame();
		frmConsole.setTitle("Main Console");
		frmConsole.setBounds(100, 100, 450, 300);
		frmConsole.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmConsole.getContentPane().setLayout(null);

		// create three function buttons
		JButton btnNewButton = new JButton("EC2");
		btnNewButton.setAction(action);
		btnNewButton.setBounds(160, 27, 93, 48);
		frmConsole.getContentPane().add(btnNewButton);

		JButton btnNewButton_1 = new JButton("S3");
		btnNewButton_1.setAction(action_1);
		btnNewButton_1.setBounds(160, 96, 93, 48);
		frmConsole.getContentPane().add(btnNewButton_1);

		JButton btnNewButton_2 = new JButton("VPC");
		btnNewButton_2.setAction(action_2);
		btnNewButton_2.setBounds(160, 165, 93, 48);
		frmConsole.getContentPane().add(btnNewButton_2);
	}

	// set actions for the three buttons
	private class SwingAction extends AbstractAction {
		public SwingAction() {
			putValue(NAME, "EC2");
			putValue(SHORT_DESCRIPTION, "Some short description");
		}

		public void actionPerformed(ActionEvent e) {
			EC2Console ec2console = new EC2Console();
			ec2console.main(null);
			frmConsole.setEnabled(false);
		}
	}

	private class SwingAction_1 extends AbstractAction {
		public SwingAction_1() {
			putValue(NAME, "S3");
			putValue(SHORT_DESCRIPTION, "Some short description");
		}

		public void actionPerformed(ActionEvent e) {
			S3Console s3console = new S3Console();
			s3console.main(null);
			frmConsole.setEnabled(false);
		}
	}

	private class SwingAction_2 extends AbstractAction {
		public SwingAction_2() {
			putValue(NAME, "VPC");
			putValue(SHORT_DESCRIPTION, "Some short description");
		}

		public void actionPerformed(ActionEvent e) {
			VPCConsole vpcconsole = new VPCConsole();
			vpcconsole.main(null);
			frmConsole.setEnabled(false);
		}
	}
}
