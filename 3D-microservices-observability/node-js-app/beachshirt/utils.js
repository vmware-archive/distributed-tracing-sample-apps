const log4js = require('log4js')
const request = require('request');

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
exports.getRequest = (res, api, param, service) => {
  request.get({
    url: `${this.getBaseUrl(service)}${api}`,
    headers: {"Content-Type":"application/json"},
    qs: param},
    (error, response, body) => {
        if(error){
            return res.status(500).json({'error': error.message})
        }
        return res.status(response.statusCode).json(JSON.parse(body));
     });
} 

// call post request
exports.postRequest = (res, api, formData, service) => {
  request.post({
    url: `${this.getBaseUrl(service)}${api}`,
    headers: {"Content-Type":"application/json"},
    form: formData
  }, (error, response, body) => {
    if(error){
        return res.status(500).json({'error': error.message})
    }
      return res.status(response.statusCode).json(JSON.parse(body));
 })
}