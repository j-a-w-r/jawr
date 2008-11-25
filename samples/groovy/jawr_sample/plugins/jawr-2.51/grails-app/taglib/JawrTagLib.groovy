import net.jawr.web.resource.bundle.renderer.JavascriptHTMLBundleLinkRenderer;
import net.jawr.web.resource.bundle.renderer.CSSHTMLBundleLinkRenderer;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.jawr.web.servlet.RendererRequestUtils;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * Jawr tag library, uses Jawr Renderer object to delegate content generation. 
 */
class JawrTagLib {
	static namespace = "jawr"
	
	/**
	 * script Tag: generates javascript script tags. 
	 * Attributes: 
	 * src (required): Set the source of the resource or bundle to retrieve. 
	 * useRandomParam (optional, default true): Set wether random param will be added in development mode to generated urls. 
	 */
	def script = { attrs ->
		boolean useRandomParam = null == attrs['useRandomParam'] ? true : attrs['useRandomParam'];
		def renderer = new JavascriptHTMLBundleLinkRenderer(servletContext[ResourceBundlesHandler.JS_CONTEXT_ATTRIBUTE],useRandomParam);
		boolean isGzippable = RendererRequestUtils.isRequestGzippable(request,renderer.getBundler().getConfig());
		def locale = RequestContextUtils.getLocale(request).toString();
		
		renderer.renderBundleLinks(attrs['src'], request.getContextPath(), locale, RendererRequestUtils.getAddedBundlesLog(request), isGzippable,out);
	}
	
	/**
	 * style Tag: generates CSS link tags
	 * Attributes: 
	 * src (required): Set the source of the resource or bundle to retrieve. 
	 * media(optional, default 'screen'):Set the media attribute to use in the link tag. 
	 * useRandomParam (optional, default true): Set wether random param will be added in development mode to generated urls. 
	 */ 
	def style = { attrs ->
		boolean useRandomParam = null == attrs['useRandomParam'] ? true : attrs['useRandomParam'];
		def renderer = new CSSHTMLBundleLinkRenderer(servletContext[ResourceBundlesHandler.CSS_CONTEXT_ATTRIBUTE],useRandomParam,attrs['media']);
		boolean isGzippable = RendererRequestUtils.isRequestGzippable(request,renderer.getBundler().getConfig());
		def locale = RequestContextUtils.getLocale(request).toString();
		
		renderer.renderBundleLinks(attrs['src'], request.getContextPath(), locale, RendererRequestUtils.getAddedBundlesLog(request), isGzippable,out);
	}
	
}