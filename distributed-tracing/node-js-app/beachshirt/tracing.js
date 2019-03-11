const jaegerTracer = require('jaeger-client').initTracer;

module.exports.initTracer = (serviceName) => {
  const config = {
    serviceName,
    sampler: {
      type: "const",
      param: 1,
    },
    reporter: {
       agentHost: "10.192.213.225",
       agentPort: 6832,
    },
  };
  return jaegerTracer(config);
};