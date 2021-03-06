/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package arq.cmdline;

import java.io.PrintStream ;
import java.util.HashSet ;
import java.util.Set ;

import arq.cmd.* ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFFormat ;
import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.riot.RDFWriterRegistry ;
import org.apache.jena.riot.system.StreamRDFWriter ;

public class ModLangOutput implements ArgModuleGeneral
{
    protected ArgDecl argOutput       = new ArgDecl(ArgDecl.HasValue, "out", "output") ;
    protected ArgDecl argPretty       = new ArgDecl(ArgDecl.HasValue, "formatted", "pretty", "fmt") ;
    protected ArgDecl argStream       = new ArgDecl(ArgDecl.HasValue, "stream") ;
    private RDFFormat streamOutput    = null ;
    private RDFFormat formattedOutput = null ;

    @Override
    public void registerWith(CmdGeneral cmdLine) {
        cmdLine.getUsage().startCategory("Output control") ;
        cmdLine.add(argOutput,    "--output=FMT",     "Output in the given format, streaming if possible.") ;
        cmdLine.add(argPretty,    "--formatted=FMT",  "Output, using pretty printing (consumes memory)") ;
        cmdLine.add(argStream,    "--stream=FMT",     "Output, using a streaming format") ;
    }

    @Override
    public void processArgs(CmdArgModule cmdLine) {
        if ( cmdLine.contains(argPretty) ) {
            String langName = cmdLine.getValue(argPretty) ;
            Lang lang = RDFLanguages.nameToLang(langName) ;
            if ( lang == null )
                throw new CmdException("Not recognized as an RDF language : '"+langName+"'") ;
            formattedOutput = RDFWriterRegistry.defaultSerialization(lang) ;
            if ( formattedOutput == null ) {
                System.err.println("Language '"+lang.getLabel()+"' not registered.") ;
                printRegistered(System.err) ;
                throw new CmdException("No output set: '"+langName+"'") ;
            }
        }
        
        if ( cmdLine.contains(argStream) ) {
            String langName = cmdLine.getValue(argStream) ;
            Lang lang = RDFLanguages.nameToLang(langName) ;
            if ( lang == null )
                throw new CmdException("Not recognized as an RDF language : '"+langName+"'") ;
            streamOutput = StreamRDFWriter.defaultSerialization(lang) ;
            if ( streamOutput == null ) {
                System.err.println("Language '"+lang.getLabel()+"' not registered for streaming.") ;
                printRegistered(System.err) ;
                throw new CmdException("No output set: '"+langName+"'") ;
            }
        }
        
        if ( cmdLine.contains(argOutput) ) {
            String langName = cmdLine.getValue(argOutput) ;
            Lang lang = RDFLanguages.nameToLang(langName) ;
            if ( lang == null )
                throw new CmdException("Not recognized as an RDF language : '"+langName+"'") ;
            
            if ( StreamRDFWriter.registered(lang) ) {
                streamOutput = StreamRDFWriter.defaultSerialization(lang) ;
            } else {
                formattedOutput = RDFWriterRegistry.defaultSerialization(lang) ;
                if ( formattedOutput == null ) {
                    System.err.println("Language '"+lang.getLabel()+"' not recognized.") ;
                    printRegistered(System.err) ;
                    throw new CmdException("No output set: '"+langName+"'") ;
                }
            }
        }
        
        if ( streamOutput == null && formattedOutput == null )
            streamOutput = RDFFormat.NQUADS ;
    }

    private static Set<Lang>  hiddenLanguages = new HashSet<>() ;
    static {
        hiddenLanguages.add(Lang.RDFNULL) ;
        hiddenLanguages.add(Lang.CSV) ;
    }
    
    private static void printRegistered(PrintStream out) {
        out.println("Streaming languages:") ;
        Set<Lang> seen = new HashSet<>() ;
        for ( RDFFormat fmt : StreamRDFWriter.registered()) {
            Lang lang = fmt.getLang() ;
            if ( hiddenLanguages.contains(lang)) 
                continue ;
            if ( seen.contains(lang) )
                continue ;
            seen.add(lang) ;
            out.println("   "+lang.getLabel()) ;
        }
        System.err.println("Non-streaming languages:") ;
        for ( RDFFormat fmt : RDFWriterRegistry.registered() ) {
            Lang lang = fmt.getLang() ;
            if ( hiddenLanguages.contains(lang)) 
                continue ;
            if ( seen.contains(lang) )
                continue ;
            seen.add(lang) ;
            out.println("   "+lang.getLabel()) ;
        }
    }
            // Stream-only code.
//            if ( ! StreamRDFWriter.registered(output) ) {
//                // ** Java8
////                StreamRDFWriter.registered().stream()
////                    .map(fmt -> fmt.getLang()) 
////                    .distinct()
////                    .forEach(x -> System.err.println("   "+x.getLabel())) ;
//                
//                System.err.println("Language '"+output.getLabel()+"' can not be used for streamed out (try rdfcat)") ;
//                System.err.println("Streaming languages are:") ;
//                Set<Lang> seen = new HashSet<>() ;
//                for ( RDFFormat fmt : StreamRDFWriter.registered()) {
//                    if ( seen.contains(fmt.getLang()) )
//                        continue ;
//                    seen.add(fmt.getLang()) ;
//                    System.err.println("   "+fmt.getLang().getLabel()) ;
//                }
//                
//                throw new CmdException("Not a streaming RDF language : '"+langName+"'") ;
//            }
//            format = StreamRDFWriter.defaultSerialization(output) ;

    public RDFFormat getOutputStreamFormat() {
        return streamOutput ;
    }
    
    public RDFFormat getOutputFormatted() {
        return formattedOutput ;
    }
}
