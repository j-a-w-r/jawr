import net.jawr.web.resource.bundle.renderer.JavascriptHTMLBundleLinkRenderer;
import net.jawr.web.resource.bundle.renderer.CSSHTMLBundleLinkRenderer;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.jawr.web.servlet.RendererRequestUtils;

class JawrTagLib {
	static namespace = "jawr"
	
	def script = { attrs ->
		boolean useRandomParam = null == attrs['useRandomParam'] ? true : attrs['useRandomParam'];
		def renderer = new JavascriptHTMLBundleLinkRenderer(servletContext[ResourceBundlesHandler.JS_CONTEXT_ATTRIBUTE],useRandomParam);
		boolean isGzippable = RendererRequestUtils.isRequestGzippable(request,renderer.getBundler().getConfig());
		
		renderer.renderBundleLinks(attrs['src'], request.getContextPath(), RendererRequestUtils.getAddedBundlesLog(request), isGzippable,out);
	}
	
	def style = { attrs ->
		boolean useRandomParam = null == attrs['useRandomParam'] ? true : attrs['useRandomParam'];
		def renderer = new CSSHTMLBundleLinkRenderer(servletContext[ResourceBundlesHandler.CSS_CONTEXT_ATTRIBUTE],useRandomParam,attrs['media']);
		boolean isGzippable = RendererRequestUtils.isRequestGzippable(request,renderer.getBundler().getConfig());
		
		renderer.renderBundleLinks(attrs['src'], request.getContextPath(), RendererRequestUtils.getAddedBundlesLog(request), isGzippable,out);
	}
	
}