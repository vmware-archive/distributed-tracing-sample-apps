/**
* Driver for Shopping service provides consumer facing APIs supporting activities like browsing
* different styles of beachshirts, and ordering beachshirts.
* @author Yogesh Prasad Kurmi (ykurmi@vmware.com)
*/
const utils = require('../utils')
const { Tags, FORMAT_HTTP_HEADERS } = require('opentracing');
module.exports = (app, config, log, tracer) => {

    app.get('/shop/menu', (req, res) => {
        let span = tracer.startSpan('/shop/menu');
        return utils.getRequest(res, "/style/", {}, config.styling, span, tracer);
    });

    app.post('/shop/order', (req, res) => {
        let span = tracer.startSpan('/shop/order');
        if (Math.floor(Math.random() * 10) === 0) {
            const error = "Failed to order shirts!";
            log.error(error);
            span.setTag(Tags.HTTP_STATUS_CODE, 503)
            span.setTag(Tags.ERROR, true)
            span.finish();
            return res.status(503).json({ error: error });
        }
        return utils.getRequest(res,
            `/style/${req.body.styleName}/make`,
            {"quantity":req.body.quantity},
            config.styling, span, tracer)
    });
};  