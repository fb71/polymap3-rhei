/* 
 * polymap.org
 * Copyright 2012, Polymap GmbH. All rights reserved.
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
package org.polymap.rhei.ide.java;

import java.util.ArrayList;
import java.util.List;

import java.io.File;
import java.net.URI;
import java.net.URL;

import org.osgi.framework.Bundle;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;

import org.polymap.rhei.ide.RheiIdePlugin;
import org.polymap.rhei.ide.java.PluginClasspathDecoder.EntryHandler;

/**
 * Provides classpath entries for all installed plugins.
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public class BundlesClasspathContainer
        implements IClasspathContainer {

    private static Log log = LogFactory.getLog( BundlesClasspathContainer.class );

    private File                homeDir;

    private IClasspathEntry[]   entries;
    
    
    protected BundlesClasspathContainer() {
//        for (Object key : System.getProperties().keySet()) {
//            log.info( key + " : " + System.getProperty( (String)key ) );
//        }
        String home = System.getProperty( "eclipse.home.location" );
        //String home = "/home/falko/servers/polymap3";
        assert home != null;
        if (home.startsWith( "file:" )) {
            homeDir = new File( URI.create( home ) );
        }
        else {
            homeDir = new File( home );
        }
    }
    
    public String getDescription() {
        return "Bundles";
    }

    public int getKind() {
        return K_SYSTEM;
    }

    public IPath getPath() {
        return new Path( BundlesClasspathContainerInitializer.ID );
    }

    
    public IClasspathEntry[] getClasspathEntries() {
        if (entries == null) {
            try {
                entries = computePluginEntries();
            }
            catch (Exception e) {
                log.warn( "", e );
            }
            
        }
        return entries;
    }

//    public IClasspathEntry[] computeEntries2() {
//        List<IClasspathEntry> result = new ArrayList();
//        
//        // find all jars
//        for (Object elm : FileUtils.listFiles( homeDir, new String[] {"jar"}, true )) {
//            File jarFile = (File)elm;
//            //log.info( "Bundle: " + jarFile );
//            result.add( JavaCore.newProjectEntry( 
//                    new Path( jarFile.getAbsolutePath() )/*, null, null*/ ) );
//        }
//        return result.toArray( new IClasspathEntry[ result.size() ] );
//    }
    
    
    private IClasspathEntry[] computePluginEntries() 
    throws Exception {
        final List<IClasspathEntry> result = new ArrayList();
        
        Bundle[] bundles = RheiIdePlugin.getDefault().getBundle().getBundleContext().getBundles();
        for (Bundle bundle : bundles) {
            //http://lmap.blogspot.de/2008/03/platform-scheme-uri.html
            IPath pluginPath = Path.fromPortableString( "platform:/plugin/" + bundle.getSymbolicName() );
            log.info( "Path: " + pluginPath );
            String loc = bundle.getLocation();
            log.info( "Location: " + loc );
            
            if (loc.contains( "/" )) {
                // in development workspace
                if (!loc.endsWith( "jar/" )) {
                    String home = System.getProperty( "user.home" );

                    final IPath filePath = Path.fromOSString( home )
                        .append( Path.fromOSString( StringUtils.substringAfter( loc, "file:" ) ).makeAbsolute() );

                    URL cpres = URI.create( pluginPath.append( ".classpath" ).toPortableString() ).toURL();
                    new PluginClasspathDecoder( cpres ).process( new EntryHandler() {

                        public void handle( String kind, String path, String srcAttach, String javadoc ) {
                            log.info( "   entry: " + path );
                            if (kind.equals( "output")) {
                                result.add( JavaCore.newLibraryEntry( filePath.append( path ), null, null ) );
                            }
                            else if (kind.equals( "lib")) {
                                result.add( JavaCore.newLibraryEntry( filePath.append( path ), null, null ) );                                
                            }
                        }
                    });
                }
                else {
                    String home = System.getProperty( "eclipse.home.location" );
                    
                    String rawPluginPath = StringUtils.substringAfter( loc, "file:" );
                    rawPluginPath = StringUtils.substringBeforeLast( rawPluginPath, "/" );
                    
                    final IPath filePath = Path.fromOSString( StringUtils.substringAfter( home, "file:" ) )
                           .append( Path.fromOSString( rawPluginPath ).makeAbsolute() );
                    log.info( "   plugin path: " + filePath );                                
                    result.add( JavaCore.newLibraryEntry( filePath, null, null ) );                                
                }
            }
        }
        return result.toArray( new IClasspathEntry[ result.size() ] );
    }

    
    
