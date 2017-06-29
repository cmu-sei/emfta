/**
 * Copyright (c) 2015 Carnegie Mellon University.
 * All Rights Reserved.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS," WITH NO WARRANTIES WHATSOEVER.
 * CARNEGIE MELLON UNIVERSITY EXPRESSLY DISCLAIMS TO THE FULLEST 
 * EXTENT PERMITTEDBY LAW ALL EXPRESS, IMPLIED, AND STATUTORY 
 * WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE WARRANTIES OF 
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND 
 * NON-INFRINGEMENT OF PROPRIETARY RIGHTS.

 * This Program is distributed under a BSD license.  
 * Please see license.txt file or permission@sei.cmu.edu for more
 * information. 
 * 
 * DM-0003411
 */

package org.osate.aadl2.errormodel.emfta.handlers;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class FTADialog extends TitleAreaDialog {

	private String value;
	java.util.List<String> values;
	private Combo errorMode;
	private Button graphBox;
	private boolean graph;
	private Button optBox;
	private Button cutsetBox;
	private Button basictreeBox;
	private boolean optimize;
	private boolean mincutset;
	private boolean basictree;
	private String target = "";

	public FTADialog(Shell parentShell) {
		super(parentShell);
	}

	public void setValues(java.util.List<String> v) {
		values = v;
	}

	public void setTarget(String targetname) {
		target = targetname;
	}

	@Override
	public void create() {
		super.create();
		setTitle("Fault-Tree Analysis");
		setMessage("Select the Failure Mode to Analyze (error state, propagation)"
				+ (target.isEmpty() ? "" : " for " + target), IMessageProvider.INFORMATION);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);

		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		container.setLayout(layout);

		Label labelErrorMode = new Label(container, SWT.NONE);
		labelErrorMode.setText("Error-Mode");

		errorMode = new Combo(container, SWT.READ_ONLY | SWT.BORDER);
		String val[] = new String[values.size()];
		for (int i = 0; i < values.size(); i++) {
			val[i] = values.get(i);
		}
		errorMode.setItems(val);

		errorMode.select(0);
// enable if we want to support showing shared events as fault graph instead of fault tree
//		graphBox = new Button(container, SWT.CHECK);
//		graphBox.setText("Fault graph");
//		graphBox.setSelection(false);
		basictreeBox = new Button(container, SWT.RADIO);
		basictreeBox.setText("Basic Fault Tree (dependent events marked by *)");
		basictreeBox.setSelection(true);
		optBox = new Button(container, SWT.RADIO);
		optBox.setText("Transformed Fault Tree with Computed Probability");
		optBox.setSelection(false);
		cutsetBox = new Button(container, SWT.RADIO);
		cutsetBox.setText("Minimal Cut Sets");
		cutsetBox.setSelection(false);
		return area;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	private void saveInput() {
		value = errorMode.getText();
		if (graphBox == null)
			graph = false;
		else
			optimize = graphBox.getSelection();
		if (optBox == null)
			optimize = true;
		else
			optimize = optBox.getSelection();
		if (cutsetBox == null)
			mincutset = false;
		else
			mincutset = cutsetBox.getSelection();
		if (basictreeBox == null)
			basictree = true;
		else
			basictree = basictreeBox.getSelection();
	}

	@Override
	protected void okPressed() {
		saveInput();
		super.okPressed();
	}

	public String getValue() {
		return value;
	}

	public boolean getSharedEventsAsGraph() {
		return graph;
	}

	public boolean getTransform() {
		return optimize;
	}

	public boolean getMinCutSet() {
		return mincutset;
	}

	public boolean getBasicTree() {
		return basictree;
	}

}
