const express = require('express');
const bodyParser = require('body-parser');
const config = require('../config.json');
const cors = require('cors')
const utils = require('./utils')

const shoppingController = require('./controllers/shoppingController');
const stylingController = require('./controllers/stylingController');
const deliveryController = require('./controllers/deliveryController');

// Configure the logger
const log = utils.getLogger()

const app = express();

// Enable CROS support
app.use(cors())

// Support JSON-encoded bodies
app.use(bodyParser.json());

// Support URL-encoded bodies
app.use(bodyParser.urlencoded({extended: false}));


// Fire controllers
shoppingController(app, config, log);
stylingController(app, config, log);
deliveryController(app, log);

// Start shopping service
utils.startServer(app, config.shopping, log)
// Start styling service
utils.startServer(app, config.styling, log)
// Start delivery service
utils.startServer(app, config.delivery, log)
console.log("Logging to beachshirt.log")
