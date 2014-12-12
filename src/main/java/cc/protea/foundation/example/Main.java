package cc.protea.foundation.example;

import cc.protea.foundation.ProteaFoundation;

public class Main {

	public static void main(final String[] args) throws Exception {
		ProteaFoundation server = new ProteaFoundation();
		server.addServicePackage("cc.protea.foundation.example");
		server.start();
	}

}
