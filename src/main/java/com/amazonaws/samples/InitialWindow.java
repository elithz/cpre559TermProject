package com.amazonaws.samples;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import java.awt.Font;

public class InitialWindow {

	private JFrame frmHi;
	private JTextField textField;
	private JTextField textField_1;
	public BasicAWSCredentials user_credential = null;
	public AmazonS3 s3Client = null;
	public static String OS_NAME = System.getProperty("os.name");

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		System.out.println(OS_NAME);
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					InitialWindow window = new InitialWindow();
					window.frmHi.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public InitialWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmHi = new JFrame();
		frmHi.setTitle("AWS Simple GUI Tool");
		frmHi.setBounds(100, 100, 450, 300);
		frmHi.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel panel = new JPanel();
		frmHi.getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(null);

		textField = new JTextField();
		textField.setBounds(182, 135, 66, 21);
		panel.add(textField);
		textField.setColumns(10);

		textField_1 = new JTextField();
		textField_1.setColumns(10);
		textField_1.setBounds(182, 80, 66, 21);
		panel.add(textField_1);

		JButton btnNewButton = new JButton("Log in");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.out.println(textField.getText() + "   " + textField_1.getText());
				user_credential = new BasicAWSCredentials(textField.getText(), textField_1.getText());
				try {
					s3Client = AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_2)
							.withCredentials(new AWSStaticCredentialsProvider(user_credential)).build();
				} catch (Exception e) {
					throw new AmazonClientException("Credential is not valid, please check with your admin.", e);
				}
			}
		});
		btnNewButton.setBounds(171, 203, 93, 23);
		panel.add(btnNewButton);

		JLabel lblWelcomeToAws = new JLabel("Welcome to AWS Simple GUI Tool");
		lblWelcomeToAws.setFont(new Font("SimSun", Font.PLAIN, 14));
		lblWelcomeToAws.setHorizontalAlignment(SwingConstants.CENTER);
		lblWelcomeToAws.setBounds(99, 10, 238, 31);
		panel.add(lblWelcomeToAws);

		JLabel lblIamAccessKey = new JLabel("IAM Access Key");
		lblIamAccessKey.setBounds(47, 83, 84, 15);
		panel.add(lblIamAccessKey);

		JLabel lblIamSecretAccess = new JLabel("IAM Secret Access Key");
		lblIamSecretAccess.setBounds(26, 138, 146, 15);
		panel.add(lblIamSecretAccess);
	}

}
