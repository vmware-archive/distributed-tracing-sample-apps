const jaegerTracer = require('jaeger-client').initTracer;

module.exports.initTracer = (serviceName) => {
  const config = {
    serviceName: serviceName,
    sampler: {
      type: "const",
      param: 1,
    },
    reporter: {
       agentHost: "127.0.0.1",
       agentPort: 6832,
    },
  };
  return jaegerTracer(config);
};