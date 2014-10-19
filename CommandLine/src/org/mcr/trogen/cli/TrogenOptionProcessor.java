/*
 * Copyright (C) 2014 The University of Manchester
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
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

package org.mcr.trogen.cli;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.sendopts.CommandException;
import org.netbeans.spi.sendopts.Env;
import org.netbeans.spi.sendopts.Option;
import org.netbeans.spi.sendopts.OptionProcessor;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Felix Feyertag <felix.feyertag@manchester.ac.uk>
 */
@ServiceProvider(service=OptionProcessor.class)
public class TrogenOptionProcessor extends OptionProcessor {

    private Option defaultRunOption = Option.defaultArguments();
    private Option inputFileOption = Option.additionalArguments('i', "input");
    private Option outputFileOption = Option.additionalArguments('o', "output");
    private Option modelOption = Option.additionalArguments('m', "model");
    private Option helpOption = Option.withoutArgument('h', "help");
    
    @Override
    protected Set<Option> getOptions() {
        HashSet<Option> set = new HashSet<Option>();
        set.add(defaultRunOption);
        set.add(inputFileOption);
        set.add(outputFileOption);
        set.add(modelOption);
        set.add(helpOption);
        return set;
    }

    @Override
    protected void process(Env env, Map<Option, String[]> maps) throws CommandException {

        if (maps.get(helpOption)!=null) {
            System.out.println("TroGen version " + System.getProperty("netbeans.buildnumber"));
            System.out.println("Copyright (C) 2014 The University of Manchester");
            System.out.println();
            System.out.println("Command line options:");
            System.out.println("\t-i --input <file>\tInput file in FASTA format");
            System.out.println("\t-o --output <file>\tOutput file (default: <input>.trogen.csv");
            System.out.println("\t-m --model <model>\tTraining model to use (default: Default)");
            System.out.println("\t-h --help\tDisplay this message");
            System.exit(0);
        }
        
        //input
        List<String> filenameList = new ArrayList<String>();
        Object obj = maps.get(defaultRunOption);
        if (obj != null) {
            filenameList.addAll(Arrays.asList((String[])obj));
        }
        obj = maps.get(inputFileOption);
        if (obj != null) {
            filenameList.addAll(Arrays.asList((String[])obj));
        }
        
        if (filenameList.isEmpty()) {
            return;
        }
        
        //output
        String outputFile = null;
        obj = maps.get(outputFileOption);
        if (obj != null) {
            outputFile = ((String[])obj)[0];
        }
        else {
            outputFile = filenameList.get(0) + ".trogen.csv";
        }
        
        //model
        String model = "Default";
        obj = maps.get(modelOption);
        if (obj != null) {
            model = ((String[])obj)[0];
        }
        
        
    }
    
}
