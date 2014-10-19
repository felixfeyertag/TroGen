/*
 * Copyright (C) 2014 The University of Manchester
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
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

package org.mcr.trogen.utils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInstall;
import org.openide.modules.OnStart;
import org.openide.util.Exceptions;

/**
 *
 * @author Felix Feyertag <felix.feyertag@manchester.ac.uk>
 */
@OnStart
public class TroGenLoader implements Runnable {
    
    @Override
    public void run() {
        System.out.println("ModuleInstall: Loading tropism model");
        FileObject configRoot = FileUtil.getConfigRoot();
        FileObject configModel = configRoot.getFileObject("TropismModels");
        if (configModel == null) {
            try {
                configModel = configRoot.createFolder("TropismModels");
                OutputStream configFile = configModel.createAndOpen("Default.model");
                File f = new File("Library/Default.model");
                System.err.println("From: " + f.getCanonicalPath() + "   To:   " + configFile.toString());
                Files.copy(f.toPath(), configFile);
                configFile.close();
                
                //System.out.println(defaultModel.getPath());
                //FileUtil.copyFile(FileUtil.toFileObject(new File(defaultModel.getFile())), configModel, "Default.model");
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    
}
