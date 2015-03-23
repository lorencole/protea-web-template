package cc.protea.foundation.example;

import cc.protea.foundation.utility.ProfoundConfiguration;
import cc.protea.foundation.utility.ProfoundServer;

public class Main {

	public static void main(final String[] args) throws Exception {

		ProfoundConfiguration.systemEmails.defaultTemplate = "platform-email";
		ProfoundConfiguration.publicUrl = "http://localhost.fieldraiser.com:8080";
		ProfoundConfiguration.servicePackages.add("cc.protea.foundation.example");
		ProfoundConfiguration.servicePackages.add("cc.protea.foundation.utility.example");
		ProfoundConfiguration.createSocialAccounts = true;

		ProfoundServer.start();

	}

}
