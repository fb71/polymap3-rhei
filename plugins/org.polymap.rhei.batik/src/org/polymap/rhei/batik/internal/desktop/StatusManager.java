/* 
 * polymap.org
 * Copyright (C) 2013, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.rhei.batik.internal.desktop;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.rwt.RWT;
import org.eclipse.rwt.lifecycle.WidgetUtil;

import org.eclipse.jface.action.ContributionItem;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;

import org.polymap.rhei.batik.BatikPlugin;
import org.polymap.rhei.batik.IPanel;
import org.polymap.rhei.batik.PanelChangeEvent;
import org.polymap.rhei.batik.PanelChangeEvent.TYPE;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class StatusManager
        extends ContributionItem {
        //implements IStatusLineManager {

    private static Log log = LogFactory.getLog( StatusManager.class );
    
    private DesktopAppManager       appManager;

    private Composite               contents;

    private List<PanelChangeEvent>  pendingStartEvents = new ArrayList();

    private IPanel                  activePanel;

    private Label                   iconLabel;
    
    private Shell                   tooltip;

    private Label                   tooltipMsg;

    private Label                   tooltipTxt;

    private Label                   tooltipIcon;


    public StatusManager( DesktopAppManager appManager ) {
        this.appManager = appManager;

        appManager.getContext().addEventHandler( this, new EventFilter<PanelChangeEvent>() {
            public boolean apply( PanelChangeEvent input ) {
                return input.getType() == TYPE.ACTIVATED || input.getType() == TYPE.STATUS;
            }
        });
    }

    
    @Override
    public void fill( Composite parent ) {
        this.contents = parent;
        contents.setLayout( FormLayoutFactory.defaults().margins( 0, 3 ).create() );

        iconLabel = new Label( contents, SWT.NONE );
        iconLabel.setLayoutData( FormDataFactory.filled().top( 0, 2 ).right( -1 ).width( 25 ).create() );
        iconLabel.setText( "..." );

        Label sep2 = new Label( contents, SWT.SEPARATOR | SWT.VERTICAL );
        sep2.setLayoutData( FormDataFactory.filled().left( -1 ).create() );
        
        // fire pending events
        for (PanelChangeEvent ev : pendingStartEvents) {
            panelChanged( ev );
        }
        pendingStartEvents.clear();
    }


    @EventHandler(display=true)
    protected void panelChanged( PanelChangeEvent ev ) {
        if (contents == null) {
            pendingStartEvents.add( ev );
            return;
        }

        if (ev.getType() == TYPE.ACTIVATED) {
            activePanel = ev.getSource();
            IStatus status = activePanel.getSite().getStatus();
            // restore status only if it is not 'OK'
            update( status.isOK() ? null : status, false );
        }
        else if (ev.getType() == TYPE.STATUS) {
            update( activePanel.getSite().getStatus(), true );
        }
    }


//    public IProgressMonitor getProgressMonitor() {
//        // XXX Auto-generated method stub
//        throw new RuntimeException( "not yet implemented." );
//    }
//
//    public boolean isCancelEnabled() {
//        // XXX Auto-generated method stub
//        throw new RuntimeException( "not yet implemented." );
//    }
//
//    public void setCancelEnabled( boolean enabled ) {
//        // XXX Auto-generated method stub
//        throw new RuntimeException( "not yet implemented." );
//    }


    protected void update( IStatus status, boolean popup ) {
        iconLabel.setImage( null );
        iconLabel.setToolTipText( null );
        
        if (status != null && status != Status.OK_STATUS) {
            // init tip after contents was layed out
            if (tooltip == null) {
                // http://hnvcam.blogspot.de/2010/04/swt-create-fake-tooltip.html
                tooltip = new Shell( contents.getShell(), SWT.TOOL | SWT.ON_TOP);
                tooltip.setLayout( FormLayoutFactory.defaults().spacing( 5 ).create() );
                tooltip.setData( WidgetUtil.CUSTOM_VARIANT, "batik-status-tooltip" );

                tooltipIcon = new Label( tooltip, SWT.NONE );
                tooltipIcon.setLayoutData( FormDataFactory.filled().top( 0, -3 ).bottom( -1 ).right( -1 ).create() );
                tooltipIcon.setData( WidgetUtil.CUSTOM_VARIANT, "batik-status-tooltip-icon" );

                tooltipTxt = new Label( tooltip, SWT.NONE );
                tooltipTxt.setLayoutData( FormDataFactory.filled().left( tooltipIcon ).bottom( -1 ).create() );
                tooltipTxt.setData( RWT.MARKUP_ENABLED, Boolean.TRUE );
                tooltipTxt.setData( WidgetUtil.CUSTOM_VARIANT, "batik-status-tooltip-text" );

                tooltipMsg = new Label( tooltip, SWT.WRAP );
                tooltipMsg.setLayoutData( FormDataFactory.filled().top( tooltipTxt ).create() );
                //tooltipMsg.setData( RWT.MARKUP_ENABLED, Boolean.TRUE );
                //tooltipMsg.setData( MarkupValidator.MARKUP_VALIDATION_DISABLED, Boolean.TRUE );
                tooltipMsg.setData( WidgetUtil.CUSTOM_VARIANT, "batik-status-tooltip-message" );
                
                tooltip.addMouseListener( new MouseAdapter() {
                    public void mouseUp( MouseEvent ev ) {
                        tooltip.dispose(); 
                        tooltip = null;
                    }
                });
            }

            // process markdown
            String msg = status.getMessage();
//            String msg = status.getMessage() != null
//                    ? new PegDownProcessor().markdownToHtml( status.getMessage() ) : null;
            tooltipMsg.setText( msg );
            
            // update tooltip location/size
            Point txtSize = tooltip.computeSize( 320, SWT.DEFAULT );
            Point iconLocation = iconLabel.getParent().getLocation();
            int displayWidth = tooltip.getDisplay().getClientArea().width;
            tooltip.setBounds( displayWidth - 325, iconLocation.y + 98, 320, txtSize.y + 5 );
            tooltip.setVisible( true );
            
            tooltip.getDisplay().timerExec( 10000, new Runnable() {
                public void run() {
                    tooltip.dispose(); 
                    tooltip = null;
                }
            });

            //
            iconLabel.setToolTipText( status.getMessage() );

            switch (status.getSeverity()) {
                case IStatus.OK: {
                    if (status != Status.OK_STATUS && msg != null) {
                        iconLabel.setImage( BatikPlugin.instance().imageForName( "resources/icons/ok-status.gif" ) );
                    }
                    tooltipIcon.setImage( BatikPlugin.instance().imageForName( "resources/icons/ok-status.gif" ) );
                    tooltip.setText( "Aktion war erfolgreich" );
                    tooltipTxt.setText( "Aktion war erfolgreich" );
                    break;
                }
                case IStatus.ERROR: {
                    iconLabel.setImage( BatikPlugin.instance().imageForName( "resources/icons/errorstate.gif" ) );
                    tooltipIcon.setImage( BatikPlugin.instance().imageForName( "resources/icons/errorstate.gif" ) );
                    tooltip.setText( "Ein Problem ist aufgetreten" );
                    tooltipTxt.setText( "Ein Problem ist aufgetreten" );
                    break;
                }
                case IStatus.WARNING: {
                    iconLabel.setImage( BatikPlugin.instance().imageForName( "resources/icons/warningstate.gif" ) );
                    tooltipIcon.setImage( BatikPlugin.instance().imageForName( "resources/icons/warningstate.gif" ) );
                    tooltip.setText( "Achtung" );
                    tooltipTxt.setText( "Achtung" );
                    break;
                }
                case IStatus.INFO: {
                    iconLabel.setImage( BatikPlugin.instance().imageForName( "resources/icons/info.png" ) );
                    tooltipIcon.setImage( BatikPlugin.instance().imageForName( "resources/icons/info.png" ) );
                    tooltip.setText( "Hinweis" );
                    tooltipTxt.setText( "Hinweis" );
                    break;
                }
            }

            if (popup) {
                tooltip.setVisible( true );
            }
        }
    }

}
