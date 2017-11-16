package edu.unh.cs753853.team1;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class AppInitialise implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		System.out.println("Application Starting up!");

		// Call function to initialize project.
	}

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
		System.out.println("Shutting down!");
		// Can Call function to clean up cache or any other clean up.
	}
}
