package com.cyanoth.secretwarden.config.UI;

import java.io.IOException;
import java.net.URI;

import javax.inject.Inject;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.soy.renderer.SoyTemplateRenderer;
import com.atlassian.webresource.api.assembler.PageBuilderService;

/**
 * Servlet for the global configuration (administrator) page.
 * Validate permission & server-side render a soy template upon an API request
 * [1] https://developer.atlassian.com/server/framework/atlassian-sdk/creating-an-admin-configuration-form/
 */
@Scanned
public class GlobalConfigServlet extends HttpServlet
{
  private final UserManager userManager;
  private final LoginUriProvider loginUriProvider;
  private SoyTemplateRenderer soyTemplateRenderer;
  private final PageBuilderService pageBuilderService;


  @Inject
  public GlobalConfigServlet(@ComponentImport UserManager userManager,
                             @ComponentImport LoginUriProvider loginUriProvider,
                             @ComponentImport SoyTemplateRenderer soyTemplateRenderer,
                             @ComponentImport PageBuilderService pageBuilderService)
  {
    this.userManager = userManager;
    this.loginUriProvider = loginUriProvider;
    this.soyTemplateRenderer = soyTemplateRenderer;
    this.pageBuilderService = pageBuilderService;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException
  {
    UserKey userKey = userManager.getRemoteUserKey(request);
    if (userKey == null || !userManager.isAdmin(userKey))
    {
      response.sendRedirect(this.loginUriProvider.getLoginUri(this.getUri(request)).toASCIIString());
    }

    response.setContentType("text/html;charset=UTF-8");
    this.pageBuilderService.assembler().resources().requireContext("com.cyanoth.secretwarden.globaladmin");

	soyTemplateRenderer.render(response.getWriter(), "com.cyanoth.secretwarden:secretwarden-globalconfig-ui-res",
	    "com.cyanoth.secretwarden.configPage", null);
  }

  private URI getUri(HttpServletRequest request)
  {
    StringBuffer builder = request.getRequestURL();
    if (request.getQueryString() != null)
    {
      builder.append("?");
      builder.append(request.getQueryString());
    }
    return URI.create(builder.toString());
  }

}