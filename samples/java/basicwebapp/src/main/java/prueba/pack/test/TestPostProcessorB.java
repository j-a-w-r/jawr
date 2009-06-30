package prueba.pack.test;

import net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;
import net.jawr.web.resource.bundle.postprocess.ResourceBundlePostProcessor;

public class TestPostProcessorB implements ResourceBundlePostProcessor {

	public StringBuffer postProcessBundle(BundleProcessingStatus status,
			StringBuffer bundleString) {
		return bundleString.append(";alert('postprocessor B');");
	}

}
