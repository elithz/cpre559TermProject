package com.amazonaws.samples;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ListVersionsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.S3VersionSummary;
import com.amazonaws.services.s3.model.VersionListing;

/***
 * 
 * S3Console.java
 * 
 * @author elith, daiyuan
 * @version 2.1 NERVE Software 2019/5/10
 *
 */

public class S3Console {

	private JFrame frmSconsole;
	public DefaultListModel<String> model;
	public DefaultListModel<String> model_1;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					S3Console window = new S3Console();
					window.frmSconsole.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public S3Console() {
		initialize();
	}

	private void initialize() {
		frmSconsole = new JFrame();
		frmSconsole.setTitle("S3Console");
		frmSconsole.setBounds(100, 100, 550, 300);
		frmSconsole.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frmSconsole.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				Console.frmConsole.setEnabled(true);
			}

		});
		frmSconsole.getContentPane().setLayout(null);

		model = new DefaultListModel<>();
		model_1 = new DefaultListModel<>();

		JLabel lblBuckets = new JLabel("Buckets");
		lblBuckets.setBounds(10, 27, 54, 15);
		frmSconsole.getContentPane().add(lblBuckets);

		JLabel lblItems = new JLabel("Item(s)");
		lblItems.setBounds(252, 27, 54, 15);
		frmSconsole.getContentPane().add(lblItems);

		JList<String> list_1 = new JList<>(model_1);
		list_1.setBounds(252, 52, 169, 199);
		frmSconsole.getContentPane().add(list_1);

		JList<String> list = new JList<>(model);
		list.setBounds(10, 52, 232, 166);
		frmSconsole.getContentPane().add(list);
		List<Bucket> buckets = InitialWindow.s3Client.listBuckets();
		model.clear();
		for (int i = 0; i < buckets.size(); i++) {
			model.addElement(buckets.get(i).getName());
		}
		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				// list the objects of the selected bucket
				String selected_bucket = list.getSelectedValue();
				System.out.println(selected_bucket);
				System.out.println(list.getSelectedIndex());
				// if the bucket is delete, make the selected bucket to the first of the list
				if (list.getSelectedIndex() == -1)
					list.setSelectedIndex(0);
				else {
					// refresh the object list of the selected bucket
					ListObjectsV2Result result = InitialWindow.s3Client.listObjectsV2(selected_bucket);
					List<S3ObjectSummary> objects = result.getObjectSummaries();
					model_1.clear();
					for (int i = 0; i < objects.size(); i++)
						model_1.addElement(objects.get(i).getKey());
				}
			}
		});

		JButton btnNewButton = new JButton("Add Bucket");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// add new bucket to s3 with the specified name
				String name = JOptionPane.showInputDialog("What's the name of the new bucket?");
				try {
					InitialWindow.s3Client.createBucket(name);
				} catch (AmazonS3Exception e1) {
					System.err.println(e1.getErrorMessage());
				}
				// refresh bucket list
				List<Bucket> buckets = InitialWindow.s3Client.listBuckets();
				model.clear();
				for (int i = 0; i < buckets.size(); i++)
					model.addElement(buckets.get(i).getName());
				list.setSelectedIndex(0);
			}
		});
		btnNewButton.setBounds(10, 228, 111, 23);
		frmSconsole.getContentPane().add(btnNewButton);

		JButton btnDelete = new JButton("Delete Bucket");
		btnDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// delete selected bucket
				String name_to_remove = list.getSelectedValue();
				System.out.println(name_to_remove);

				// removing all objects in the bucket
				try {
					System.out.println(" - removing objects from bucket");
					ObjectListing object_listing = InitialWindow.s3Client.listObjects(name_to_remove);
					while (true) {
						for (Iterator<?> iterator = object_listing.getObjectSummaries().iterator(); iterator
								.hasNext();) {
							S3ObjectSummary summary = (S3ObjectSummary) iterator.next();
							InitialWindow.s3Client.deleteObject(name_to_remove, summary.getKey());
						}

						// more object_listing to retrieve?
						if (object_listing.isTruncated())
							object_listing = InitialWindow.s3Client.listNextBatchOfObjects(object_listing);
						else
							break;
					}

					// removing all versions of the bucket
					System.out.println(" - removing versions from bucket");
					VersionListing version_listing = InitialWindow.s3Client
							.listVersions(new ListVersionsRequest().withBucketName(name_to_remove));
					while (true) {
						for (Iterator<?> iterator = version_listing.getVersionSummaries().iterator(); iterator
								.hasNext();) {
							S3VersionSummary vs = (S3VersionSummary) iterator.next();
							InitialWindow.s3Client.deleteVersion(name_to_remove, vs.getKey(), vs.getVersionId());
						}

						if (version_listing.isTruncated())
							version_listing = InitialWindow.s3Client.listNextBatchOfVersions(version_listing);
						else
							break;
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

				// refresh bucket list
				List<Bucket> buckets = InitialWindow.s3Client.listBuckets();
				model.clear();
				for (int i = 0; i < buckets.size(); i++)
					model.addElement(buckets.get(i).getName());
			}
		});
		btnDelete.setBounds(131, 228, 111, 23);
		frmSconsole.getContentPane().add(btnDelete);

		JButton btnUpload = new JButton("Upload");
		btnUpload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// upload a local object to the selected bucket
				String target_bucket = list.getSelectedValue();
				System.out.println(target_bucket);
				JFileChooser chooser = new JFileChooser();
				File selectedFile = null;
				if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					selectedFile = chooser.getSelectedFile();
					try {
						System.out.println("Uploading " + selectedFile.getName() + "\n");
						InitialWindow.s3Client.putObject(target_bucket, selectedFile.getName(), selectedFile);
						System.out.println("Upload finished");
					} catch (AmazonServiceException ase) {
						System.out.println("Caught an AmazonServiceException, which means your request made it "
								+ "to Amazon S3, but was rejected with an error response for some reason.");
						System.out.println("Error Message:    " + ase.getMessage());
						System.out.println("HTTP Status Code: " + ase.getStatusCode());
						System.out.println("AWS Error Code:   " + ase.getErrorCode());
						System.out.println("Error Type:       " + ase.getErrorType());
						System.out.println("Request ID:       " + ase.getRequestId());
					} catch (AmazonClientException ace) {
						System.out.println("Caught an AmazonClientException, which means the client encountered "
								+ "a serious internal problem while trying to communicate with S3, "
								+ "such as not being able to access the network.");
						System.out.println("Error Message: " + ace.getMessage());
					}

					// refresh object list
					ListObjectsV2Result result = InitialWindow.s3Client.listObjectsV2(target_bucket);
					List<S3ObjectSummary> objects = result.getObjectSummaries();
					model_1.clear();
					for (int i = 0; i < objects.size(); i++)
						model_1.addElement(objects.get(i).getKey());
				} else
					System.out.println("user canceled upload");
			}
		});
		btnUpload.setBounds(431, 49, 93, 23);
		frmSconsole.getContentPane().add(btnUpload);

		JButton button = new JButton("Download");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// download the selected object from the target bucket to local
				// user will need to manually specify the system directory to store the object
				String target_file = list_1.getSelectedValue();
				String target_bucket = list.getSelectedValue();
				String save_path = null;
				if (target_file != null) {
					JFileChooser chooser = new JFileChooser();
					chooser.setCurrentDirectory(new java.io.File("."));
					chooser.setDialogTitle("Choose the directory you want to save the file");
					chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					chooser.setAcceptAllFileFilterUsed(false);
					if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
						save_path = chooser.getSelectedFile().getAbsolutePath();
						System.out.println(chooser.getSelectedFile().getAbsolutePath());
						System.out.println(chooser.getSelectedFile());
						System.out.println(chooser.getCurrentDirectory());
						try {
							S3Object o = InitialWindow.s3Client.getObject(target_bucket, target_file);
							S3ObjectInputStream s3is = o.getObjectContent();
							FileOutputStream fos = new FileOutputStream(
									new File(save_path + File.separator + target_file));
							byte[] read_buf = new byte[1024];
							int read_len = 0;
							while ((read_len = s3is.read(read_buf)) > 0) {
								fos.write(read_buf, 0, read_len);
							}
							System.out.println("Download finished");
							s3is.close();
							fos.close();
						} catch (AmazonServiceException e1) {
							System.err.println(e1.getErrorMessage());
							System.exit(1);
						} catch (FileNotFoundException e1) {
							System.err.println(e1.getMessage());
							System.exit(1);
						} catch (IOException e1) {
							System.err.println(e1.getMessage());
							System.exit(1);
						}
					} else
						System.out.println("user canceled download");
				} else
					System.out.println("no object to download");

			}
		});
		button.setBounds(431, 96, 93, 23);
		frmSconsole.getContentPane().add(button);

		JButton btnDelete_1 = new JButton("Delete");
		btnDelete_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// delete the selected object from the target bucket
				String target_file = list_1.getSelectedValue();
				String target_bucket = list.getSelectedValue();
				if (target_file != null) {
					try {
						DeleteObjectsRequest dor = new DeleteObjectsRequest(target_bucket).withKeys(target_file);
						InitialWindow.s3Client.deleteObjects(dor);
						System.out.println("delete complete");
					} catch (AmazonServiceException e1) {
						System.err.println(e1.getErrorMessage());
						System.exit(1);
					}
					ListObjectsV2Result result = InitialWindow.s3Client.listObjectsV2(target_bucket);
					List<S3ObjectSummary> objects = result.getObjectSummaries();
					model_1.clear();
					for (int i = 0; i < objects.size(); i++)
						model_1.addElement(objects.get(i).getKey());
				} else
					System.out.println("no object to delete");

			}
		});
		btnDelete_1.setBounds(431, 140, 93, 23);
		frmSconsole.getContentPane().add(btnDelete_1);

	}

}
