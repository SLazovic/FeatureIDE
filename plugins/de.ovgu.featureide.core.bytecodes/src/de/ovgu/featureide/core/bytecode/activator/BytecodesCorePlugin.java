package de.ovgu.featureide.core.bytecode.activator;
import org.osgi.framework.BundleContext;

import de.ovgu.featureide.fm.core.AbstractCorePlugin;

public class BytecodesCorePlugin extends AbstractCorePlugin{
	
	public static final String PLUGIN_ID = "de.ovgu.featureide.core.Bytecode";
	private static BytecodesCorePlugin plugin;
	
	@Override
	public String getID() {
		return PLUGIN_ID;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}
	
	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static BytecodesCorePlugin getDefault() {
		return plugin;
	}

}
