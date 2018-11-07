package com.wfsample.constants;

import com.codahale.metrics.MetricRegistry;

public abstract class CommonRegistry {
    public static final MetricRegistry METRIC_REGISTRY = new MetricRegistry();
}
