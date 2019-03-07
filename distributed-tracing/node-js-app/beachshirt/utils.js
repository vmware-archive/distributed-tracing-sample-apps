const log4js = require('log4js')
const request = require('request');
const { Tags, FORMAT_HTTP_HEADERS } = require('opentracing');

// prepares the base url
exports.getBaseUrl = (service) => {
    return `http://${service.host}:${service.port}`;
}

// prepares the logger
exports.getLogger = () => {
  // Configure the logger
  log4js.configure({
    appenders: {
      everything: { type: 'file', filename: 'beachshirt.log' }
    },
    categories: {
      default: { appenders: [ 'everything' ], level: 'debug' }
    }
  });
  return log4js.getLogger();
}

// Start the server
exports.startServer = (app, service, log) => {
  app.listen(service.port, () => {
    log.debug(`${service.service} service is listening on port ${service.port}`)
  }).on('error', (error) => {
    log.error(`Unable to start ${service.service} service on port ${service.port}. Error: ${error.message}`);
  });
}

// call get request
exports.getRequest = (res, api, param, service, span, tracer) => {
  let http_url = `${this.getBaseUrl(service)}${api}`;
  let http_headers = {"Content-Type":"application/json"};
  span.setTag(Tags.HTTP_URL, http_url);
  span.setTag(Tags.HTTP_METHOD, 'GET');
  span.setTag(Tags.SPAN_KIND, Tags.SPAN_KIND_RPC_CLIENT);
  // Send span context via request headers (parent id etc.)
  tracer.inject(span, FORMAT_HTTP_HEADERS, http_headers);

  request.get({
    url: http_url,
    headers: http_headers,
    qs: param},
    (error, response, body) => {
        if(error){
            span.setTag(Tags.HTTP_STATUS_CODE, 500)
            span.setTag(Tags.ERROR, true)
            span.finish();
            return res.status(500).json({'error': error.message})
        }
        span.setTag(Tags.HTTP_STATUS_CODE, response.statusCode)
        span.finish();
        return res.status(response.statusCode).json(JSON.parse(body));
     });
} 

// call post request
exports.postRequest = (res, api, formData, service, span, tracer) => {
  let http_url = `${this.getBaseUrl(service)}${api}`;
  let http_headers = {"Content-Type":"application/json"};
  span.setTag(Tags.HTTP_URL, http_url);
  span.setTag(Tags.HTTP_METHOD, 'POST');
  span.setTag(Tags.SPAN_KIND, Tags.SPAN_KIND_RPC_CLIENT);
  // Send span context via request headers (parent id etc.)
  tracer.inject(span, FORMAT_HTTP_HEADERS, http_headers);

  request.post({
    url: http_url,
    headers: http_headers,
    form: formData
  }, (error, response, body) => {
    if(error){
        span.setTag(Tags.HTTP_STATUS_CODE, 500)
        span.setTag(Tags.ERROR, true)
        span.finish();
        return res.status(500).json({'error': error.message})
    }
      span.setTag(Tags.HTTP_STATUS_CODE, response.statusCode)
      span.finish();
      return res.status(response.statusCode).json(JSON.parse(body));
 })
}