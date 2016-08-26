/* OpenDA v2.3.1 
* Copyright (c) 2016 OpenDA Association 
* All rights reserved.
* 
* This file is part of OpenDA. 
* 
* OpenDA is free software: you can redistribute it and/or modify 
* it under the terms of the GNU Lesser General Public License as 
* published by the Free Software Foundation, either version 3 of 
* the License, or (at your option) any later version. 
* 
* OpenDA is distributed in the hope that it will be useful, 
* but WITHOUT ANY WARRANTY; without even the implied warranty of 
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
* GNU Lesser General Public License for more details. 
* 
* You should have received a copy of the GNU Lesser General Public License
* along with OpenDA.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.openda.uncertaintygui;

import javax.swing.*;
import java.awt.*;

/**
 * Uppermost GUI parent for DATools.
 */
public class MainFrame extends javax.swing.JFrame {

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem closeProjectMenuItem;
    private javax.swing.JMenuItem exitMenuItem;
    protected javax.swing.JMenu fileMenu;
    protected javax.swing.JMenu helpMenu;
    protected javax.swing.JMenuItem showHelpMenuItem;
    protected javax.swing.JMenuItem showAboutBoxMenuItem;
    private javax.swing.JSeparator firstFileSeparator;
    private org.openda.uncertaintygui.MainPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    protected javax.swing.JButton newProjectButton;
    private javax.swing.JMenuItem newProjectMenuItem;
    protected javax.swing.JButton openProjectButton;
    private javax.swing.JMenuItem openProjectMenuItem;
    protected javax.swing.JButton runButton;
    private javax.swing.JMenuItem saveResultSelectionMenuItem;
    private javax.swing.JMenuItem saveUncertaintySpecificationMenuItem;
    private javax.swing.JSeparator secondFileSeparator;
    private javax.swing.JSeparator thirdFileSeparator;
    protected javax.swing.JToolBar toolBar;
    // End of variables declaration//GEN-END:variables


	/**
	 * Constructor  for UATools User Interface Application
	 */
	public MainFrame()  {

		try {

			enableEvents(AWTEvent.WINDOW_EVENT_MASK);

			this.initComponents();
            this.setMinimumSize(new java.awt.Dimension(400, 400));
			this.pack();

		} catch (Exception e) {
			String msg = "Error Initializing UI: " + e.getMessage();
			String title = "Initializing UI";
			showMessageDialog(title, msg);
		}

		setExtendedState(Frame.MAXIMIZED_BOTH);
		this.setVisible(true);
	}

	/**
	 * Show Message Dialog.
	 * @param title the title of the dialog
	 * @param msg the message
	 */
	private void showMessageDialog(String title, String msg) {
		JOptionPane.showMessageDialog(this, msg, title,
				JOptionPane.INFORMATION_MESSAGE);
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        toolBar = new javax.swing.JToolBar();
        openProjectButton = new javax.swing.JButton();
        newProjectButton = new javax.swing.JButton();
        runButton = new javax.swing.JButton();
        mainPanel = new org.openda.uncertaintygui.MainPanel();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        newProjectMenuItem = new javax.swing.JMenuItem();
        openProjectMenuItem = new javax.swing.JMenuItem();
        closeProjectMenuItem = new javax.swing.JMenuItem();
        firstFileSeparator = new javax.swing.JSeparator();
        secondFileSeparator = new javax.swing.JSeparator();
        saveUncertaintySpecificationMenuItem = new javax.swing.JMenuItem();
        saveResultSelectionMenuItem = new javax.swing.JMenuItem();
        thirdFileSeparator = new javax.swing.JSeparator();
        exitMenuItem = new javax.swing.JMenuItem();

        helpMenu = new javax.swing.JMenu();
        showHelpMenuItem = new javax.swing.JMenuItem();
        showAboutBoxMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("DA Uncertainty GUI");
        toolBar.setFloatable(false);
        toolBar.setMinimumSize(new java.awt.Dimension(400, 27));
        openProjectButton.setText("Open Project");
        toolBar.add(openProjectButton);

        newProjectButton.setText("New Project");
        toolBar.add(newProjectButton);

        runButton.setText("Run");
        toolBar.add(runButton);

        mainPanel.setMinimumSize(new java.awt.Dimension(550, 750));

        fileMenu.setText("File");
        newProjectMenuItem.setMnemonic('N');
        newProjectMenuItem.setText("New Project");
        fileMenu.add(newProjectMenuItem);

        openProjectMenuItem.setMnemonic('O');
        openProjectMenuItem.setText("Open Project");
        fileMenu.add(openProjectMenuItem);

        closeProjectMenuItem.setMnemonic('C');
        closeProjectMenuItem.setText("Close Project");
        fileMenu.add(closeProjectMenuItem);

        fileMenu.add(firstFileSeparator);

        fileMenu.add(secondFileSeparator);

        saveUncertaintySpecificationMenuItem.setMnemonic('S');
        saveUncertaintySpecificationMenuItem.setText("Save Uncertainty Specification As");
        fileMenu.add(saveUncertaintySpecificationMenuItem);

        saveResultSelectionMenuItem.setMnemonic('A');
        saveResultSelectionMenuItem.setText("Save Result Selection As");
        fileMenu.add(saveResultSelectionMenuItem);

        fileMenu.add(thirdFileSeparator);

        exitMenuItem.setMnemonic('X');
        exitMenuItem.setText("Exit");
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        helpMenu.setText("Help");

        showHelpMenuItem.setMnemonic('H');
        showHelpMenuItem.setText("Help");
        helpMenu.add(showHelpMenuItem);

        showAboutBoxMenuItem.setMnemonic('A');
        showAboutBoxMenuItem.setText("About");
        helpMenu.add(showAboutBoxMenuItem);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(toolBar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 896, Short.MAX_VALUE)
            .add(mainPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 896, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(toolBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mainPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 805, Short.MAX_VALUE))
        );
        pack();
    }// </editor-fold>//GEN-END:initComponents


	public org.openda.uncertaintygui.MainPanel getMainPanel() {
		return mainPanel;
	}

	public javax.swing.JMenuItem getNewProjectMenuItem() {
		return newProjectMenuItem;
	}

	public javax.swing.JMenuItem getOpenProjectMenuItem() {
		return openProjectMenuItem;
	}

	public javax.swing.JMenuItem getCloseProjectMenuItem() {
		return closeProjectMenuItem;
	}

	public javax.swing.JMenuItem getSaveResultSelectionAsMenuItem() {
		return saveResultSelectionMenuItem;
	}

	public javax.swing.JMenuItem getSaveUncertaintySpecificationAsMenuItem() {
		return saveUncertaintySpecificationMenuItem;
	}

	public javax.swing.JMenuItem getExitMenuItem() {
		return exitMenuItem;
	}

	public javax.swing.JButton getNewProjectButton() {
		return newProjectButton;
	}

	public javax.swing.JButton getOpenProjectButton() {
		return openProjectButton;
	}

	public javax.swing.JButton getRunButton() {
		return runButton;
	}

    public javax.swing.JMenuItem getShowHelpMenuItem() {
        return showHelpMenuItem;
    }

    public javax.swing.JMenuItem getShowAboutBoxMenuItem() {
        return showAboutBoxMenuItem;
    }
}
