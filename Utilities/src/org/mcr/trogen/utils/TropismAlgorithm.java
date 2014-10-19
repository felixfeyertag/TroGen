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

import java.util.List;

/**
 *
 * @author Felix Feyertag <felix.feyertag@manchester.ac.uk>
 */
public interface TropismAlgorithm {
    public String getAlgorithmName();
    public TropismAlgorithmParameters getAlgorithmParameters();
    public Sequence classifySequence (Sequence seq) throws TropismAlgorithmException;
    
    public TropismModel trainModel (TropismAlgorithmParameters params, List<Sequence> r5training, List<Sequence> x4training, List<Sequence> r5testing, List<Sequence> x4testing);
    public void loadModel(TropismModel model);
}
