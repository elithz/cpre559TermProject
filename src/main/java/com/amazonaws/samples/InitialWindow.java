package com.amazonaws.samples;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.util.List;
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
import com.amazonaws.services.s3.model.Bucket;

import java.awt.Font;
import javax.swing.JList;
import java.awt.Choice;

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
		textField.setBounds(182, 82, 66, 21);
		panel.add(textField);
		textField.setColumns(10);

		textField_1 = new JTextField();
		textField_1.setColumns(10);
		textField_1.setBounds(182, 51, 66, 21);
		panel.add(textField_1);
		
		Choice choice = new Choice();
		choice.setBounds(182, 124, 66, 21);
		choice.addItem("us-east-1");
		choice.addItem("us-east-2");
		choice.addItem("us-west-1");
		choice.addItem("us-west-2");
		panel.add(choice);

		JButton btnNewButton = new JButton("Log in");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.out.println(textField_1.getText() + "   " + textField.getText());
				user_credential = new BasicAWSCredentials(textField_1.getText(), textField.getText());
				try {
					s3Client = AmazonS3ClientBuilder.standard().withRegion(choice.getSelectedItem())
							.withCredentials(new AWSStaticCredentialsProvider(user_credential)).build();
					try {
						List<Bucket> buckets = s3Client.listBuckets();
						System.out.println(buckets.get(1).getName());
						Console console = new Console();
						console.main(null);
						frmHi.setVisible(false);
					} catch (Exception e) {
						JFrame aframe = new JFrame();
						aframe.setVisible(true);
						aframe.setName("a test window");
						aframe.setTitle("Hi");
						aframe.setBounds(100, 100, 450, 300);
						aframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
						throw new AmazonClientException("Credential is not valid, please check with your admin.", e);
					}
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
		lblIamAccessKey.setBounds(40, 54, 84, 15);
		panel.add(lblIamAccessKey);

		JLabel lblIamSecretAccess = new JLabel("IAM Secret Access Key");
		lblIamSecretAccess.setBounds(26, 85, 146, 15);
		panel.add(lblIamSecretAccess);
		
		JLabel lblRegion = new JLabel("Region");
		lblRegion.setBounds(54, 124, 54, 15);
		panel.add(lblRegion);
	}
}
