/**
* Driver for Shopping service provides consumer facing APIs supporting activities like browsing
* different styles of beachshirts, and ordering beachshirts.
* @author Yogesh Prasad Kurmi (ykurmi@vmware.com)
*/
const request = require('request');
const utils = require('../utils')

module.exports = (app, config, log) => {

    app.get('/shop/menu', (req, res) => {
        return utils.getRequest(res, "/style/", {}, config.styling)
    });

    app.post('/shop/order', (req, res) => {
        if (Math.floor(Math.random() * 10) === 0) {
            const error = "Failed to order shirts!";
            log.error(error);
            return res.status(503).json({ error: error });
        }
        return utils.getRequest(res,
            `/style/${req.body.styleName}/make`,
            {"quantity":req.body.quantity},
            config.styling)
    });
};  