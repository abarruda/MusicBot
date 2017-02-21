package com.abarruda.musicbot.metrics;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class MetricsModule extends AbstractModule {
	
	@Override
	protected void configure() {
	}
	
	@Provides @Singleton
	public MetricRegistry getMetrics() {
		final MetricRegistry metrics = new MetricRegistry();
		final JmxReporter reporter =  JmxReporter.forRegistry(metrics).build();
		reporter.start();
		
		return metrics;
	}

}
