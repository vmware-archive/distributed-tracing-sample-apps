using System;
using Microsoft.AspNetCore.Http;
using OpenTracing;
using OpenTracing.Noop;
using OpenTracing.Propagation;
using OpenTracing.Tag;

namespace BeachShirts.Common
{
    public static class Tracing
    {
        public static ITracer Init(string service)
        {
            // TODO: Replace this with Wavefront Tracer
            return NoopTracerFactory.Create();
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
