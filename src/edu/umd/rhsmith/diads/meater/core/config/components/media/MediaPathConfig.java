package edu.umd.rhsmith.diads.meater.core.config.components.media;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;

import edu.umd.rhsmith.diads.meater.core.app.MEaterConfigurationException;
import edu.umd.rhsmith.diads.meater.core.app.components.media.MediaClassNotFoundException;
import edu.umd.rhsmith.diads.meater.core.app.components.media.MediaPath;
import edu.umd.rhsmith.diads.meater.core.app.components.media.MediaPathInitializer;
import edu.umd.rhsmith.diads.meater.core.config.components.ComponentConfig;
import edu.umd.rhsmith.diads.meater.core.config.setup.ops.media.path.PathDescriptionOperation;
import edu.umd.rhsmith.diads.meater.core.config.setup.ops.media.path.PathProcessorAddOperation;
import edu.umd.rhsmith.diads.meater.core.config.setup.ops.media.path.PathProcessorListOperation;
import edu.umd.rhsmith.diads.meater.core.config.setup.ops.media.path.PathProcessorRemoveOperation;
import edu.umd.rhsmith.diads.meater.core.config.setup.ops.media.path.PathRejectableOperation;
import edu.umd.rhsmith.diads.meater.core.config.setup.ops.media.path.PathSetMediaOperation;
import edu.umd.rhsmith.diads.meater.core.config.setup.ops.media.path.PathSourceAddOperation;
import edu.umd.rhsmith.diads.meater.core.config.setup.ops.media.path.PathSourceListOperation;
import edu.umd.rhsmith.diads.meater.core.config.setup.ops.media.path.PathSourceRemoveOperation;

public final class MediaPathConfig extends ComponentConfig {

	public static final String CKEY_SOURCE = "source";
	public static final String CKEY_PROCESSOR = "processor";
	public static final String CKEY_MEDIA_CLASS = "mediaClass";
	public static final String CKEY_REJECTABLE = "rejectable";
	public static final String CKEY_DESCR = "description";

	private Class<?> mediaClass;
	private final Collection<String> sources;
	private final List<String> processors;
	public boolean rejectable;

	private String description;

	MediaPathConfig() {
		super();

		this.sources = new HashSet<String>();
		this.processors = new ArrayList<String>();
		this.setMediaClass(null);
		this.setDescription("");

		// manipulate processors
		this.registerSetupConsoleOperation(new PathProcessorAddOperation(this));
		this.registerSetupConsoleOperation(new PathProcessorRemoveOperation(
				this));
		this.registerSetupConsoleOperation(new PathProcessorListOperation(this));

		// manipulate sources
		this.registerSetupConsoleOperation(new PathSourceAddOperation(this));
		this.registerSetupConsoleOperation(new PathSourceRemoveOperation(this));
		this.registerSetupConsoleOperation(new PathSourceListOperation(this));

		// media types
		this.setCreationSetupConsoleOperation(new PathSetMediaOperation(this));
		this.registerSetupConsoleOperation(this
				.getCreationSetupConsoleOperation());

		// misc
		this.registerSetupConsoleOperation(new PathRejectableOperation(this));
		this.registerSetupConsoleOperation(new PathDescriptionOperation(this));
	}

	@Override
	public MediaPath<?> instantiateComponent()
			throws MEaterConfigurationException {
		// use these intermediate steps to maintain type safety - parameter M is
		// inferred from generic wildcard of current media class
		if (this.mediaClass != null) {
			return this.createTypeSafe(this.mediaClass);
		} else {
			throw new MEaterConfigurationException(String.format(
					MSG_ERR_NO_CLASS, this.getInstanceName()));
		}
	}

	private <M> MediaPath<M> createTypeSafe(Class<M> mediaClass)
			throws MEaterConfigurationException {
		return new MediaPath<M>(this.new Initializer<M>(mediaClass));
	}

	private class Initializer<M> implements MediaPathInitializer<M> {
		private Class<M> mediaClass;

		public Initializer(Class<M> mediaClass) {
			this.mediaClass = mediaClass;
		}

		@Override
		public String getInstanceName() {
			return MediaPathConfig.this.getInstanceName();
		}

		@Override
		public Class<M> getMediaClass() {
			return mediaClass;
		}

		@Override
		public Collection<String> getSourceNames() {
			return MediaPathConfig.this.getSources();
		}

		@Override
		public Collection<String> getProcessorNames() {
			return MediaPathConfig.this.getProcessors();
		}

		@Override
		public boolean isRejectable() {
			return MediaPathConfig.this.rejectable;
		}
	}

