/*
 * Part of Simbrain--a java-based neural network kit
 * Copyright (C) 2006 Jeff Yoshimi <www.jeffyoshimi.net>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.simbrain.world.visionworld;

import java.awt.BorderLayout;

import javax.swing.Action;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import org.simbrain.workspace.Workspace;

/**
 * Vision world frame.
 */
public final class VisionWorldFrame
    extends JInternalFrame {

    /** Default title. */
    private static final String DEFAULT_TITLE = "Vision world";

    /** Resizeable flag. */
    private static final boolean RESIZEABLE = true;

    /** Closeable flag. */
    private static final boolean CLOSEABLE = true;

    /** Maximizeable flag. */
    private static final boolean MAXIMIZEABLE = true;

    /** Iconifiable flag. */
    private static final boolean ICONIFIABLE = true;

    /** Workspace. */
    private final Workspace workspace;


    /**
     * Create a new vision world frame with the specified workspace.
     *
     * @param workspace workspace, must not be null
     */
    public VisionWorldFrame(final Workspace workspace) {
        super(DEFAULT_TITLE, RESIZEABLE, CLOSEABLE, MAXIMIZEABLE, ICONIFIABLE);
        if (workspace == null) {
            throw new IllegalArgumentException("workspace must not be null");
        }
        this.workspace = workspace;

        VisionWorldModel visionWorldModel = new MutableVisionWorldModel();
        VisionWorld visionWorld = new VisionWorld(visionWorldModel);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JMenuBar menuBar = new JMenuBar();
        JToolBar toolBar = new JToolBar();

        JMenu file = new JMenu("File");
        for (Action action : visionWorld.getFileMenuActions()) {
            file.add(action);
            toolBar.add(action);
        }

        toolBar.addSeparator();

        JMenu edit = new JMenu("Edit");
        for (Action action : visionWorld.getEditMenuActions()) {
            edit.add(action);
            toolBar.add(action);
        }

        toolBar.addSeparator();

        JMenu view = new JMenu("View");
        for (Action action : visionWorld.getViewMenuActions()) {
            view.add(action);
            toolBar.add(action);
        }

        menuBar.add(file);
        menuBar.add(edit);
        menuBar.add(view);
        setJMenuBar(menuBar);

        JPanel contentPane = (JPanel) getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add("North", toolBar);
        contentPane.add("Center", visionWorld);
    }
}
