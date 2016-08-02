package cc.protea.foundation.example;

import org.eclipse.jetty.servlet.ServletContextHandler;

import cc.protea.foundation.ProteaFoundation;
import cc.protea.foundation.template.model.TemplateUser;
import cc.protea.platform.ProfoundConfiguration;
import cc.protea.platform.ProfoundServer;
import cc.protea.platform.ProfoundConfiguration.Storage.Service;

public class Main {

	public static void main(final String[] args) throws Exception {

		ProfoundConfiguration.systemEmails.defaultTemplate = "platform-email";
		//ProfoundConfiguration.publicUrl = "http://protea-web-template-react.herokuapp.com/";
		ProfoundConfiguration.publicUrl = "http://localhost:8080";
		ProfoundConfiguration.servicePackages.add("cc.protea.foundation.template.services");
		ProfoundConfiguration.createSocialAccounts = true;

		ProfoundConfiguration.storage.sessions = Service.REDIS;
		ProfoundConfiguration.userClass = TemplateUser.class;
		
		ProteaFoundation foundation = new ProteaFoundation() {
			@Override
			public void addStaticHtmlServlet(final ServletContextHandler context) {
				super.addStaticHtmlServlet(context);
				context.setResourceBase("webapp/public");
				context.setWelcomeFiles(new String[] {"app/index.html"});
			}
		};
		
		ProfoundServer.start(foundation);

	}

}
