using System.Collections.Generic;
using Microsoft.AspNetCore.Mvc;
using BeachShirts.Common.Models;
using BeachShirts.Common;
using Microsoft.Extensions.Configuration;
using System;
using Microsoft.Extensions.Logging;
using OpenTracing;
using System.Net.Http;
using OpenTracing.Tag;

namespace BeachShirts.Shopping.Controllers
{
    [Route("api/shop")]
    [ApiController]
    public class ShoppingController : Controller
    {
        private readonly string stylingHost;
        private readonly Random random;
        private readonly ILogger logger;
        private readonly ITracer tracer;
        private readonly HttpClient client;

        public ShoppingController(IConfiguration configuration, ILogger<ShoppingController> logger,
            ITracer tracer, IHttpClientFactory httpClientFactory)
        {
            stylingHost = configuration.GetValue("StylingHost", Configuration.Host);
            random = new Random();
            this.logger = logger;
            this.tracer = tracer;
            client = httpClientFactory.CreateClient(NamedHttpClients.SpanContextPropagationClient);
        }

        // GET api/shop/menu
        [Route("menu")]
        [HttpGet]
        public ActionResult<IEnumerable<ShirtStyle>> GetShoppingMenu()
        {
            using (var scope = tracer.BuildSpan("GetShoppingMenu").StartActive(true))
            {
                return Utils.HttpGet<IEnumerable<ShirtStyle>>(
                    client,
                    stylingHost,
                    Configuration.StylingPort,
                    "api/style"
                );
            }
        }

        // POST api/shop/order
        [Route("order")]
        [HttpPost]
        public ActionResult<DeliveryStatus> OrderShirts(Order order)
        {
            using (var scope = tracer.BuildSpan("OrderShirts").StartActive(true))
            {
                if (random.Next(0, 10) == 0)
                {
                    string msg = "Failed to order shirts!";
                    logger.LogWarning(msg);
                    scope.Span.SetTag(Tags.Error, true);
                    return StatusCode(503, msg);
                }

                if (order.StyleName == null)
                {
                    string msg = "StyleName is null";
                    logger.LogWarning(msg);
                    scope.Span.SetTag(Tags.Error, true);
                    return BadRequest(msg);
                }
                if (order.Quantity <= 0)
                {
                    string msg = "Quantity is not a positive number: " + order.Quantity;
                    logger.LogWarning(msg);
                    scope.Span.SetTag(Tags.Error, true);
                    return BadRequest(msg);
                }

                var deliveryResponse = Utils.HttpGet<DeliveryStatus>(
                    client,
                    stylingHost,
                    Configuration.StylingPort,
                    $"api/style/{order.StyleName}/make/{order.Quantity}"
                );

                if (deliveryResponse.Result is OkObjectResult)
                {
                    logger.LogInformation("Successfully ordered shirts!");
                    return deliveryResponse;
                }
                else
                {
                    string msg = "Failed to order shirts!";
                    logger.LogWarning(msg);
                    scope.Span.SetTag(Tags.Error, true);
                    return StatusCode(((StatusCodeResult)deliveryResponse.Result).StatusCode, msg);
                }
            }
        }
    }
}
