using BeachShirts.Common;
using BeachShirts.Common.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Logging;
using System;
using System.Collections.Generic;

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

        public StylingController(IConfiguration configuration, ILogger<StylingController> logger)
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
        }

        // GET api/style
        [HttpGet]
        public ActionResult<IEnumerable<ShirtStyle>> GetAllStyles()
        {
            return Ok(shirtStyles.Values);
        }

        // GET api/style/{id}/make/{quantity}
        [Route("{id}/make/{quantity}")]
        [HttpGet]
        public ActionResult<DeliveryStatus> MakeShirts(string id, int quantity)
        {
            if (random.Next(0, 5) == 0)
            {
                string msg = "Failed to make shirts!";
                logger.LogWarning(msg);
                return StatusCode(503, msg);
            }
            if (id == null)
            {
                string msg = "style id is null";
                logger.LogWarning(msg);
                return BadRequest(msg);
            }
            if (!shirtStyles.ContainsKey(id))
            {
                string msg = "style id not found: " + id;
                logger.LogWarning(msg);
                return BadRequest(msg);
            }
            if (quantity <= 0)
            {
                string msg = "quantity is not a positive number: " + quantity;
                logger.LogWarning(msg);
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
                deliveryHost,
                Configuration.DeliveryPort,
                $"api/delivery/dispatch/{orderNum}",
                packedShirts
            );

            if (deliveryResponse.Result is OkObjectResult)
            {
                return deliveryResponse;
            }
            else
            {
                string msg = "Failed to make shirts!";
                logger.LogWarning(msg);
                return StatusCode(((StatusCodeResult)deliveryResponse.Result).StatusCode, msg);
            }
        }
    }
}