	/*
	 * --------------------------------
	 * General getters & setters
	 * --------------------------------
	 */

	public Class<?> getMediaClass() {
		return mediaClass;
	}

	public void setMediaClass(Class<?> mediaClass) {
		this.mediaClass = mediaClass;
		if (mediaClass != null) {
			this.registerMediaProcessorName("", mediaClass);
		} else {
			this.unregisterMediaProcessorName("");
		}
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) throws NullPointerException {
		if (description == null) {
			throw new NullPointerException();
		}

		this.description = description;
	}

	public boolean isRejectable() {
		return rejectable;
	}

	public void setRejectable(boolean rejectable) {
		this.rejectable = rejectable;
	}

	/*
	 * --------------------------------
	 * Handlers & sources
	 * --------------------------------
	 */

	public boolean addProcessor(String name) {
		return this.processors.add(name);
	}

	public void addProcessor(int index, String name) {
		this.processors.add(index, name);
	}

	public boolean removeProcessor(String name) {
		return this.processors.remove(name);
	}

	public List<String> getProcessors() {
		return new ArrayList<String>(this.processors);
	}

	public int getNumProcessors() {
		return this.processors.size();
	}

	public boolean addSource(String name) {
		return this.sources.add(name);
	}

	public boolean removeSource(String name) {
		return this.sources.remove(name);
	}

	public List<String> getSources() {
		return new ArrayList<String>(this.sources);
	}

	public int getNumSources() {
		return this.sources.size();
	}

	/*
	 * --------------------------------
	 * UI
	 * --------------------------------
	 */

	@Override
	public String getUiName() {
		if (this.mediaClass != null) {
			return String.format(MSG_OPERATES_ON_FMT, super.getUiName(),
					this.mediaClass.getName());
		} else {
			return String.format(MSG_OPERATES_ON_NONE_FMT, super.getUiName());
		}
	}

	@Override
	public String getUiDescription() {
		return this.getDescription();
	}

	/*
	 * --------------------------------
	 * Config operations
	 * --------------------------------
	 */

	@Override
	public void resetInternalConfiguration() {
		this.sources.clear();
		this.processors.clear();
		this.rejectable = false;
		this.description = "";
	}

	@Override
	protected void loadInternalConfigurationFrom(
			HierarchicalConfiguration config)
			throws MEaterConfigurationException {
		super.loadInternalConfigurationFrom(config);

		this.description = config.getString(CKEY_DESCR, this.description);
		this.rejectable = config.getBoolean(CKEY_REJECTABLE, this.rejectable);

		for (String s : config.getStringArray(CKEY_SOURCE)) {
			this.sources.add(s);
		}
		for (String s : config.getStringArray(CKEY_PROCESSOR)) {
			this.processors.add(s);
		}

		loadMediaClass(config);
	}

	@Override
	protected void saveInternalConfigurationTo(HierarchicalConfiguration config)
			throws MEaterConfigurationException {
		super.saveInternalConfigurationTo(config);

		config.setProperty(CKEY_REJECTABLE, this.rejectable);
		config.setProperty(CKEY_DESCR, this.description);

		for (String s : this.sources) {
			config.addProperty(CKEY_SOURCE, s);
		}
		for (String s : this.processors) {
			config.addProperty(CKEY_PROCESSOR, s);
		}
		saveMediaClass(config);
	}

	private void loadMediaClass(HierarchicalConfiguration config)
			throws MediaClassNotFoundException {

		String className = config.getString(CKEY_MEDIA_CLASS);
		if (className != null) {
			try {
				Class<?> mediaClass = Class.forName(className);
				this.setMediaClass(mediaClass);
			} catch (ClassNotFoundException e) {
				throw new MediaClassNotFoundException(className);
			}
		}
	}

	private void saveMediaClass(HierarchicalConfiguration config) {
		if (this.mediaClass != null) {
			config.addProperty(CKEY_MEDIA_CLASS, this.mediaClass
					.getCanonicalName());
		}
	}

	/*
	 * --------------------------------
	 * Config type registration
	 * --------------------------------
	 */

	public static final String TNAME = "MediaPath";
	public static final String TDESC = "Define a mapping of media source inputs to media processors";

	/*
	 * --------------------------------
	 * Messages
	 * --------------------------------
	 */

	private static final String MSG_ERR_NO_CLASS = "Media path %s has no assigned media class, cannot instantiate.";
	private static final String MSG_OPERATES_ON_FMT = "%s (operates on %s)";
	private static final String MSG_OPERATES_ON_NONE_FMT = "%s (no media class set)";
}
