package com.amazonaws.samples;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import javax.swing.JLabel;
import javax.swing.ListModel;

public class S3Console {

	private JFrame frame;
	public DefaultListModel<String> model;
	public DefaultListModel<String> model_1;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					S3Console window = new S3Console();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public S3Console() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		model = new DefaultListModel<>();
		model_1 = new DefaultListModel<>();
		
		JLabel lblBuckets = new JLabel("Buckets");
		lblBuckets.setBounds(10, 27, 54, 15);
		frame.getContentPane().add(lblBuckets);
		
		
		
		JLabel lblItems = new JLabel("Item(s)");
		lblItems.setBounds(156, 27, 54, 15);
		frame.getContentPane().add(lblItems);
		
		JList<String> list_1 = new JList<>(model_1);
		list_1.setBounds(156, 52, 120, 199);
		frame.getContentPane().add(list_1);
		
		JList<String> list = new JList<>(model);
		list.setBounds(10, 52, 120, 199);
		frame.getContentPane().add(list);
		
		List<Bucket> buckets = InitialWindow.s3Client.listBuckets();
		for (int i = 0; i < buckets.size(); i++) {
			model.addElement(buckets.get(i).getName());
		}
		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				ListObjectsV2Result result = InitialWindow.s3Client.listObjectsV2(list.getSelectedValue());
				List<S3ObjectSummary> objects = result.getObjectSummaries();
				for (int i = 0; i < objects.size(); i++) {
					model_1.clear();
					model_1.addElement(objects.get(i).getKey());
				}
			}

		});
		

		
	}

//	public void refreshBucketlist() {
//		model = new DefaultListModel<>();
//		List<Bucket> buckets = InitialWindow.s3Client.listBuckets();
//		for (int i = 0; i < buckets.size(); i++) {
//			model.addElement(buckets.get(i).getName());
//		}
//	}
}
