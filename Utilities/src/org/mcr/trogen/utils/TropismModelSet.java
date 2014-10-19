/*
 * Copyright (C) 2014 The University of Manchester
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2.0 as published by the Free Software Foundation.
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.filesystems.FileObject;

/**
 * @filename TropismModelSet.java
 * @date 12-Aug-2013
 * @author Felix Feyertag <felix.feyertag@manchester.ac.uk>
 * @desc
 */
public final class TropismModelSet implements Serializable {
    
    
    private static final long serialVersionUID = 7526472295622776147L;

    private String name;
    private Map<String,TropismModel> models;
    
    private List<Sequence> r5TrainingSeqs;
    private List<Sequence> x4TrainingSeqs;
    private List<Sequence> r5ValidationSeqs;
    private List<Sequence> x4ValidationSeqs;
    
    public TropismModelSet() {
        name = "";
        models = new HashMap<String,TropismModel>();
        r5TrainingSeqs = new LinkedList<Sequence>();
        x4TrainingSeqs = new LinkedList<Sequence>();
        r5ValidationSeqs = new LinkedList<Sequence>();
        x4ValidationSeqs = new LinkedList<Sequence>();
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public void addModel(String algorithmName, TropismModel model) {
        models.put(algorithmName, model);
    }
    
    public Map<String,TropismModel> getModels() {
        return models;
    }

    public List<Sequence> getR5TrainingSeqs() {
        return r5TrainingSeqs;
    }

    public void setR5TrainingSeqs(List<Sequence> r5TrainingSeqs) {
        this.r5TrainingSeqs = r5TrainingSeqs;
    }

    public List<Sequence> getX4TrainingSeqs() {
        return x4TrainingSeqs;
    }

    public void setX4TrainingSeqs(List<Sequence> x4TrainingSeqs) {
        this.x4TrainingSeqs = x4TrainingSeqs;
    }

    public List<Sequence> getR5ValidationSeqs() {
        return r5ValidationSeqs;
    }

    public void setR5ValidationSeqs(List<Sequence> r5ValidationSeqs) {
        this.r5ValidationSeqs = r5ValidationSeqs;
    }

    public List<Sequence> getX4ValidationSeqs() {
        return x4ValidationSeqs;
    }

    public void setX4ValidationSeqs(List<Sequence> x4ValidationSeqs) {
        this.x4ValidationSeqs = x4ValidationSeqs;
    }
        
    public static void writeModelSetToFile(TropismModelSet ms, FileObject fo) {
        try {
            OutputStream os = fo.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(os);
            oos.writeObject(ms);
            oos.close();
            os.close();
        } catch (FileAlreadyLockedException ex) {
            Logger.getLogger(TropismModelSet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TropismModelSet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static TropismModelSet readModelSetFromFile(FileObject fo) {
        ObjectInputStream ois;
        TropismModelSet ret = null;
        try {
            ois = new ObjectInputStream(fo.getInputStream());
            ret = (TropismModelSet) ois.readObject();
            ois.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TropismModelSet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TropismModelSet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(TropismModelSet.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }
    
}