//    private IClasspathEntry[] computePluginEntries() {
//        ArrayList result = new ArrayList();
//        try {
//            StateObjectFactory.defaultFactory.createBundleDescription( )
//            //RheiIdePlugin.getDefault().getBundle().get
//            BundleDescription desc = fModel.getBundleDescription();
//            if (desc == null) {
//                return new IClasspathEntry[0];
//            }
//            Map map = retrieveVisiblePackagesFromState( desc );
//            
//            HashSet added = new HashSet();
//
//            HostSpecification host = desc.getHost();
//            if (desc.isResolved() && host != null) {
//                addHostPlugin( host, added, map, result );
//            }
//
//            // add dependencies
//            for (BundleSpecification required : desc.getRequiredBundles()) {
//                addDependency( getSupplier( required ), added, map, result);
//            }
//            
//            // add Import-Package
//            Iterator iter = map.keySet().iterator();
//            while (iter.hasNext()) {
//                String symbolicName = iter.next().toString();
//                if (symbolicName.equals(desc.getSymbolicName()))
//                    continue;
//                IPluginModelBase model = PDECore.getDefault().getModelManager().findModel(symbolicName);
//                if (model != null && model.isEnabled())
//                    addDependencyViaImportPackage(model.getBundleDescription(), added, map, result);
//            }
//
//            addExtraClasspathEntries(added, result);
//
//            // add implicit dependencies
//            addImplicitDependencies(added, map, result);
//            
//        } catch (CoreException e) {
//        }
//        return (IClasspathEntry[])result.toArray(new IClasspathEntry[result.size()]);
//    }
//    
//    
//    private BundleDescription getSupplier( BundleSpecification spec ) {
//        if (spec.isResolved()) {
//            return (BundleDescription)spec.getSupplier();
//        }
//        IPluginModelBase model = PDECore.getDefault().getModelManager().findModel(spec.getName());
//        return model != null && model.isEnabled() ? model.getBundleDescription() : null;    
//    }
//    
//    
//    private Map retrieveVisiblePackagesFromState( BundleDescription bundle ) {
//        Map visiblePackages = new TreeMap();
//        if (bundle.isResolved()) {
//            BundleDescription desc = bundle;
//            if (desc.getHost() != null)
//                desc = (BundleDescription)desc.getHost().getSupplier();
//
//            StateHelper helper = Platform.getPlatformAdmin().getStateHelper();
//            ExportPackageDescription[] exports = helper.getVisiblePackages( desc );
//            for (int i = 0; i < exports.length; i++) {
//                BundleDescription exporter = exports[i].getExporter();
//                if (exporter == null)
//                    continue;
//                ArrayList list = (ArrayList)visiblePackages.get( exporter.getName() );
//                if (list == null)
//                    list = new ArrayList();
//                list.add( getRule( helper, desc, exports[i] ) );
//                visiblePackages.put( exporter.getName(), list );
//            }
//        }
//        return visiblePackages;
//    }
//
//    
////    private Rule getRule(StateHelper helper, BundleDescription desc, ExportPackageDescription export) {
////        Rule rule = new Rule();
////        rule.discouraged = helper.getAccessCode(desc, export) == StateHelper.ACCESS_DISCOURAGED;
////        rule.path = new Path(export.getName().replaceAll("\\.", "/") + "/*"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
////        return rule;
////    }
//
//    
//    private void addDependencyViaImportPackage( BundleDescription desc, HashSet added, Map map, ArrayList entries) throws CoreException {
//        if (desc == null || !added.add( desc.getSymbolicName() )) {
//            return;
//        }
//        addPlugin( desc, true, map, entries );
//
//        if (hasExtensibleAPI( desc ) && desc.getContainingState() != null) {
//            BundleDescription[] fragments = desc.getFragments();
//            for (int i = 0; i < fragments.length; i++) {
//                if (fragments[i].isResolved())
//                    addDependencyViaImportPackage( fragments[i], added, map, entries );
//            }
//        }
//    }
//
//
//    private void addDependency( BundleDescription desc, HashSet added, Map map, ArrayList entries )
//            throws CoreException {
//        if (desc == null || !added.add( desc.getSymbolicName() )) {
//            return;
//        }
//        addPlugin( desc, true, map, entries );
//
//        if (hasExtensibleAPI( desc ) && desc.getContainingState() != null) {
//            BundleDescription[] fragments = desc.getFragments();
//            for (int i = 0; i < fragments.length; i++) {
//                if (fragments[i].isResolved())
//                    addDependency( fragments[i], added, map, entries );
//            }
//        }
//
//        BundleSpecification[] required = desc.getRequiredBundles();
//        for (int i = 0; i < required.length; i++) {
//            if (required[i].isExported()) {
//                addDependency( getSupplier( required[i] ), added, map, entries );
//            }
//        }
//    }
//
//    
//    private void addPlugin( BundleDescription desc, boolean useInclusions, Map map,
//            ArrayList entries )
//            throws CoreException {
//        IPluginModelBase model = PDECore.getDefault().getModelManager().findModel( desc );
//        if (model == null || !model.isEnabled()) {
//            return;
//        }
//        IResource resource = model.getUnderlyingResource();
//        Rule[] rules = useInclusions ? getInclusions( map, model ) : null;
//        if (resource != null) {
//            addProjectEntry( resource.getProject(), rules, entries );
//        }
//        else {
//            addExternalPlugin( model, rules, entries );
//        }
//    }
//
//    
//    private Rule[] getInclusions(Map map, IPluginModelBase model) {
//        if ("false".equals(System.getProperty("pde.restriction")) //$NON-NLS-1$ //$NON-NLS-2$
//                ||!fModel.getBundleDescription().isResolved())
//            return null;
//        
//        String version = PDECore.getDefault().getTargetVersion();
//        if (version.equals(ICoreConstants.TARGET21) || version.equals(ICoreConstants.TARGET30))
//            return null;
//        
//        BundleDescription desc = model.getBundleDescription();
//        if (desc == null || !desc.isResolved())
//            return null;
//        
//        Rule[] rules;
//
//        if (desc.getHost() != null)
//            rules = getInclusions(map, (BundleDescription)desc.getHost().getSupplier());
//        else
//            rules = getInclusions(map, desc);
//        
//        return (rules.length == 0 && !ClasspathUtilCore.isBundle(model)) ? null : rules;
//    }
//    
//    private Rule[] getInclusions(Map map, BundleDescription desc) {
//        ArrayList list = (ArrayList)map.get(desc.getSymbolicName());
//        return list != null ? (Rule[])list.toArray(new Rule[list.size()]) : new Rule[0];        
//    }
//
//    private void addImplicitDependencies(HashSet added, Map map, ArrayList entries) throws CoreException {
//        String id = fModel.getPluginBase().getId();
//        String schemaVersion = fModel.getPluginBase().getSchemaVersion();
//        boolean isOSGi = TargetPlatform.isOSGi();
//        
//        if ((isOSGi && schemaVersion != null)
//                || id.equals("org.eclipse.core.boot") //$NON-NLS-1$
//                || id.equals("org.apache.xerces") //$NON-NLS-1$
//                || id.startsWith("org.eclipse.swt")) //$NON-NLS-1$
//            return;
//
//        PluginModelManager manager = PDECore.getDefault().getModelManager();
//
//        if (schemaVersion == null && isOSGi) {
//            if (!id.equals("org.eclipse.core.runtime")) { //$NON-NLS-1$
//                IPluginModelBase plugin = manager.findModel(
//                        "org.eclipse.core.runtime.compatibility"); //$NON-NLS-1$
//                if (plugin != null && plugin.isEnabled())
//                    addDependency(plugin.getBundleDescription(), added, map, entries);
//            }
//        } else {
//            IPluginModelBase plugin = manager.findModel("org.eclipse.core.boot"); //$NON-NLS-1$
//            if (plugin != null && plugin.isEnabled())
//                addDependency(plugin.getBundleDescription(), added, map, entries);
//            
//            if (!id.equals("org.eclipse.core.runtime")) { //$NON-NLS-1$
//                plugin = manager.findModel("org.eclipse.core.runtime"); //$NON-NLS-1$
//                if (plugin != null && plugin.isEnabled())
//                    addDependency(plugin.getBundleDescription(), added, map, entries);
//            }
//        }
//    }
//
//    private void addHostPlugin(HostSpecification hostSpec, HashSet added, Map map, ArrayList entries) throws CoreException {
//        BaseDescription desc = hostSpec.getSupplier();
//        
//        if (desc instanceof BundleDescription && added.add(desc.getName())) {
//            BundleDescription host = (BundleDescription)desc;
//            // add host plug-in
//            addPlugin(host, false, map, entries);
//            
//            BundleSpecification[] required = host.getRequiredBundles();
//            for (int i = 0; i < required.length; i++) {
//                desc = getSupplier(required[i]);
//                if (desc != null && desc instanceof BundleDescription) {
//                    addDependency((BundleDescription)desc, added, map, entries);
//                }
//            }
//        }
//    }
//    
//    private boolean hasExtensibleAPI(BundleDescription desc) {
//        IPluginModelBase model = PDECore.getDefault().getModelManager().findModel(desc);
//        return (model instanceof IPluginModel) 
//                    ? ClasspathUtilCore.hasExtensibleAPI(((IPluginModel)model).getPlugin()) 
//                    : false;
//    }
//    
//    protected void addExtraClasspathEntries(HashSet added, ArrayList entries) throws CoreException {
//        IBuild build = ClasspathUtilCore.getBuild(fModel);
//        IBuildEntry entry = (build == null) ? null : build.getEntry(IBuildEntry.JARS_EXTRA_CLASSPATH);
//        if (entry == null)
//            return;
//
//        String[] tokens = entry.getTokens();
//        for (int i = 0; i < tokens.length; i++) {
//            IPath path = Path.fromPortableString(tokens[i]);
//            if (!path.isAbsolute()) {
//                File file = new File(fModel.getInstallLocation(), path.toString());
//                if (file.exists()) {
//                    IFile resource = PDECore.getWorkspace().getRoot().getFileForLocation(new Path(file.getAbsolutePath()));
//                    if (resource != null && resource.getProject().equals(fModel.getUnderlyingResource().getProject())) {
//                        addExtraLibrary(resource.getFullPath(), null, entries);
//                        continue;
//                    }
//                }
//                if (path.segmentCount() >= 3 && "..".equals(path.segment(0))) { //$NON-NLS-1$
//                    path = path.removeFirstSegments(1);
//                    path = Path.fromPortableString("platform:/plugin/").append(path); //$NON-NLS-1$ 
//                } else {
//                    continue;
//                }
//            }
//            
//            if (!path.toPortableString().startsWith("platform:")) { //$NON-NLS-1$
//                File file = new File(path.toOSString());
//                if (file.exists()) {
//                    addExtraLibrary(path, null, entries);           
//                }
//            } else {
//                int count = path.getDevice() == null ? 4 : 3;
//                if (path.segmentCount() >= count) {
//                    String pluginID = path.segment(count-2);
//                    if (added.contains(pluginID))
//                        continue;
//                    IPluginModelBase model = PDECore.getDefault().getModelManager().findModel(pluginID);
//                    if (model != null && model.isEnabled()) {
//                        path = path.setDevice(null);
//                        path = path.removeFirstSegments(count-1);
//                        if (model.getUnderlyingResource() == null) {
//                            File file = new File(model.getInstallLocation(), path.toOSString());
//                            if (file.exists()) {
//                                addExtraLibrary(new Path(file.getAbsolutePath()), model, entries);
//                            }
//                        } else {
//                            IProject project = model.getUnderlyingResource().getProject();
//                            IFile file = project.getFile(path);
//                            if (file.exists()) {
//                                addExtraLibrary(file.getFullPath(), model, entries);
//                            }
//                        }
//                    }
//                }
//            }                       
//        }   
//    }
//    
//    private void addExtraLibrary(IPath path, IPluginModelBase model, ArrayList entries) throws CoreException {
//        IPath srcPath = null;
//        if (model != null) {
//            IPath shortPath = path.removeFirstSegments(path.matchingFirstSegments(new Path(model.getInstallLocation())));
//            srcPath = ClasspathUtilCore.getSourceAnnotation(model, shortPath.toString());
//        }
//        IClasspathEntry clsEntry = JavaCore.newLibraryEntry(
//                path,
//                srcPath,
//                null);
//        if (!entries.contains(clsEntry))
//            entries.add(clsEntry);                      
//    }
    
}