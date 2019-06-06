using BeachShirts.Common;
using BeachShirts.Common.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Logging;
using OpenTracing;
using OpenTracing.Tag;
using System;
using System.Collections.Generic;
using System.Net.Http;

namespace BeachShirts.Styling.Controllers
{
    [Route("api/style")]
    [ApiController]
    public class StylingController : Controller
    {
        private readonly string deliveryHost;
        private readonly IDictionary<string, ShirtStyle> shirtStyles;
        private readonly Random random;
        private readonly ILogger logger;
        private readonly ITracer tracer;
        private readonly HttpClient client;

        public StylingController(IConfiguration configuration, ILogger<StylingController> logger,
            ITracer tracer, IHttpClientFactory httpClientFactory)
        {
            deliveryHost = configuration.GetValue("DeliveryHost", Configuration.Host);
            shirtStyles = new Dictionary<string, Common.Models.ShirtStyle>(
                StringComparer.InvariantCultureIgnoreCase)
            {
                { "Wavefront", new Common.Models.ShirtStyle("Wavefront", "WavefrontUrl") },
                { "BeachOps", new Common.Models.ShirtStyle("BeachOps", "BeachOpsUrl") }
            };
            random = new Random();
            this.logger = logger;
            this.tracer = tracer;
            client = httpClientFactory.CreateClient(NamedHttpClients.SpanContextPropagationClient);
        }

        // GET api/style
        [HttpGet]
        public ActionResult<IEnumerable<ShirtStyle>> GetAllStyles()
        {
            using (var scope = Tracing.StartServerSpan(tracer, HttpContext, "GetAllStyles"))
            {
                return Ok(shirtStyles.Values);
            }
        }

        // GET api/style/{id}/make/{quantity}
        [Route("{id}/make/{quantity}")]
        [HttpGet]
        public ActionResult<DeliveryStatus> MakeShirts(string id, int quantity)
        {
            using (var scope = Tracing.StartServerSpan(tracer, HttpContext, "MakeShirts"))
            {
                if (random.Next(0, 5) == 0)
                {
                    string msg = "Failed to make shirts!";
                    logger.LogWarning(msg);
                    scope.Span.SetTag(Tags.Error, true);
                    return StatusCode(503, msg);
                }
                if (id == null)
                {
                    string msg = "style id is null";
                    logger.LogWarning(msg);
                    scope.Span.SetTag(Tags.Error, true);
                    return BadRequest(msg);
                }
                if (!shirtStyles.ContainsKey(id))
                {
                    string msg = "style id not found: " + id;
                    logger.LogWarning(msg);
                    scope.Span.SetTag(Tags.Error, true);
                    return BadRequest(msg);
                }
                if (quantity <= 0)
                {
                    string msg = "quantity is not a positive number: " + quantity;
                    logger.LogWarning(msg);
                    scope.Span.SetTag(Tags.Error, true);
                    return BadRequest(msg);
                }

                string orderNum = Guid.NewGuid().ToString();
                var shirts = new List<Shirt>();
                for (int i = 0; i < quantity; i++)
                {
                    shirts.Add(new Shirt(new ShirtStyle(id, id + "Image")));
                }
                var packedShirts = new PackedShirts(shirts);

                var deliveryResponse = Utils.HttpPost<PackedShirts, DeliveryStatus>(
                    client,
                    deliveryHost,
                    Configuration.DeliveryPort,
                    $"api/delivery/dispatch/{orderNum}",
                    packedShirts
                );

                if (deliveryResponse.Result is OkObjectResult)
                {
                    logger.LogInformation("Successfully made shirts!");
                    return deliveryResponse;
                }
                else
                {
                    string msg = "Failed to make shirts!";
                    logger.LogWarning(msg);
                    scope.Span.SetTag(Tags.Error, true);
                    return StatusCode(((StatusCodeResult)deliveryResponse.Result).StatusCode, msg);
                }
            }
        }
    }
}
