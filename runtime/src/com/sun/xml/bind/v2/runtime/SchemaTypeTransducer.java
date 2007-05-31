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

package com.sun.xml.bind.v2.runtime;

import java.io.IOException;

import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import com.sun.xml.bind.api.AccessorException;

import org.xml.sax.SAXException;

/**
 * {@link Transducer} that signals the runtime that this datatype
 * is marshalled to a different XML Schema type.
 *
 * <p>
 * This transducer is used to implement the semantics of {@link XmlSchemaType} annotation.
 *
 *
 * @see XMLSerializer#schemaType
 * @author Kohsuke Kawaguchi
 */
public class SchemaTypeTransducer<V> extends FilterTransducer<V> {
    private final QName schemaType;

    public SchemaTypeTransducer(Transducer<V> core, QName schemaType) {
        super(core);
        this.schemaType = schemaType;
    }

    @Override
    public CharSequence print(V o) throws AccessorException {
        XMLSerializer w = XMLSerializer.getInstance();
        QName old = w.setSchemaType(schemaType);
        try {
            return core.print(o);
        } finally {
            w.setSchemaType(old);
        }
    }

    @Override
    public void writeText(XMLSerializer w, V o, String fieldName) throws IOException, SAXException, XMLStreamException, AccessorException {
        QName old = w.setSchemaType(schemaType);
        try {
            core.writeText(w,o,fieldName);
        } finally {
            w.setSchemaType(old);
        }
    }

    @Override
    public void writeLeafElement(XMLSerializer w, Name tagName, V o, String fieldName) throws IOException, SAXException, XMLStreamException, AccessorException {
        QName old = w.setSchemaType(schemaType);
        try {
            core.writeLeafElement(w, tagName, o, fieldName);
        } finally {
            w.setSchemaType(old);
        }
    }
}
