using System;
using Jaeger.Samplers;
using Microsoft.AspNetCore.Http;
using Microsoft.Extensions.Logging.Abstractions;
using OpenTracing;
using OpenTracing.Propagation;
using OpenTracing.Tag;
using static Jaeger.Configuration;

namespace BeachShirts.Common
{
    public static class Tracing
    {
        public static ITracer Init(string service)
        {
            var loggerFactory = NullLoggerFactory.Instance;
            var samplerConfig = SamplerConfiguration.FromEnv(loggerFactory)
                .WithType(ConstSampler.Type)
                .WithParam(1);
            var reporterConfig = ReporterConfiguration.FromEnv(loggerFactory);
            var config = new Jaeger.Configuration(service, loggerFactory)
                .WithSampler(samplerConfig)
                .WithReporter(reporterConfig);
            return config.GetTracer();
        }

        public static IScope StartServerSpan(
            ITracer tracer, HttpContext context, string operationName)
        {
            ISpanBuilder spanBuilder;
            try
            {
                ISpanContext parentSpanCtx = tracer.Extract(BuiltinFormats.HttpHeaders,
                    new RequestHeadersExtractAdapter(context.Request.Headers));
                if (parentSpanCtx == null)
                {
                    spanBuilder = tracer.BuildSpan(operationName);
                }
                else
                {
                    spanBuilder = tracer.BuildSpan(operationName).AsChildOf(parentSpanCtx);
                }
            }
            catch (ArgumentException)
            {
                spanBuilder = tracer.BuildSpan(operationName);
            }
            return spanBuilder.WithTag(Tags.SpanKind.Key, Tags.SpanKindServer).StartActive(true);
        }
    }
}
