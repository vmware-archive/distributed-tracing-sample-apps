const jaegerTracer = require('jaeger-client').initTracer;

module.exports.initTracer = (serviceName) => {
  const config = {
    serviceName,
    sampler: {
      type: "const",
      param: 1,
    },
    reporter: {
       agentHost: "localhost",
       agentPort: 6832,
    },
  };
  return jaegerTracer(config);
};