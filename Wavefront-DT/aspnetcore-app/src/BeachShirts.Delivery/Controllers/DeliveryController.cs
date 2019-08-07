using System;
using System.Threading;
using System.Threading.Tasks;
using BeachShirts.Common;
using BeachShirts.Common.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Logging;
using OpenTracing;
using OpenTracing.Tag;

namespace BeachShirts.Delivery.Controllers
{
    [Route("api/delivery")]
    [ApiController]
    public class DeliveryController : Controller
    {
        private readonly Random random;
        private readonly ILogger logger;
        private readonly ITracer tracer;

        public DeliveryController(ITracer tracer, ILogger<DeliveryController> logger)
        {
            random = new Random();
            this.logger = logger;
            this.tracer = tracer;
        }

        // POST api/delivery/dispatch/{orderNum}
        [Route("dispatch/{orderNum}")]
        [HttpPost]
        public ActionResult<DeliveryStatus> Dispatch(string orderNum, PackedShirts packedShirts)
        {
            using (var scope = Tracing.StartServerSpan(tracer, HttpContext, "Dispatch"))
            {
                Thread.Sleep(random.Next(1, 5));
                if (random.Next(0, 5) == 0)
                {
                    string msg = "Failed to dispatch shirts!";
                    logger.LogWarning(msg);
                    scope.Span.SetTag(Tags.Error, true);
                    return StatusCode(503, msg);
                }
                if (random.Next(0, 10) == 0)
                {
                    orderNum = "";
                }
                if (string.IsNullOrWhiteSpace(orderNum))
                {
                    string msg = "Invalid Order Num";
                    logger.LogWarning(msg);
                    scope.Span.SetTag(Tags.Error, true);
                    return BadRequest(msg);
                }
                if (packedShirts == null || packedShirts.Shirts == null || packedShirts.Shirts.Count == 0)
                {
                    string msg = "No shirts to deliver";
                    logger.LogWarning(msg);
                    scope.Span.SetTag(Tags.Error, true);
                    return BadRequest(msg);
                }
                Task.Run(async () => await HandleDispatchAsync());
                string trackingNum = Guid.NewGuid().ToString();
                logger.LogInformation($"Successfully dispatched shirts! Tracking number of Order {orderNum} is {trackingNum}");
                return Ok(new DeliveryStatus(orderNum, trackingNum, "shirts delivery dispatched"));
            }
        }

        private async Task HandleDispatchAsync()
        {
            await Task.Delay(TimeSpan.FromSeconds(1 + random.NextDouble() / 3));
        }

        // POST api/delivery/return/{orderNum}
        [Route("return/{orderNum}")]
        [HttpPost]
        public ActionResult Retrieve(string orderNum)
        {
            using (var scope = Tracing.StartServerSpan(tracer, HttpContext, "Retrieve"))
            {
                if (string.IsNullOrWhiteSpace(orderNum))
                {
                    string msg = "Invalid Order Num";
                    logger.LogWarning(msg);
                    scope.Span.SetTag(Tags.Error, true);
                    return BadRequest(msg);
                }
                return Ok($"Order {orderNum} returned");
            }
        }
    }
}
