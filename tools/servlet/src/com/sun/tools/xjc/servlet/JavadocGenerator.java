/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.sun.tools.xjc.servlet;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates Javadoc from all the source code in a zip file.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class JavadocGenerator {
    private final File outDir;
    private final List args = new ArrayList();
    
    /**
     * Executes javadoc for source code in the "inDir" directory
     * and produces HTML t the "outDir" directory.
     * Any message from javadoc will be sent to the "out" stream. 
     */
    public static int process( File inDir, File outDir, OutputStream out ) throws Exception {
        return new JavadocGenerator(outDir)._process(inDir,out);
    }
    
    public JavadocGenerator( File _outDir ) throws IOException {
        this.outDir = _outDir;
        
        args.add("javadoc");
        args.add("-d");
        args.add( outDir.getPath() );
        args.add("-linkoffline");
        args.add("http://java.sun.com/webservices/docs/1.1/api/");
        args.add(getPackageListLocation("javadoc/jwsdp/package-list"));
        args.add("-linkoffline");
        args.add("http://java.sun.com/j2se/1.4/docs/api"); 
        args.add(getPackageListLocation("javadoc/j2se/package-list"));
    }

    private String getPackageListLocation(String path) throws IOException {
        String name = this.getClass().getResource(path).toExternalForm();
        return name.substring(0, name.length()-12/*lengthOf("package-list")*/);
    }
    
    private int _process( File inDir, OutputStream out ) throws Exception {
        listJavaFiles(inDir);

        Process proc = Runtime.getRuntime().exec( (String[]) args.toArray(new String[args.size()]) );
        return execProcess(proc,out);
    }

    private void listJavaFiles( File dir ) throws IOException {
        File[] files = dir.listFiles();
        
        for( int i=0; i<files.length; i++ ) {
            File f = files[i];
            if( f.isDirectory() )
                listJavaFiles(f);
            else
            if( f.getName().endsWith(".java") )
                args.add( f.getPath() );
        }
    }
    
    
    /**
     * Waits for the given process to complete, and return its exit code.
     */
    private static int execProcess( Process proc, OutputStream out ) throws IOException, InterruptedException {
        // is this a correct handling?
        proc.getOutputStream().close();
        new Thread(new ProcessReader(proc.getInputStream(),out)).start();
        new Thread(new ProcessReader(proc.getErrorStream(),out)).start();
            
        return proc.waitFor();
    }

    /**
     * Reads an input stream and copies them into the given output stream.
     */
    private static class ProcessReader implements Runnable {

        ProcessReader( InputStream is, OutputStream out ) {
            this.in = new BufferedInputStream(is);
            this.out = out;
        }
        
        private final BufferedInputStream in;
        private final OutputStream out;
        
        public void run() {
            try {
                byte[] buf = new byte[256];
                while(true) {
                    int len = in.read(buf);
                    if(len==-1) {
                        in.close();
                        // don't close "out".
                        return;
                    }
                    out.write(buf,0,len);
                }
            } catch( Exception e ) {
                e.printStackTrace();
                throw new Error();
            }
        }
    }
    
    /**
     * For quick testing.
     */
    public static void main( String[] args ) throws Exception {
        JavadocGenerator.process( new File(args[0]), new File(args[1]), System.out );
    }
    
}
