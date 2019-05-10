package com.amazonaws.samples;

import java.awt.BorderLayout;
import java.awt.Choice;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.text.DefaultEditorKit;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancing;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;

/***
 * 
 * InitialWindow.java
 * 
 * @author elith, daiyuan
 * @version 2.1 NERVE Software 2019/5/10
 *
 */

public class InitialWindow {

	private JFrame frmHi;
	private JPasswordField textField;
	private JPasswordField textField_1;
	public BasicAWSCredentials user_credential = null;
	public static AmazonS3 s3Client = null;
	public static AmazonEC2 ec2Client = null;
	public static AmazonElasticLoadBalancing elbClient = null;
	public static String OS_NAME = System.getProperty("os.name");

	// start the frame of log in
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

	public InitialWindow() {
		initialize();
	}

	private void initialize() {
		// add right click options to text fields with copy cut and paste
		JPopupMenu menu = new JPopupMenu();
		Action cut = new DefaultEditorKit.CutAction();
		cut.putValue(Action.NAME, "Cut");
		cut.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control X"));
		menu.add(cut);

		Action copy = new DefaultEditorKit.CopyAction();
		copy.putValue(Action.NAME, "Copy");
		copy.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control C"));
		menu.add(copy);

		Action paste = new DefaultEditorKit.PasteAction();
		paste.putValue(Action.NAME, "Paste");
		paste.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control V"));
		menu.add(paste);

		frmHi = new JFrame();
		frmHi.setTitle("AWS Simple GUI Tool");
		frmHi.setBounds(100, 100, 447, 300);
		frmHi.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel panel = new JPanel();
		frmHi.getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(null);

		// create text field that let user input credential information
		textField = new JPasswordField();
		textField.setBounds(182, 82, 154, 21);
		panel.add(textField);
		textField.setColumns(10);
		textField.setComponentPopupMenu(menu);

		textField_1 = new JPasswordField();
		textField_1.setColumns(10);
		textField_1.setBounds(182, 51, 154, 21);
		panel.add(textField_1);
		textField_1.setComponentPopupMenu(menu);

		Choice choice = new Choice();
		choice.setBounds(182, 109, 113, 21);
		choice.addItem("us-east-1");
		choice.addItem("us-east-2");
		choice.addItem("us-west-1");
		choice.addItem("us-west-2");
		panel.add(choice);

		// button action to send the credential to aws
		JButton btnNewButton = new JButton("Log in");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// debug
				System.out.println(textField_1.getText() + "   " + textField.getText());
				// generate user basic credential with collected information
				user_credential = new BasicAWSCredentials(textField_1.getText(), textField.getText());
				try {
					// build three clients that will be used in program functions with given
					// credential
					s3Client = AmazonS3ClientBuilder.standard().withRegion(choice.getSelectedItem())
							.withCredentials(new AWSStaticCredentialsProvider(user_credential)).build();
					ec2Client = AmazonEC2ClientBuilder.standard().withRegion(choice.getSelectedItem())
							.withCredentials(new AWSStaticCredentialsProvider(user_credential)).build();
					elbClient = AmazonElasticLoadBalancingClientBuilder.standard().withRegion(choice.getSelectedItem())
							.withCredentials(new AWSStaticCredentialsProvider(user_credential)).build();
					try {
						// test if the credential is valid
						List<Bucket> buckets = s3Client.listBuckets();
						System.out.println(buckets.get(1).getName());
						// call a new console to show function of program
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
		btnNewButton.setBounds(182, 136, 93, 23);
		panel.add(btnNewButton);

		// add text labels to indicate explanations
		JLabel lblWelcomeToAws = new JLabel("Welcome to AWS Simple GUI Tool");
		lblWelcomeToAws.setFont(new Font("SimSun", Font.PLAIN, 14));
		lblWelcomeToAws.setHorizontalAlignment(SwingConstants.CENTER);
		lblWelcomeToAws.setBounds(98, 10, 238, 31);
		panel.add(lblWelcomeToAws);

		JLabel lblIamAccessKey = new JLabel("IAM Access Key");
		lblIamAccessKey.setBounds(20, 54, 146, 15);
		panel.add(lblIamAccessKey);

		JLabel lblIamSecretAccess = new JLabel("IAM Secret Access Key");
		lblIamSecretAccess.setBounds(20, 85, 146, 15);
		panel.add(lblIamSecretAccess);

		JLabel lblRegion = new JLabel("Region");
		lblRegion.setBounds(20, 109, 54, 15);
		panel.add(lblRegion);

		JLabel lblNewLabel = new JLabel("Presented By: NERVE Software");
		lblNewLabel.setBounds(220, 236, 201, 15);
		panel.add(lblNewLabel);

	}
}
