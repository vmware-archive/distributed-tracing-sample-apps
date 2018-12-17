using System.Collections.Generic;
using System.Net.Http;
using System.Threading;
using System.Threading.Tasks;
using OpenTracing;
using OpenTracing.Noop;
using OpenTracing.Propagation;

namespace BeachShirts.Common
{
    public class SpanContextPropagationHandler : DelegatingHandler
    {
        private readonly ITracer tracer;

        public SpanContextPropagationHandler(ITracer tracer)
        {
            this.tracer = tracer;
        }

        protected override async Task<HttpResponseMessage> SendAsync(
            HttpRequestMessage request, CancellationToken cancellationToken)
        {
            if (!(tracer is NoopTracer))
            {
                var activeSpan = tracer.ActiveSpan;
                if (activeSpan != null)
                {
                    var spanContextEntries = new Dictionary<string, string>();
                    tracer.Inject(activeSpan.Context, BuiltinFormats.HttpHeaders,
                                  new TextMapInjectAdapter(spanContextEntries));
                    foreach (var entry in spanContextEntries)
                    {
                        request.Headers.Add(entry.Key, entry.Value);
                    }
                }
            }
            return await base.SendAsync(request, cancellationToken);
        }
    }
}
