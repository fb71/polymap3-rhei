/*
 * polymap.org
 * Copyright 2010-2013, Falko Br�utigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.rhei.field;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import org.polymap.rhei.form.IFormEditorToolkit;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public class TextFormField
        extends StringFormField {

    public TextFormField( Style... styles ) {
        super( styles );
    }

    public void init( IFormFieldSite _site ) {
        super.init( _site );
    }
    
    protected Control createControl( Composite parent, IFormEditorToolkit toolkit, int style ) {
        Text text = (Text)super.createControl( parent, toolkit, style | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL );
        
        FormData layoutData = new FormData();
//        layoutData.height = 75;
        layoutData.top = new FormAttachment( 0, 0 );
        layoutData.bottom = new FormAttachment( 100, -3 );
        text.setLayoutData( layoutData );

//        text.setSize( SWT.DEFAULT, minHeight );
        return text;
    }

}
