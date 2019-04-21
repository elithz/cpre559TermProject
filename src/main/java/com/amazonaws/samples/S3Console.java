package com.amazonaws.samples;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ListVersionsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.S3VersionSummary;
import com.amazonaws.services.s3.model.VersionListing;

import javax.swing.JLabel;
import javax.swing.ListModel;
import javax.swing.JButton;

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
		lblItems.setBounds(198, 27, 54, 15);
		frame.getContentPane().add(lblItems);

		JList<String> list_1 = new JList<>(model_1);
		list_1.setBounds(198, 52, 120, 199);
		frame.getContentPane().add(list_1);

		JList<String> list = new JList<>(model);
		list.setBounds(10, 52, 178, 166);
		frame.getContentPane().add(list);
		List<Bucket> buckets = InitialWindow.s3Client.listBuckets();
		model.clear();
		for (int i = 0; i < buckets.size(); i++) {
			model.addElement(buckets.get(i).getName());
		}
		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				String selected_bucket = list.getSelectedValue();
				System.out.println(selected_bucket);
				System.out.println(list.getSelectedIndex());
				if(list.getSelectedIndex() == -1) {
					list.setSelectedIndex(0);
				}else {
					ListObjectsV2Result result = InitialWindow.s3Client.listObjectsV2(selected_bucket);
					List<S3ObjectSummary> objects = result.getObjectSummaries();
					model_1.clear();
					for (int i = 0; i < objects.size(); i++) {
						model_1.addElement(objects.get(i).getKey());
					}
				}
				
			}

		});

		JButton btnNewButton = new JButton("Add");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String name = JOptionPane.showInputDialog("What's the name of the new bucket?");
				try {
					InitialWindow.s3Client.createBucket(name);
				} catch (AmazonS3Exception e1) {
					System.err.println(e1.getErrorMessage());
				}
				List<Bucket> buckets = InitialWindow.s3Client.listBuckets();
				model.clear();
				for (int i = 0; i < buckets.size(); i++) {
					model.addElement(buckets.get(i).getName());
				}
				list.setSelectedIndex(0);
			}
		});
		btnNewButton.setBounds(10, 228, 82, 23);
		frame.getContentPane().add(btnNewButton);
		
		JButton btnDelete = new JButton("Delete");
		btnDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String name_to_remove = list.getSelectedValue();
				System.out.println(name_to_remove);
				
				try {
				    System.out.println(" - removing objects from bucket");
				    ObjectListing object_listing = InitialWindow.s3Client.listObjects(name_to_remove);
				    while (true) {
				        for (Iterator<?> iterator =
				                object_listing.getObjectSummaries().iterator();
				                iterator.hasNext();) {
				            S3ObjectSummary summary = (S3ObjectSummary)iterator.next();
				            InitialWindow.s3Client.deleteObject(name_to_remove, summary.getKey());
				        }

				        // more object_listing to retrieve?
				        if (object_listing.isTruncated()) {
				            object_listing = InitialWindow.s3Client.listNextBatchOfObjects(object_listing);
				        } else {
				            break;
				        }
				    };

				    System.out.println(" - removing versions from bucket");
				    VersionListing version_listing = InitialWindow.s3Client.listVersions(
				            new ListVersionsRequest().withBucketName(name_to_remove));
				    while (true) {
				        for (Iterator<?> iterator =
				                version_listing.getVersionSummaries().iterator();
				                iterator.hasNext();) {
				            S3VersionSummary vs = (S3VersionSummary)iterator.next();
				            InitialWindow.s3Client.deleteVersion(
				            		name_to_remove, vs.getKey(), vs.getVersionId());
				        }

				        if (version_listing.isTruncated()) {
				            version_listing = InitialWindow.s3Client.listNextBatchOfVersions(
				                    version_listing);
				        } else {
				            break;
				        }
				    }
				} catch (AmazonServiceException e1) {
				    System.err.println(e1.getErrorMessage());
				    System.exit(1);
				}
				
				try {
					InitialWindow.s3Client.deleteBucket(name_to_remove);
					 System.out.println("bucket removed");
				} catch (AmazonServiceException e1) {
				    System.err.println(e1.getErrorMessage());
				    System.exit(1);
				}
				
				List<Bucket> buckets = InitialWindow.s3Client.listBuckets();
				model.clear();
				for (int i = 0; i < buckets.size(); i++) {
					model.addElement(buckets.get(i).getName());
				}
//				if (list.getSelectedIndex() != -1) {
//				    model.remove(list.getSelectedIndex());
//				}
//				list.setSelectedIndex(0);
			}
		});
		btnDelete.setBounds(102, 228, 82, 23);
		frame.getContentPane().add(btnDelete);

		

	}

}
