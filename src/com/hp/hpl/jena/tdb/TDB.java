/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP}
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.impl.RDFReaderFImpl;

import com.hp.hpl.jena.sparql.engine.main.OpCompiler;
import com.hp.hpl.jena.sparql.engine.main.StageGenBasicPattern;
import com.hp.hpl.jena.sparql.engine.main.StageGenerator;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.sparql.util.Symbol;

import com.hp.hpl.jena.query.ARQ;

import com.hp.hpl.jena.tdb.base.loader.NTriplesReader2;
import com.hp.hpl.jena.tdb.pgraph.assembler.PGraphAssemblerVocab;
import com.hp.hpl.jena.tdb.solver.OpCompilerTDB;
import com.hp.hpl.jena.tdb.solver.StageGeneratorDirectTDB;
import com.hp.hpl.jena.tdb.solver.StageGeneratorGeneric;
import com.hp.hpl.jena.tdb.sys.Metadata;
import com.hp.hpl.jena.tdb.sys.SystemTDB;


public class TDB
{
    private static final Logger log = LoggerFactory.getLogger(TDB.class) ;
    
    public static String namespace = "http://jena.hpl.hp.com/2008/tdb#" ;

    /** Root of TDB-defined parameter names */
    public static final String symbolNamespace = "http://jena.hpl.hp.com/TDB#" ;
    
    /** Root of TDB-defined parameter short names */  
    public static final String tdbSymbolPrefix = "tdb" ;

    public static final Symbol logBGP               = SystemTDB.allocSymbol("logBGP") ;
    public static final Symbol logDuplicates        = SystemTDB.allocSymbol("logDuplicates") ;
    // Value: direct, mapped, default 
    public static final Symbol symFileMode          = SystemTDB.allocSymbol("fileMode") ;  
    public static final Symbol symIndexType         = SystemTDB.allocSymbol("indexType") ;
    
    public static Context getContext()     { return ARQ.getContext() ; }  
    
    public static void optimizerOn()    { optimizer(true) ; }
    public static void optimizerOff()   { optimizer(false) ; }
    public static void optimizer(boolean b)
    { }
    
    // Called on assembler loading.
    /** TDB System initialization - normally, this is not explicitly called because
     * all routes to use TDB will cause initialization to occur.  However, calling it
     * repeatedly is safe and low cost.
     */
    public static void init() { }
    
    static { initWorker() ; }
    
    private static boolean initialized = false ;
    private static synchronized void initWorker()
    {
        if ( initialized )
            return ;
        initialized = true ;

        //QueryEngineTDB.register() ;
        
        //Gets in the way!
        ARQ.getContext().set(ARQ.filterPlacement, false) ;
        
        PGraphAssemblerVocab.init();
        
        wireStageGenerator() ;
        
        // Override N-TRIPLES
        String bulkLoaderClass = NTriplesReader2.class.getName() ;
        RDFReaderFImpl.setBaseReaderClassName("N-TRIPLES", bulkLoaderClass) ;
        RDFReaderFImpl.setBaseReaderClassName("N-TRIPLE", bulkLoaderClass) ;
        
        if ( log.isDebugEnabled() )
            log.debug("\n"+ARQ.getContext()) ;
    }
    
    private static void wireStageGenerator()
    {
        // Globally change the stage generator to intercept BGP on TDB
        StageGenerator orig = (StageGenerator)ARQ.getContext().get(ARQ.stageGenerator) ;
        
        if ( orig instanceof StageGenBasicPattern )
            // ARQ base.  Cause chaos by using the new version.
            orig = new StageGeneratorGeneric() ;

        // Wire in the new 
        OpCompiler.factory = OpCompilerTDB.altFactory ;
        StageGenerator stageGenerator = new StageGeneratorDirectTDB(orig) ;
        ARQ.getContext().set(ARQ.stageGenerator, stageGenerator) ;
    }
    
    /** The root package name for TDB */   
    public static final String PATH = "com.hp.hpl.jena.tdb";

    // The names known to ModVersion : "NAME", "VERSION", "BUILD_DATE"
    
    public static final String NAME = "TDB" ;
    
    /** The full name of the current TDB version */   
    public static final String VERSION = Metadata.get(PATH+".version", "unknown") ;
//   
//    /** The major version number for this release of TDB (ie '2' for TDB 2.0) */
//    public static final String MAJOR_VERSION = "@version-major@";
//   
//    /** The minor version number for this release of TDB (ie '0' for TDB 2.0) */
//    public static final String MINOR_VERSION = "@version-minor@";
//   
//    /** The version status for this release of SDB (eg '-beta1' or the empty string) */
//    public static final String VERSION_STATUS = "@version-status@";
//   
    /** The date and time at which this release was built */   
    public static final String BUILD_DATE = Metadata.get(PATH+".build.datetime", "unset") ;
}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */