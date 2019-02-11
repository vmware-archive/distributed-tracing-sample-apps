const express = require('express');
const bodyParser = require('body-parser');
const config = require('../config.json');
const log4js = require('log4js')

const shoppingController = require('./controllers/shoppingController');
const stylingController = require('./controllers/stylingController');
const deliveryController = require('./controllers/deliveryController');

// Configure the logger
log4js.configure({
    appenders: {
      everything: { type: 'file', filename: 'beachshirt.log' }
    },
    categories: {
      default: { appenders: [ 'everything' ], level: 'debug' }
    }
  });

log = log4js.getLogger();

const app = express();

// Support JSON-encoded bodies
app.use(bodyParser.json());

// Support URL-encoded bodies
app.use(bodyParser.urlencoded({extended: false}));

// Fire controllers
shoppingController(app, config, log);
stylingController(app, config, log);
deliveryController(app, log);

// Listen to port
app.listen(config.shopping.port);
log.debug('Shopping service is listening on port ' + config.shopping.port);
app.listen(config.styling.port);
log.debug('styling service is listening on port ' +  config.styling.port);
app.listen(config.delivery.port);
log.debug('delivery service is listening on port ' + config.delivery.port);
console.log("Server started. Logging to beachshirt.log")
